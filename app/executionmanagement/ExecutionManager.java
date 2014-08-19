package executionmanagement;

import executionmanagement.datatransferobjects.Position;
import executionmanagement.datatransferobjects.Strategy;
import executionmanagement.datatransferobjects.TwoMovingAveragesStrategy;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.datatransferobjects.Quote;

import java.util.*;

/**
 * Author: Xiawei
 */
public class ExecutionManager {
    private Map<String, Strategy> strategies;
    private MarketDataManager marketDataManager;

    public ExecutionManager(MarketDataManager marketDataManager) {
        this.marketDataManager = marketDataManager;
        strategies = new HashMap<String, Strategy>();
    }

    public void addTwoMovingAveragesStrategy(String ticker, int longPeriod, int shortPeriod, int volume,
                                                    double percentLoss, double percentProfit) {
        String id = String.format("Truffle-%s-%d", ticker, System.currentTimeMillis()/1000L);
        if (!marketDataManager.isStockSubscribed(ticker))
            marketDataManager.subscribe(ticker);

        // calculate longAverage, shortAverage
        double longAvg = 94.5; // hard code --> short is larger than long
        double shortAvg = 94.6; // hard code

        // create and add
        boolean isShortAvgSmaller = shortAvg < longAvg;
        boolean isShortAvgLarger = shortAvg > longAvg;

        Strategy newStrategy = new TwoMovingAveragesStrategy(id, longPeriod, shortPeriod, volume, ticker, percentLoss,
                percentProfit, isShortAvgSmaller, isShortAvgLarger);

        strategies.put(id, newStrategy);
        System.out.println("Strategy added.");
    }

    public void startExecution() {
        Collection<Strategy> values = new ArrayList<Strategy>(strategies.values());
        for (Strategy strategy : values) {
            if (strategy.getRemaingVolume() > 0){
                attemptTransaction(strategy);
            }
            for (Position t : strategy.getPositions()) {
                if (!t.isClose())
                    attemptClosePosition(t);
            }
        }
    }

    public Position attemptTransaction(Strategy strategy) {
        if(strategy instanceof TwoMovingAveragesStrategy){
            return attemptTwoMovingAveragesTransaction((TwoMovingAveragesStrategy) strategy);
        }

        return null;
    }

    private Position attemptTwoMovingAveragesTransaction(TwoMovingAveragesStrategy strategy) {
        // TODO Yahoo feed data
        Quote quote = marketDataManager.getSpotPrice(strategy.getStock());
        double longAvg = marketDataManager.getLongAverage(strategy.getStock(), strategy.getLongPeriod());
        double shortAvg = marketDataManager.getShortAverage(strategy.getStock(), strategy.getShortPeriod());
        double bidPrice = quote.getBid();
        double askPrice = quote.getAsk();
        int bidSize = quote.getBidSize();
        int askSize = quote.getAskSize();

        if ((shortAvg > longAvg && strategy.isShortAvgSmaller())
                || (shortAvg < longAvg && strategy.isShortAvgLarger())) {
            boolean isGoLong = (shortAvg > longAvg && strategy
                    .isShortAvgSmaller()) ? true : false;
            System.out.println(isGoLong ? "Time to go long."
                    : "Time to go short.");

            double strikePrice = isGoLong ? askPrice : bidPrice;
            int offeredVolume = isGoLong ? askSize : bidSize;
            int usedVolume = offeredVolume > strategy.getVolume() ? strategy.getVolume() : offeredVolume;

            // ***SEND TO BROKER: usedVolume
            Strategy originalCopy = strategies.get(strategy.getId());
            if(originalCopy!=null){
                originalCopy.setRemaingVolume(strategy.getVolume() - usedVolume);
            }
//			Position position = new Position(isGoLong, usedVolume, strikePrice, strategy.getId());

//			return position;
        }
        return null;

    }

    public void openPosition(Position position) {
        // only operate to those with conditonMet
        System.out.println("Position is triggered.Start entry");

    }

    public void attemptClosePosition(Position position) {
        double percentChange = 0;
        // pull Feeds
        double bidPrice = 100.4; // hard code
        double askPrice = 100.6;// hard code
        int bidSize = 800;// hard code
        int askSize = 800;// hard code

        double usedCurrentPrice = position.isGoLong() ? bidPrice : askPrice;
        position.setPerformace(position.getStrikePrice() - usedCurrentPrice);

        percentChange = (position.getPerformace() / position.getStrikePrice()) * 100;

//		if (percentChange <= percentLoss * (-1))
//				|| percentChange >= (strategy.getPercentProfit())) {
//			closePosition(p);
//		}
    }

    public void closePosition(Strategy strategy) {
        // Record into database

    }
}
