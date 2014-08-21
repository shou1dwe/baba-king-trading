package marketdatamanagement;

import marketdatamanagement.callbacks.OnCompanyInfoReceiveListener;
import marketdatamanagement.callbacks.OnSpotPriceReceiveListener;

import java.util.Set;

/**
 * Created by user on 8/16/2014.
 */
public interface MarketDataSource {
    play.libs.F.Promise<Set<marketdatamanagement.datatransferobjects.Quote>> getSpotPrices(Set<String> tickers);
    play.libs.F.Promise<models.Stock> getCompanyInfoById(String ticker, OnCompanyInfoReceiveListener onCompanyInfoReceiveListener);
}
