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
import java.util.List;

/**
 * Created by user on 8/18/2014.
 */
public class TruffleDataManager {

    // Stock Related
    public void insertStock(String ticker, String companyName, String moreInfo, String notes, String exchange){
        Stock newStock = new Stock(ticker, companyName, moreInfo, notes, exchange);
        Ebean.save(newStock);
    }

    public Stock getStockByTicker(String symbol){
        Stock stock = Stock.find.byId(symbol);
        if(stock != null) {
            List<Strategy> strategies = stock.strategies;
        }
        return stock;
    }

    public void deleteStock(String name){
        Stock.find.ref(name).delete();
    }

    // Strategy Related
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

    public Strategy getStrategyById(String id) {
        Strategy strategy = Strategy.find.byId(id);
        return strategy;
    }

    public List<Strategy> getStrategyAll() {
        return Strategy.find.all();
    }

    public void deleteStrategy(String id){
        Strategy strategy = Strategy.find.ref(id);
        strategy.isDeleted=true;
        Ebean.update(strategy);
    }

    public Strategy activateStrategy(String id) {
        Strategy strategy = Strategy.find.ref(id);
        strategy.isClose=false;
        Ebean.update(strategy);
        return strategy;
    }


    public Strategy deactivateStrategy(String id) {
        Strategy strategy = Strategy.find.ref(id);
        strategy.isClose=null;
        Ebean.update(strategy);
        return strategy;
    }

    public Strategy closeStrategy(String id) {
        Strategy strategy = Strategy.find.ref(id);
        strategy.isClose=true;
        Ebean.update(strategy);
        return strategy;
    }
    public Strategy modifyTwoMovingAveragesStrategy(String id, int longPeriod, int shortPeriod, double percentLoss, double percentProfit) {
        Strategy strategy = Strategy.find.ref(id);
        ObjectNode result = Json.newObject();
        result.put("longPeriod", longPeriod);
        result.put("shortPeriod", shortPeriod);
        strategy.extras= result.toString();
        strategy.lossPercent=percentLoss;
        strategy.profitPercent=percentProfit;
        Ebean.update(strategy);
        return strategy;
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

    public ActionHistory closeActionHistory(int hisId){
        ActionHistory history=ActionHistory.find.ref(hisId);
        history.isClose=true;
        Ebean.update(history);
        return history;
    }

    public void deleteTransaction(int id){
        ActionHistory.find.ref(id).delete();
    }


}
