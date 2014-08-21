package marketdatamanagement;

import com.google.common.base.Strings;
import marketdatamanagement.callbacks.OnCompanyInfoReceiveListener;
import marketdatamanagement.callbacks.OnSpotPriceReceiveListener;
import marketdatamanagement.datatransferobjects.Quote;
import models.Stock;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.Date;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by user on 8/16/2014.
 */
public class YahooMarketData implements MarketDataSource {
    private String spotPriceUrlFormat = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=s0b2s0a5s0b3s0b6s0l1s0c6&e=.csv";
    private String companyInfoUrlFormat = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=s0i0n0n4x0&e=.csv";

    @Override
    public F.Promise<Quote> getSpotPrice(Set<String> tickers, final OnSpotPriceReceiveListener onSpotPriceReceiveListener) {
        return WS.url(generateSpotPriceURL(tickers)).get().map(
                new F.Function<WSResponse, Quote>() {
                    public Quote apply(WSResponse response) {
                        String content = response.getBody();
                        Scanner scanner = new Scanner(content);
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();

                            String[] valueForName = line.split(",");
                            String ticker = valueForName[0];
                            ticker = ticker.substring(1,ticker.length()-1);

                            line = line.replace("\"","");
                            line = line.replace(String.format(",%s,",ticker),"-");
                            line = line.replace(String.format("%s,",ticker),"");
                            String[] values = line.split("-");

                            if (!ticker.equalsIgnoreCase("N/A") && !values[0].equalsIgnoreCase("N/A")
                                    && !values[2].equalsIgnoreCase("N/A") && !values[4].equalsIgnoreCase("N/A")) {
                                double ask = Double.parseDouble(values[0].replace(",",""));
                                int askSize = values[1].equalsIgnoreCase("N/A") ? -1 : Integer.parseInt(values[1].replace(",",""));
                                double bid = Double.parseDouble(values[2].replace(",",""));
                                int bidSize = values[3].equalsIgnoreCase("N/A") ? -1 : Integer.parseInt(values[3].replace(",",""));
                                double lastTradePrice = Double.parseDouble(values[4].replace(",",""));
                                double change = Double.parseDouble(values[5].replace(",",""));
                                Quote quote = new Quote(ticker, ask, askSize, bid, bidSize, lastTradePrice, change, new Date());
                                onSpotPriceReceiveListener.onSpotPriceReceived(quote);
                            }
                        }
                        scanner.close();
                        System.out.println(response.getBody());
                        return null;
                    }
                }
        );
    }

    @Override
    public F.Promise<Stock> getCompanyInfoById(String ticker, final OnCompanyInfoReceiveListener onCompanyInfoReceiveListener) {
        return WS.url(generateCompanyInfoURL(ticker)).get().map(
                new F.Function<WSResponse, Stock>() {
                    public Stock apply(WSResponse response) {
                        String content = response.getBody();
                        Scanner scanner = new Scanner(content);
                        if (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            String[] value = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                            String symbol = removeQuotes(value[0]);
                            String moreInfo = removeQuotes(value[1]);
                            String companyName = removeQuotes(value[2]);
                            String notes = removeQuotes(value[3]);
                            String exchange = removeQuotes(value[4]);
                            Stock stock = new Stock(symbol, companyName, moreInfo, notes, exchange);
                            onCompanyInfoReceiveListener.onCompanyInfoReceive(stock);
                            return stock;
                        }
                        scanner.close();
                        System.out.println(response.getBody());
                        return null;
                    }
                }
        );
    }

    private String removeQuotes(String toBeProcessed) {
        return toBeProcessed.replaceAll("^\"|\"$", "");
    }

    private String generateCompanyInfoURL(String ticker) {
        return String.format(companyInfoUrlFormat, ticker);
    }

    private String generateSpotPriceURL(Set<String> tickers) {
        StringBuilder sb = new StringBuilder();
        for(String ticker : tickers){
            sb.append(ticker);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return String.format(spotPriceUrlFormat, sb.toString());
    }
}
