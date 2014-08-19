package datamanagement;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ActionHistory;
import models.Stock;
import models.Strategy;
import models.Template;
import play.libs.Json;

/**
 * Created by user on 8/18/2014.
 */
public class TruffleDataManager {

    public static void insertStock(String name){
        Stock newStock = new Stock(name);
        Ebean.save(newStock);
    }

    public static void deleteStock(String name){
        Stock.find.ref(name).delete();
    }

    public static void deleteTransaction(int id){
        ActionHistory.find.ref(id).delete();
    }

    public static void insertStrategy(String stockId, int longDur, int shortDur, int vol, int remainingVol, double lossPercent, double profitPercent, int templateId){
        Template template = Template.find.byId(templateId);
        Stock stock = Stock.find.byId(stockId);
//        longDur, shortDur,
        String extraParams; // TODO
        switch(template.tempId){
            case 1: //Two Moving Averages Strategy
                ObjectNode result = Json.newObject();
                result.put("longPeriod", longDur);
                result.put("shortPeriod", shortDur);
                extraParams = result.toString();
                break;
            default:
                extraParams = "{}";
        }
        Strategy newStrategy = new Strategy(stock, vol, remainingVol, lossPercent, profitPercent, extraParams, template);
        Ebean.save(newStrategy);
    }

    public static void deleteStrategy(int id){
        Strategy.find.ref(id).delete();
    }

    public static void insertTemplate(int id, String name){
        Template newTemp = new Template(id, name);
        Ebean.save(newTemp);
    }

    public static void deleteTemplate(int id){
        Template.find.ref(id).delete();
    }

    public static Template getTemplate(int id){
        return Template.find.byId(id);
    }

}
