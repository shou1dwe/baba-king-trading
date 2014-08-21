package marketdatamanagement;

import com.google.common.base.Strings;
import marketdatamanagement.callbacks.OnCompanyInfoReceiveListener;
import marketdatamanagement.datatransferobjects.Quote;
import marketdatamanagement.exceptions.NoLongAverageAvailableException;
import marketdatamanagement.exceptions.NoShortAverageAvailableException;
import models.Stock;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Author: Xiawei
 */
public class MarketDataManager {
    private final MarketDataSource dataSource;

    public static final int UPDATE_RATE = 10;
    public static final Map<String, Quote> latestPrices = new HashMap<>();
    public static final Map<String, List<Double>> historicPrices = new HashMap<>();
    public static final Map<String, Double> longAverages = new HashMap<>();

    private String historicalUrlFormat = "http://ichart.finance.yahoo.com/table.csv?s=%s&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d&g=d&ignore=.csv";
    private String historicalUrl;

    public MarketDataManager(){
        dataSource = new YahooMarketData();
    }

    public void startQuotesUpdate(){
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS),
                Duration.create(UPDATE_RATE, TimeUnit.SECONDS),
                new Runnable() {
                    public void run() {
                        final Map<String, Quote> latestPrices = MarketDataManager.latestPrices;
                        if(!latestPrices.isEmpty()) {
                            Set<Quote> quotes = dataSource.getSpotPrices(latestPrices.keySet()).get(10000);
//                            Logger.debug("Quote added: {} stock", quotes.size());
                            for(Quote quote : quotes){
                                if(quote != null){
                                    MarketDataManager.latestPrices.put(quote.getTickerSymbol(), quote);
                                    List<Double> historicalPrices = historicPrices.get(quote.getTickerSymbol());
                                    if (historicalPrices != null) {
                                        historicalPrices.add(quote.getLastTradePrice());
                                    } else {
                                        historicalPrices = new ArrayList<>();
                                        historicalPrices.add(quote.getLastTradePrice());
                                    }
                                }
                            }

                        } else {
                        }
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public Stock getCompanyInfo(String ticker, OnCompanyInfoReceiveListener onCompanyInfoReceiveListener){
        F.Promise<Stock> stockInfoUpdated = dataSource.getCompanyInfoById(ticker, onCompanyInfoReceiveListener);
        return stockInfoUpdated.get(30000);
    }

    public String toUpperCaseSafe(String input){
        if(!Strings.isNullOrEmpty(input)){
            return input.toUpperCase();
        }
        return null;
    }

    public boolean isStockSubscribed(String ticker) {
        ticker = toUpperCaseSafe(ticker);
        return MarketDataManager.latestPrices.containsKey(ticker);
    }

    public void subscribe(String ticker) {
        ticker = toUpperCaseSafe(ticker);
        if (!isStockSubscribed(ticker)){
            MarketDataManager.latestPrices.put(ticker, null);
            Logger.debug("Subscribe prices for {}", ticker);
        } else {
        }

        if (!historicPrices.containsKey(ticker)) {
            historicPrices.put(ticker, new ArrayList<Double>());
        }
    }

    public Quote getSpotPrice(String ticker) {
        ticker = toUpperCaseSafe(ticker);
        Quote quote = MarketDataManager.latestPrices.get(ticker);
//        Logger.debug("Getting Spot Price for {}: {}", ticker, quote);
        if(quote != null){
            return quote;
        }
        return null;
    }

    public Quote getSpotPriceRealtime(String ticker){
        Set<String> tickers = new HashSet<String>();
        tickers.add(ticker);
        int counter = 5;
        while (counter>0){
            Set<Quote> quotes = dataSource.getSpotPrices(tickers).get(5000);
            if(!quotes.isEmpty()){
                Quote next = quotes.iterator().next();
                Logger.debug("Realtime Quote: {}", next);
                return next;
            }
            counter--;
        }
        return null;
    }

    public double getShortAverage(String stock, int shortPeriod) throws NoShortAverageAvailableException {
        // TODO thread-safe fix needed
        List<Double> prices = historicPrices.get(stock);
        int count = (shortPeriod*60/UPDATE_RATE); // How many entries we need for calculating shortPeriod average
        double sum = 0;
        int sizeOfPrices = prices.size();
        if(sizeOfPrices < count){
            throw new NoShortAverageAvailableException("There is not enough data for short average calculation.");
        } else {
            for (int i = 0; i < count; i++) {
                sum += prices.get(sizeOfPrices - 1 - i);
            }
        }
        return sum/count;
    }

    public double getLongAverage(final String stock, final int longPeriod) throws NoLongAverageAvailableException {
        final String longAverageKey = String.format("%s-%d", stock, longPeriod).toUpperCase();
        if(longAverages.containsKey(longAverageKey) && longAverages.get(longAverageKey)!=null){
            return longAverages.get(longAverageKey);
        }else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();

            cal1.add(Calendar.DATE,(-1)*(longPeriod+2*((longPeriod%7)+1)));
            cal2.add(Calendar.DATE, -1);

            historicalUrl = String.format(historicalUrlFormat, stock, cal1.get(Calendar.MONTH),cal1.get(Calendar.DATE),cal1.get(Calendar.YEAR),
                    cal2.get(Calendar.MONTH),cal2.get(Calendar.DATE),cal2.get(Calendar.YEAR));
            F.Promise<Double> jsonPromise = WS.url(historicalUrl).get().map(
                    new F.Function<WSResponse, Double>() {
                        public Double apply(WSResponse response) {
                            String content = response.getBody();
                            Scanner scanner = new Scanner(content);
                            Double result = null;
                            double sum=0;
                            scanner.nextLine(); //skip the first line

                            for (int i=0; i<longPeriod; i++){
                                if (scanner.hasNext()){
                                    String line = scanner.nextLine();
                                    String[] values = line.split(",");
                                    sum+=Double.parseDouble(values[4]);
                                }
                            }
                            result = sum/longPeriod;
                            longAverages.put(longAverageKey, result);
                            scanner.close();
                            return result;
                        }
                    }
            );
            Double result = jsonPromise.get(5000);
            if (result != null){
                return result;
            }else {
                throw new NoLongAverageAvailableException("There is not available data for long average calculation at the moment.");
            }
        }
    }

    public String getHistoricalUrl() {
        return historicalUrl;
    }
}
