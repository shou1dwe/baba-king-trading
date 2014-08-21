import executionmanagement.ExecutionManager;
import executionmanagement.datatransferobjects.TwoMovingAveragesStrategy;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.datatransferobjects.Quote;
import marketdatamanagement.exceptions.NoLongAverageAvailableException;
import marketdatamanagement.exceptions.NoShortAverageAvailableException;
import org.junit.Before;
import org.junit.Test;
import executionmanagement.datatransferobjects.Strategy;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by user on 8/20/2014.
 */
public class ExecutionManagerTest {
    ExecutionManager executionManager;
    private static final double DELTA = 1e-15;


    @Before
    public void init() {
        //test1
        executionManager  = new ExecutionManager();

        //test2
        MarketDataManager mockedMarketDataManager = mock(MarketDataManager.class);
        Quote quote = new Quote("AAPL",94.7,1000,94.4,300,94.5, 10.0, new Date());
        when(mockedMarketDataManager.getSpotPrice("AAPL")).thenReturn(quote);
        try {
            when(mockedMarketDataManager.getLongAverage("AAPL", 10)).thenReturn(93.2);
            when(mockedMarketDataManager.getShortAverage("AAPL", 1)).thenReturn(94.2);
        } catch  (NoLongAverageAvailableException e) {
            e.getMessage();
        } catch (NoShortAverageAvailableException e) {
            e.getMessage();
        }
    }

    @Test
    public void testAddTwoMovingAveragesStrategy(){
        Strategy strategy1 = executionManager.addTwoMovingAveragesStrategy("AAPL",10,1,1000,1,2);
        assertEquals(((TwoMovingAveragesStrategy) strategy1).getLongPeriod(),10);
        assertEquals(((TwoMovingAveragesStrategy) strategy1).getShortPeriod(),1);
        assertEquals(strategy1.getVolume(),1000);
        assertEquals(strategy1.getPercentLoss(),1,DELTA);
        assertEquals(strategy1.getPercentProfit(),2,DELTA);
        assertEquals(executionManager.getStrategies().get(strategy1.getId()),strategy1);
    }

    @Test
    public void testAttempTwoMovingAveragesTransactionFirstTime(){
        Strategy strategy2 = new TwoMovingAveragesStrategy("1",10,1,400,"AAPL",0.5,1.5);
        executionManager.attemptTransaction(strategy2);
        assertEquals(((TwoMovingAveragesStrategy) strategy2).isShortAvgLarger(),true);
        assertEquals(((TwoMovingAveragesStrategy) strategy2).isShortAvgSmaller(),false);
    }
}
