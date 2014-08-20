package controllers;

import OrderManagement.TruffleOrderManager;
import OrderManagement.datatransferobjects.Trade;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datamanagement.TruffleDataManager;
import org.apache.activemq.kaha.impl.DataManager;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;

import java.util.Date;
import java.util.Map;

public class Application extends Controller {

    private static TruffleOrderManager orderManager = new TruffleOrderManager();
    private static TruffleDataManager truffleDataManager= new TruffleDataManager();


    public static Result index() {
        return ok(index.render("Really? Your new application is ready."));
    }

    public static Result strategies() {

        return ok(strategies.render(""));
    }

    public static Result strategyView(long id) {

        return ok(strategy_view.render("{Strategy Title}"));
    }

    public static Result strategyCreate() {
        return ok(strategy_create.render("New Strategy"));
    }

    public static Result strategyCreatePost() {
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        String tickerSymbol = params.get("ticker")[0];
        Integer templateId = Integer.parseInt(params.get("templateId")[0]);
        Integer volume = Integer.parseInt(params.get("volume")[0]);
        Integer longPeriod = Integer.parseInt(params.get("longDur")[0]);
        Integer shortPeriod = Integer.parseInt(params.get("shortDur")[0]);
        Double profitPercent = Double.parseDouble(params.get("profitPer")[0]);
        Double lossPercent = Double.parseDouble(params.get("lossPer")[0]);

        try {
            truffleDataManager.insertStrategyNew(tickerSymbol, longPeriod, shortPeriod, volume, lossPercent, profitPercent, templateId);
            return strategies();
        } catch (Exception e) {
            System.err.println(e);
            return internalServerError();
        }
    }

    public static Result strategyModify(long id) {
        return ok(strategy_modify.render("Modify {Strategy Title}"));
    }

    public static Result strategyModifyPost(long id) {
        return play.mvc.Results.TODO;
    }

    public static Result strategyRemove(long id) {
        return ok("Remove " + id);
    }

    public static Result stockView(String tickerSymbol) {
        return ok(stock_view.render(tickerSymbol));
    }

    /* Test Methods */
    public static Result test() {
        return ok(test.render());
    }

    public static Result testGraph() {
        return ok(testHighStock.render());
    }

    public static Result submitBuyTrade(){
        return submitTrade(request(), true);
    }

    public static Result submitSellTrade(){
        return submitTrade(request(), false);
    }

    private static Result submitTrade(Http.Request request, boolean isBuyOrder){
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        String tickerSymbol = params.get("ticker")[0];
        System.out.println(String.format("TickerSymbol: %s", tickerSymbol));
        Integer size = Integer.parseInt(params.get("size")[0]);
        System.out.println(String.format("Size: %d",size));
        Double price = Double.parseDouble(params.get("price")[0]);
        System.out.println(String.format("Price: %f",price));

        int id = 0;
        Trade trade = orderManager.submitTrade( tickerSymbol, isBuyOrder, size, price, null);

        if(trade!=null){
            return ok(test.render());
        } else {
            return internalServerError();
        }
    }

    public static Result fetchPrice() {
        ObjectNode result = Json.newObject();
        int price = (int) (Math.random() * ( 1000 - 500 ));
        long time = new Date().getTime();
        result.put("timestamp", time);
        result.put("price", price);
        System.out.println(String.format("Timestamp: %d Price: %d", time, price));
        return ok(result);
    }
}
