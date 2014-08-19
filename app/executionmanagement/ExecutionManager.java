package executionmanagement;

import OrderManagement.TruffleOrderManager;
import OrderManagement.callbacks.TruffleOrderConfirmationListener;
import OrderManagement.datatransferobjects.Trade;
import datamanagement.TruffleDataManager;
import executionmanagement.datatransferobjects.Position;
import executionmanagement.datatransferobjects.Strategy;
import executionmanagement.datatransferobjects.TwoMovingAveragesStrategy;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.datatransferobjects.Quote;
import marketdatamanagement.exceptions.NoLongAverageAvailableException;
import marketdatamanagement.exceptions.NoShortAverageAvailableException;
import models.ActionHistory;

import java.util.*;

/**
 * Author: Xiawei
 */
public class ExecutionManager {
    private TruffleDataManager truffleDataManager;
    private TruffleOrderManager truffleOrderManager;
    private Map<String, Strategy> strategies;
    private Map<Integer, Position> tradePositionMap;
    private MarketDataManager marketDataManager;

    public ExecutionManager(MarketDataManager marketDataManager, TruffleOrderManager truffleOrderManager, TruffleDataManager truffleDataManager) {
        this.truffleOrderManager = truffleOrderManager;
        this.marketDataManager = marketDataManager;
        this.truffleDataManager = truffleDataManager;
        tradePositionMap = new HashMap<>();
        strategies = new HashMap<String, Strategy>();
    }

    public void addTwoMovingAveragesStrategy(String ticker, int longPeriod, int shortPeriod, int volume,
                                                    double percentLoss, double percentProfit) {
        String id = String.format("Truffle-%s-%d", ticker, System.currentTimeMillis()/1000L);
        if (!marketDataManager.isStockSubscribed(ticker))
            marketDataManager.subscribe(ticker);

        TwoMovingAveragesStrategy newStrategy = new TwoMovingAveragesStrategy(id, longPeriod, shortPeriod, volume, ticker, percentLoss, percentProfit);
        truffleDataManager.insertTwoMovingAveragesStrategy(newStrategy);
        strategies.put(id, newStrategy);
        System.out.println("Strategy added.");
    }

    public void startExecution() {
        Collection<Strategy> values = new ArrayList<Strategy>(strategies.values());
        for (Strategy strategy : values) {
            if (strategy.getRemaingVolume() > 0){
                attemptTransaction(strategy);
            }
            List<Position> positions = new ArrayList<Position>(strategy.getPositions());
            for (int index=0; index<positions.size();index++ ) {
                Position p = positions.get(index);
                if (!p.isClose())
                    attemptClosePosition(strategy.getStock(),p,index,strategy.getPercentLoss(),strategy.getPercentProfit());
            }
        }
    }

    public void attemptTransaction(Strategy strategy) {
        if(strategy instanceof TwoMovingAveragesStrategy){
            attemptTwoMovingAveragesTransaction((TwoMovingAveragesStrategy) strategy);
        }
    }

