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
    //    private TruffleDataManager truffleDataManager;
//    private TruffleOrderManager truffleOrderManager;
//    private Map<String, Strategy> strategies;
//    private Map<Integer, Position> tradePositionMap;
//    private MarketDataManager marketDataManager;
    private TruffleDataManager truffleDataManager;
    private TruffleOrderManager truffleOrderManager;
    private Map<String, Strategy> strategies = new HashMap<>();
    private Map<Integer, Position> tradePositionMap = new HashMap<>();
    private MarketDataManager marketDataManager = new MarketDataManager();

    public ExecutionManager() {
    }

    public ExecutionManager(MarketDataManager marketDataManager, TruffleOrderManager truffleOrderManager, TruffleDataManager truffleDataManager) {
        this.truffleOrderManager = truffleOrderManager;
        this.marketDataManager = marketDataManager;
        this.truffleDataManager = truffleDataManager;
        tradePositionMap = new HashMap<>();
        strategies = new HashMap<String, Strategy>();
    }

    public Strategy addTwoMovingAveragesStrategy(String ticker, int longPeriod, int shortPeriod, int volume,
                                                 double percentLoss, double percentProfit) {
        String id = String.format("Truffle-%s-%d", ticker, System.currentTimeMillis() / 1000L);
        System.out.println(id);
        if (!marketDataManager.isStockSubscribed(ticker))
            marketDataManager.subscribe(ticker);
        TwoMovingAveragesStrategy newStrategy = new TwoMovingAveragesStrategy(id, longPeriod, shortPeriod, volume, ticker, percentLoss, percentProfit);
        //truffleDataManager.insertTwoMovingAveragesStrategy(newStrategy);
        strategies.put(id, newStrategy);
        System.out.println("Strategy added.");
        return newStrategy;
    }

    public void startExecution() {
        Collection<Strategy> values = new ArrayList<Strategy>(strategies.values());
        for (Strategy strategy : values) {
            if (strategy.getRemaingVolume() > 0) {
                attemptTransaction(strategy);
            }
            List<Position> positions = new ArrayList<Position>(strategy.getPositions());
            for (int index = 0; index < positions.size(); index++) {
                Position p = positions.get(index);
                if (!p.isClose())
                    attemptClosePosition(strategy.getStock(), p, positions, strategy.getPercentLoss(), strategy.getPercentProfit());
            }
        }
    }

    public void attemptTransaction(Strategy strategy) {
        if (strategy instanceof TwoMovingAveragesStrategy) {
            attemptTwoMovingAveragesTransaction((TwoMovingAveragesStrategy) strategy);
        }
    }

    //NOTE only for open strategy
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

            if (strategy.isShortAvgLarger() == null) {
                strategy.setShortAvgLarger(shortAvg > longAvg);
            }
            if (strategy.isShortAvgSmaller() == null) {
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
                if (askSize == 0)
                    askSize = strategy.getRemaingVolume();
                if (bidSize == 0)
                    bidSize = strategy.getRemaingVolume();
                int offeredVolume = isGoLong ? askSize : bidSize;
                int usedVolume = offeredVolume > strategy.getRemaingVolume() ? strategy.getRemaingVolume() : offeredVolume;

                Position position = new Position(isGoLong, usedVolume, strikePrice, strategy.getId());
                submitOrderToBroker(tickerSymbol, isBuyOrder, usedVolume, strikePrice, position);

                Strategy originalCopy = strategies.get(strategy.getId());
                if (originalCopy != null) {
                    originalCopy.setRemaingVolume(strategy.getRemaingVolume() - usedVolume);
                }
            }
        } catch (NoLongAverageAvailableException e) {
            e.printStackTrace();
        } catch (NoShortAverageAvailableException e) {
            e.printStackTrace();
        }
    }

    private void submitOrderToBroker(String tickerSymbol, boolean isBuyOrder, int size, double price, Position position) {

        Trade tradeSubmitted = truffleOrderManager.submitTrade(tickerSymbol, isBuyOrder, size, price, new TruffleOrderConfirmationListener() {
            //in the close or open Trade Transaction;
            //actually open position, or set a positon to close, record to database
            @Override
            public void onOrderConfirmed(Trade trade) {
                double strikePrice = trade.getPrice();
                Position position = tradePositionMap.remove(trade.getId());
                if (!position.isOpen() && !position.isClose()) {
                    Strategy relatedStrategy = strategies.get(position.getStrategyID());
                    ActionHistory history = truffleDataManager.insertActionHistory(relatedStrategy.getId(), position.isGoLong(), strikePrice);
                    position.openPosition(trade.getSize(), strikePrice, history.getHistId());
                    relatedStrategy.getPositions().add(position);
                } else if (position.isOpen() && !position.isClose()) {
                    Strategy relatedStrategy = strategies.get(position.getStrategyID());
                    for (Position p : relatedStrategy.getPositions()) {
                        if (p.getId() == position.getId()) {
                            p.closePosition();
                        }
                    }
                    // TODO update database for closing Position (done - XW check again)
                    truffleDataManager.closeActionHistory(position.getId());

                    // TODO update database for closing Strategy (done - XW check again)
                    boolean allPositionsClosed = true;
                    for (Position p : relatedStrategy.getPositions()) {
                        if (!p.isClose()) {
                            allPositionsClosed = false;
                            break;
                        }
                    }
                    if (relatedStrategy.getRemaingVolume() == 0 && allPositionsClosed) {
                        relatedStrategy.setIsClose(true);
                        truffleDataManager.closeStrategy(relatedStrategy.getId());
                    }
                }
            }

            @Override
            public void onOrderFailed(Trade trade) {
                // trade should be the submitted trade
                Position position = tradePositionMap.remove(trade.getId());
                if (!position.isOpen() && !position.isClose()) { //revert strategy's remainingVolume
                    Strategy strategy = strategies.get(position.getStrategyID());
                    int remainingVolume = strategy.getRemaingVolume() + trade.getSize();
                    strategy.setRemaingVolume(remainingVolume);
                } else if (position.isOpen() && !position.isClose()) { //revert position's remainingVolume
                    Strategy relatedStrategy = strategies.get(position.getStrategyID());
                    for (Position p : relatedStrategy.getPositions()) {
                        if (p.getId() == position.getId()) {
                            p.setRemainingVolume(p.getUsedVolume());
                        }
                    }
                }
            }
        });
        tradePositionMap.put(tradeSubmitted.getId(), position);
    }

    public void attemptClosePosition(String ticker, Position p, List<Position> positions, double percentLoss, double percentProfit) {
        //run this method for position with remainingVolume>0
        double percentChange = 0;
        Quote quote = marketDataManager.getSpotPrice(ticker);
        double bidPrice = quote.getBid();
        double askPrice = quote.getAsk();

        double usedCurrentPrice = p.isGoLong() ? bidPrice : askPrice;
        p.setPerformance(p.getStrikePrice() - usedCurrentPrice);
        percentChange = (p.getPerformance() / p.getStrikePrice()) * 100;

        boolean isBuyOrder = p.isGoLong() ? false : true;

        if (percentChange <= percentLoss * (-1)
                || percentChange >= (percentProfit)) {

            //TODO Communicate with broker - XW check again
            submitOrderToBroker(ticker, isBuyOrder, p.getRemainingVolume(), usedCurrentPrice, p);

            //temporarily update the original version
            for (Position i : positions) {
                if (i.getId() == p.getId()) {
                    i.setRemainingVolume(0);
                }
            }
        }
    }

    public boolean deleteStrategy(String id) {
        Strategy strategy = strategies.get(id);
        if (strategy.getPositions().isEmpty() && strategy.getRemaingVolume() == strategy.getVolume()) {
            // TODO update database; delete strategy (done - XW check again)
            strategies.remove(strategy.getId());
            truffleDataManager.deleteStrategy(strategy.getId());
            return true;
        } else {
            return false;
        }
    }

    public boolean activateStrategy(String id) {
        Strategy strategy = strategies.get(id);
        if (strategy.getIsClose() == null) {
            //TODO update database - done XW check again
            strategy.setIsClose(false);
            truffleDataManager.activateStrategy(strategy.getId());
            return true;
        } else {
            return false;
        }
    }

    public boolean deactivateStrategy(String id) {
        Strategy strategy = strategies.get(id);
        if (strategy.getIsClose() != null) {
            System.out.println("Im here");
            //TODO update database - done XW check again
            strategy.setIsClose(null);
            truffleDataManager.deactivateStrategy(strategy.getId());
            return true;
        } else {
            return false;
        }
    }

    public boolean modifyTwoMovingAveragesStrategy(String id, int longPeriod, int shortPeriod,
                                                   double percentLoss, double percentProfit) {
        //can only modify strategy without positions.
        Strategy strategy = strategies.get(id);
        if (strategy.getPositions().isEmpty() && strategy.getRemaingVolume() == strategy.getVolume()) {
            if (strategy instanceof TwoMovingAveragesStrategy) {
                ((TwoMovingAveragesStrategy) strategy).setLongPeriod(longPeriod);
                ((TwoMovingAveragesStrategy) strategy).setShortPeriod(shortPeriod);
                strategy.setPercentLoss(percentLoss);
                strategy.setPercentProfit(percentProfit);
                //todo update in database
                truffleDataManager.modifyTwoMovingAveragesStrategy(strategy.getId(), longPeriod, shortPeriod, percentLoss, percentProfit);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    //PERFORMANCE MATTERS
    public double getPerformanceByStrategy(String id) {
        double sum = 0;
        Collection<Strategy> values = new ArrayList<>(strategies.values());
        for (Strategy strategy : values) {
            if (strategy.getId().equalsIgnoreCase(id) && strategy.getIsClose() != null) {
                for (Position p : strategy.getPositions()) {
                    if (p.isClose()) {
                        sum += p.getPerformance() * p.getUsedVolume();
                    }
                }
            }
        }
        return sum;
    }

    public double getPerformanceByTicker(String ticker) {
        double sum = 0;
        Collection<Strategy> values = new ArrayList<>(strategies.values());
        for (Strategy strategy : values) {
            if (strategy.getStock().equalsIgnoreCase(ticker) && strategy.getIsClose() != null) {
                for (Position p : strategy.getPositions()) {
                    if (p.isClose()) {
                        sum += p.getPerformance() * p.getUsedVolume();
                    }
                }
            }
        }
        return sum;
    }


    //getters and setters
    public Map<String, Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(Map<String, Strategy> strategies) {
        this.strategies = strategies;
    }

    public Map<Integer, Position> getTradePositionMap() {
        return tradePositionMap;
    }

    public void setTradePositionMap(Map<Integer, Position> tradePositionMap) {
        this.tradePositionMap = tradePositionMap;
    }

    public MarketDataManager getMarketDataManager() {
        return marketDataManager;
    }

    public void setMarketDataManager(MarketDataManager marketDataManager) {
        this.marketDataManager = marketDataManager;
    }
}