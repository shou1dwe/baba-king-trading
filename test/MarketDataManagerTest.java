import com.google.common.collect.Lists;
import executionmanagement.ExecutionManager;
import executionmanagement.datatransferobjects.Strategy;
import executionmanagement.datatransferobjects.TwoMovingAveragesStrategy;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.exceptions.NoLongAverageAvailableException;
import marketdatamanagement.exceptions.NoShortAverageAvailableException;
import org.junit.Before;
import org.junit.Test;
import play.twirl.api.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class MarketDataManagerTest {
//    private static final double DELTA = 1e-15;
//    MarketDataManager marketDataManager;
//
//    @Before
//    public void init(){
//        marketDataManager = new MarketDataManager();

//        Map<String, List<Double>> historicPrices = new HashMap<>();
//        List<Double > listOfPrices = new ArrayList<>();
//        listOfPrices.add(25.6);
//        listOfPrices.add(25.7);
//        listOfPrices.add(25.8);
//        listOfPrices.add(25.9);
//        listOfPrices.add(25.1);
//        listOfPrices.add(25.2);
//        listOfPrices.add(25.3);
//        historicPrices.put("AAPL", listOfPrices);
//        marketDataManager.setHistoricPrices(historicPrices);
//    }

//    @Test
//    public void something(){
//        try {
//            ExecutionManager executionManager = new ExecutionManager();
//            MarketDataManager marketDataManager = new MarketDataManager();
//
//            Strategy strategy = executionManager.addTwoMovingAveragesStrategy("AAPL",10,1,200,1,2);
//            marketDataManager.subscribe("AAPL");
//            marketDataManager.subscribe("IBM");
//
//            System.out.println(marketDataManager.getSpotPriceUrl());
//
//            System.out.println(marketDataManager.isStockSubscribed("AAPL"));
//            System.out.println(marketDataManager.isStockSubscribed("GOOG"));
//
//            //System.out.println(marketDataManager.getShortAverage("IBM",1));
//
//            try {
//                System.out.println(marketDataManager.getLongAverage("AAPL",10));
//            } catch (NoLongAverageAvailableException e) {
//                e.printStackTrace();
//            }
//            executionManager.activateStrategy(strategy);
//            executionManager.deactivateStrategy(strategy);
//            executionManager.modifyTwoMovingAveragesStrategy(strategy,9,2,1,3);
//            System.out.printf("%d, %d, ",strategy.getVolume(),strategy.getRemaingVolume());
//            System.out.println(strategy.getPositions().isEmpty());
//            System.out.println(executionManager.deleteStrategy(strategy));
//            System.out.println(executionManager.getStrategies().isEmpty());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @Test
//    public void testGetShortAvg() throws NoShortAverageAvailableException {
//        double result = marketDataManager.getShortAverage("AAPL",1 );
//        assertEquals(result,(25.1+25.2+25.3+25.7+25.8+25.9)/6,DELTA);
//    }
//
//    @Test (expected=NoShortAverageAvailableException.class)
//    public void testGetShortAverageWhenNoShortDataAvailable() throws NoShortAverageAvailableException {
//        marketDataManager.getShortAverage("AAPL", 3);
//    }

}