    private void attemptTwoMovingAveragesTransaction(TwoMovingAveragesStrategy strategy) {
        try {
            String tickerSymbol = strategy.getStock();
            Quote quote = marketDataManager.getSpotPrice(tickerSymbol);
            double longAvg = marketDataManager.getLongAverage(tickerSymbol, strategy.getLongPeriod());
            double shortAvg = marketDataManager.getShortAverage(tickerSymbol, strategy.getShortPeriod());
            double bidPrice = quote.getBid();
            double askPrice = quote.getAsk();
            int bidSize = quote.getBidSize();
            int askSize = quote.getAskSize();

            if (strategy.isShortAvgLarger()==null)  {
                strategy.setShortAvgLarger(shortAvg > longAvg);
            }
            if (strategy.isShortAvgSmaller()==null) {
                strategy.setShortAvgSmaller(shortAvg < longAvg);
            }

            if ((shortAvg > longAvg && strategy.isShortAvgSmaller())
                    || (shortAvg < longAvg && strategy.isShortAvgLarger())) {
                boolean isGoLong = (shortAvg > longAvg && strategy
                        .isShortAvgSmaller()) ? true : false;
                System.out.println(isGoLong ? "Time to go long."
                        : "Time to go short.");
                boolean isBuyOrder = isGoLong; // When opening position go long -> buy; go short -> sell
                double strikePrice = isGoLong ? askPrice : bidPrice;
                if (askSize==0)
                    askSize=strategy.getRemaingVolume();
                if (bidSize==0)
                    bidSize=strategy.getRemaingVolume();
                int offeredVolume = isGoLong ? askSize : bidSize;
                int usedVolume = offeredVolume > strategy.getRemaingVolume() ? strategy.getRemaingVolume() : offeredVolume;

                // TODO SEND TO BROKER: usedVolume
                //tradeTransactionOpen(strategyID, volume, strikeprice, isGoLong)
                //if successful, Position position = new Position(isGoLong, usedVolume, strikePrice, strategy.getId());
                Position position = new Position(isGoLong, usedVolume, strikePrice, strategy.getId());
                submitOrderToBroker(tickerSymbol, isBuyOrder, usedVolume, strikePrice, position);

                Strategy originalCopy = strategies.get(strategy.getId());
                if(originalCopy!=null){
                    originalCopy.setRemaingVolume(strategy.getRemaingVolume() - usedVolume);
                }
            }
        } catch (NoLongAverageAvailableException e) {
            e.printStackTrace();
        } catch (NoShortAverageAvailableException e) {
            e.printStackTrace();
        }
    }

    private void submitOrderToBroker(String tickerSymbol, boolean isBuyOrder, int size, double price, Position position){

        Trade tradeSubmitted = truffleOrderManager.submitTrade(tickerSymbol, isBuyOrder, size, price, new TruffleOrderConfirmationListener() {
            //in the close or open Trade Transaction;
            //actually open position, or set a positon to close, record to database
            @Override
            public void onOrderConfirmed(Trade trade) {
                double strikePrice = trade.getPrice();
                Position position = tradePositionMap.remove(trade.getId());
                if(!position.isOpen() && !position.isClose()){
                    Strategy relatedStrategy = strategies.get(position.getStrategyID());
                    ActionHistory history = truffleDataManager.insertActionHistory(relatedStrategy.getId(), position.isGoLong(), strikePrice);
                    position.openPosition(trade.getSize(), strikePrice, history.getHistId());
                    relatedStrategy.getPositions().add(position);
                } else if (position.isOpen() && !position.isClose()){
                    // TODO close position & update database & update strategy
                    // todO if remainingVolume=0 and all postions is close then close the strategy
                }
            }

            @Override
            public void onOrderFailed(Trade trade) {
                // trade should be the submitted trade
                Position position = tradePositionMap.remove(trade.getId());
                Strategy strategy = strategies.get(position.getStrategyID());
                int remainingVolume = strategy.getRemaingVolume() + trade.getSize();
                strategy.setRemaingVolume(remainingVolume);
            }
        });
        tradePositionMap.put(tradeSubmitted.getId(), position);
    }

    public void attemptClosePosition(String ticker, Position position,int index , double percentLoss, double percentProfit) {
        //run this method for position with remainingVolume>0
        double percentChange = 0;
        Quote quote = marketDataManager.getSpotPrice(ticker);
        double bidPrice = quote.getBid();
        double askPrice = quote.getAsk();

        double usedCurrentPrice = position.isGoLong() ? bidPrice : askPrice;
        position.setPerformance(position.getStrikePrice() - usedCurrentPrice);
        percentChange = (position.getPerformance() / position.getStrikePrice()) * 100;

		if (percentChange <= percentLoss * (-1)
				|| percentChange >= (percentProfit)) {

            //TODO Communicate with broker, Volume and usedCurrentPrice
            //tradeTransactionClose(strategyID, positionID, volume, price)

            //temporarily update the original version
            // TODO retrieve it by comparing id
            Position originalCopy = strategies.get(position.getStrategyID()).getPositions().get(index);
            originalCopy.setRemainingVolume(0);
		}
    }

    public boolean deleteStrategy(Strategy strategy){
        if (strategy.getPositions().isEmpty()) {
            // TODO update database; delete strategy
            strategies.remove(strategy.getId());
            return true;
        } else {
            return false;
        }
    }

}
