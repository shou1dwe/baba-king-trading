package executionmanagement;

import datamanagement.TruffleDataManager;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.callbacks.OnCompanyInfoReceiveListener;
import marketdatamanagement.datatransferobjects.Quote;
import models.Stock;

/**
 * Author: Xiawei
 */
public class AdhocExecutionManager {
    private TruffleDataManager truffleDataManager;
    private MarketDataManager marketDataManager;

    public AdhocExecutionManager(TruffleDataManager truffleDataManager, MarketDataManager marketDataManager) {
        this.truffleDataManager = truffleDataManager;
        this.marketDataManager = marketDataManager;
    }

    public Stock retriveStockInformationByTicker(String tickerSymbol){
        Stock stock = truffleDataManager.getStockByTicker(tickerSymbol);
        if (stock == null) {
            stock = marketDataManager.getCompanyInfo(tickerSymbol, new OnCompanyInfoReceiveListener() {
                @Override
                public void onCompanyInfoReceive(Stock companyInfo) {
                    truffleDataManager.insertStock(companyInfo.ticker, companyInfo.companyName, companyInfo.moreInfo, companyInfo.notes, companyInfo.exchange);
                }
            });
        }

        return stock;
    }

    public Quote getLatestQuote(String tickerSymbol) {
        return marketDataManager.getSpotPrice(tickerSymbol);
    }
}
