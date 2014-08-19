package marketdatamanagement;

import com.fasterxml.jackson.databind.JsonNode;
import marketdatamanagement.datatransferobjects.Quote;
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
    private String spotPriceUrlFormat = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=s0b2a5b3b6&e=.csv";
    private String spotPriceUrl;

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

//                                        while(true){
//                                            String ticker = "1st column";
//                                            Quote quote = new Quote(); //"from the rest values";
//                                            latestPrices.put(ticker, quote);
//                                        }
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
        return new Quote("AAPL", 95.6, 1000, 95.3, 2000, new Date());
//        return latestPrices.get(stock);
    }

    public double getShortAverage(String stock, int shortPeriod) {
        // TODO thread-safe fix needed
        List<Double> prices = historicPrices.get(stock);
        int count = (shortPeriod*60/UPDATE_RATE); // How many entries we need for calculating shortPeriod average
        double sum = 0;
        int sizeOfPrices = prices.size();
        for(int i=0 ; i<count; i++){
            sum += prices.get(sizeOfPrices - 1 - i);
        }
        return sum/count;
    }

    public double getLongAverage(String stock, int longPeriod) {

        return 0;
    }


}
