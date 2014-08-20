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
    private String spotPriceUrlFormat = "http://finance.yahoo.com/d/quotes.csv?s=%s&f=s0b2a5b3b6l1&e=.csv";
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
                            String[] value = line.split(",");

                            if (!value[0].equalsIgnoreCase("N/A") && !value[1].equalsIgnoreCase("N/A")
                                    && !value[3].equalsIgnoreCase("N/A") && !value[5].equalsIgnoreCase("N/A")) {
                                String tickerSymbol = value[0];
                                double ask = Double.parseDouble(value[1]);
                                int askSize = value[2].equalsIgnoreCase("N/A") ? 0 : Integer.parseInt(value[2]);
                                double bid = Double.parseDouble(value[3]);
                                int bidSize = value[4].equalsIgnoreCase("N/A") ? 0 : Integer.parseInt(value[4]);
                                double lastTradePrice = Double.parseDouble(value[5]);
                                Quote quote = new Quote(tickerSymbol, ask, askSize, bid, bidSize, lastTradePrice, new Date());
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
