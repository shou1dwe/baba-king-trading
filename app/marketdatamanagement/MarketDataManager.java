package marketdatamanagement;

import marketdatamanagement.datatransferobjects.Quote;
import marketdatamanagement.exceptions.NoLongAverageAvailableException;
import marketdatamanagement.exceptions.NoShortAverageAvailableException;
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
    private static final int UPDATE_RATE = 10;
    private Map<String, Quote> latestPrices = new HashMap<>();
    private Map<String, List<Double>> historicPrices = new HashMap<>();
    private Map<String, Double> longAverages = new HashMap<>();

    private String spotPriceUrlFormat = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=s0b2a5b3b6l1&e=.csv";
    private String spotPriceUrl;
    private String historicalUrlFormat = "http://ichart.finance.yahoo.com/table.csv?s=%s&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d&g=d&ignore=.csv";
    private String historicalUrl;

    public void startQuotesUpdate(){
        if(latestPrices.isEmpty()){
            historicPrices.put("AAPL", null);
            historicPrices.put("IBM", null);
            historicPrices.put("GOOG", null);
        }

        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS),
                Duration.create(UPDATE_RATE, TimeUnit.SECONDS),
                new Runnable() {
                    public void run() {
                        F.Promise<Void> jsonPromise = WS.url(spotPriceUrl).get().map(
                                new F.Function<WSResponse, Void>() {
                                    public Void apply(WSResponse response) {
                                        String content = response.getBody();
                                        Scanner scanner = new Scanner(content);
                                        while (scanner.hasNextLine()) {
                                            String line = scanner.nextLine();
                                            String[] value = line.split(",");

                                            if(!value[0].equalsIgnoreCase("N/A") && !value[1].equalsIgnoreCase("N/A")
                                                    && !value[3].equalsIgnoreCase("N/A") && !value[5].equalsIgnoreCase("N/A")){
                                                String tickerSymbol = value[0];
                                                double ask = Double.parseDouble(value[1]);
                                                int askSize = value[2].equalsIgnoreCase("N/A")?0:Integer.parseInt(value[2]);
                                                double bid =Double.parseDouble(value[3]);
                                                int bidSize = value[4].equalsIgnoreCase("N/A")?0:Integer.parseInt(value[4]);
                                                double lastTradePrice = Double.parseDouble(value[5]);
                                                Quote newQuote = new Quote(tickerSymbol,ask,askSize,bid,bidSize,lastTradePrice,new Date());
                                                latestPrices.put(newQuote.getTickerSymbol(),newQuote);

                                                List<Double> list = historicPrices.get(tickerSymbol);
                                                list.add(lastTradePrice);
                                            }
                                        }
                                        scanner.close();
                                        System.out.println(response.getBody());
                                        return null;
                                    }
                                }
                        );
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public boolean isStockSubscribed(String ticker) {
        return latestPrices.containsKey(ticker);
    }

    public void subscribe(String ticker) {
        if (!latestPrices.containsKey(ticker)){
            latestPrices.put(ticker, null);
            regenerateURL();
        }
    }
    private void regenerateURL() {
        StringBuilder sb = new StringBuilder();
        for(String ticker : latestPrices.keySet()){
            sb.append(ticker);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        spotPriceUrl = String.format(spotPriceUrlFormat, sb.toString());
    }

    public Quote getSpotPrice(String stock) {
        return new Quote("AAPL", 95.6, 1000, 95.3, 2000, 93.4, new Date());
//        return latestPrices.get(stock);
    }

    public double getShortAverage(String stock, int shortPeriod) throws NoShortAverageAvailableException {
        // TODO thread-safe fix needed
        List<Double> prices = historicPrices.get(stock);
        int count = (shortPeriod*60/UPDATE_RATE); // How many entries we need for calculating shortPeriod average
        double sum = 0;
        int sizeOfPrices = prices.size();

        if(sizeOfPrices < count){
            throw new NoShortAverageAvailableException("There is not enough data for short average calculation.");
        }

        for(int i=0 ; i<count; i++){
            sum += prices.get(sizeOfPrices - 1 - i);
        }
        return sum/count;
    }

    public double getLongAverage(final String stock, final int longPeriod) throws NoLongAverageAvailableException {
        final String longAverageKey = String.format("%s-%d", stock, longPeriod);
        if(longAverages.containsKey(longAverageKey) && longAverages.get(longAverageKey)!=null){
            return longAverages.get(longAverageKey);
        }else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();

            cal1.add(Calendar.DATE, -1);
            cal2.add(Calendar.DATE,(-1)*(longPeriod+2*((longPeriod%7)+1)));

            historicalUrl = String.format(historicalUrlFormat, stock, cal1.get(Calendar.DATE),cal1.get(Calendar.MONTH),cal1.get(Calendar.YEAR),
                    cal2.get(Calendar.DATE),cal2.get(Calendar.MONTH),cal2.get(Calendar.YEAR));

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
                            longAverages.put(stock, result);
                            scanner.close();
                            System.out.println(response.getBody());
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

}
