package executionmanagement;

import datamanagement.TruffleDataManager;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.callbacks.OnCompanyInfoReceiveListener;
import marketdatamanagement.datatransferobjects.Quote;
import models.Stock;
import models.Strategy;
import play.Logger;

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
        Logger.debug("Retriving {} stock info from database...", tickerSymbol);
        if (stock == null) {
            Logger.debug("{} stock info not exist in database...", tickerSymbol);
            stock = marketDataManager.getCompanyInfo(tickerSymbol, new OnCompanyInfoReceiveListener() {
                @Override
                public void onCompanyInfoReceive(Stock companyInfo) {
                    marketDataManager.subscribe(companyInfo.ticker);
                    truffleDataManager.insertStock(companyInfo.ticker, companyInfo.companyName, companyInfo.moreInfo, companyInfo.notes, companyInfo.exchange);
                }

                @Override
                public void onCompanyNotExist() {
                    // do nothing
                }
            });
            return stock;
        } else {
            marketDataManager.subscribe(stock.ticker);
            return stock;
        }

    }

    public Quote getLatestQuote(String tickerSymbol) {
        Quote quote = marketDataManager.getSpotPrice(tickerSymbol);
        if(quote==null){
            quote = marketDataManager.getSpotPriceRealtime(tickerSymbol);
        }
        return quote;
    }

    public Strategy getStrategy(String id){
        return truffleDataManager.getStrategyById(id);
    }
}
