package datamanagement;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import executionmanagement.datatransferobjects.TwoMovingAveragesStrategy;
import models.ActionHistory;
import models.Stock;
import models.Strategy;
import models.Template;
import play.libs.Json;

import java.util.Date;

/**
 * Created by user on 8/18/2014.
 */
public class TruffleDataManager {

    // Stock Related
    public void insertStock(String name){
        Stock newStock = new Stock(name);
        Ebean.save(newStock);
    }

    public void deleteStock(String name){
        Stock.find.ref(name).delete();
    }

    // Strategy related
    public Strategy insertTwoMovingAveragesStrategy(TwoMovingAveragesStrategy strategy){
        final int templateId = 1; // TODO the static id of Two Moving Averages
        Template template = Template.find.byId(templateId);
        Stock stock = Stock.find.byId(strategy.getStock());
        String extraParams;
        switch(template.tempId){
            case 1: //Two Moving Averages Strategy
                ObjectNode result = Json.newObject();
                result.put("longPeriod", strategy.getLongPeriod());
                result.put("shortPeriod", strategy.getShortPeriod());
                extraParams = result.toString();
                break;
            default:
                extraParams = "{}";
        }
        Strategy newStrategy = new Strategy(strategy.getId(),
                                            stock,
                                            strategy.getVolume(),
                                            strategy.getVolume(),
                                            strategy.getPercentLoss(),
                                            strategy.getPercentProfit(),
                                            extraParams,
                                            template);
        Ebean.save(newStrategy);
        return newStrategy;
    }

    public void deleteStrategy(String id){
        Strategy.find.ref(id).delete();
    }

    // Template Related
    public void insertTemplate(int id, String name){
        Template newTemp = new Template(id, name);
        Ebean.save(newTemp);
    }

    public void deleteTemplate(int id){
        Template.find.ref(id).delete();
    }

    public Template getTemplate(int id){
        return Template.find.byId(id);
    }

    // ActionHistory Related
    public ActionHistory insertActionHistory(String strategyId, boolean isLong, double openPositionPrice) {
        double performance = 0.0;
        boolean isClose = false;
        double closePrice = -1.0;
        Date now = new Date();
        Strategy strategy = Strategy.find.byId(strategyId);
        ActionHistory history = new ActionHistory(strategy, performance, isLong, isClose, openPositionPrice, closePrice, now);
        Ebean.save(history);

        return history;
    }

    public void deleteTransaction(int id){
        ActionHistory.find.ref(id).delete();
    }

}
