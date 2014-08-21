package controllers;

import OrderManagement.TruffleOrderManager;
import OrderManagement.datatransferobjects.Trade;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datamanagement.TruffleDataManager;
import executionmanagement.AdhocExecutionManager;
import executionmanagement.ExecutionManager;
import marketdatamanagement.MarketDataManager;
import marketdatamanagement.datatransferobjects.Quote;
import models.Stock;
import models.Strategy;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application extends Controller {

    private static TruffleOrderManager orderManager = new TruffleOrderManager();
    private static MarketDataManager marketDataManager = new MarketDataManager();
    private static TruffleDataManager truffleDataManager = new TruffleDataManager();
    private static ExecutionManager executionManager = new ExecutionManager(marketDataManager, orderManager, truffleDataManager);
    private static AdhocExecutionManager adhocExecutionManager = new AdhocExecutionManager(truffleDataManager, marketDataManager);

    public static Result index() {
        return ok(index.render("Really? Your new application is ready."));
    }

    public static Result strategies() {
        List<Strategy> strategiesActive = new ArrayList<>();
        List<Strategy> strategiesClosed = new ArrayList<>();
        for(Strategy strategy : truffleDataManager.getStrategyAll()){
            if(strategy.isClose == null || !strategy.isClose){
                strategiesActive.add(strategy);
            } else {
                strategiesClosed.add(strategy);
            }
        }
        return ok(strategies.render(strategiesActive, strategiesClosed));
    }

    public static Result strategyView(String id) {
        Strategy strategy = truffleDataManager.getStrategyById(id);
        Stock stock = strategy.stock;
        Quote quote = adhocExecutionManager.getLatestQuote(stock.ticker);

        return play.mvc.Results.TODO;
    }

    public static Result getActionHistory() {
        return ok(action_history.render(""));
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
            switch(templateId){
                case 1:
                    executionManager.addTwoMovingAveragesStrategy(tickerSymbol, longPeriod, shortPeriod, volume, lossPercent, profitPercent);
                    break;
                default:
                    break;
            }
            return strategies();
        } catch (Exception e) {
            Logger.error("Strategy Creation failed: ", e);
            return internalServerError();
        }
    }

    public static Result strategyActivate(String id){
        boolean result = executionManager.activateStrategy(id);
        if (result){
            return redirect(request().uri());
        } else {
            return internalServerError();
        }
    }

    public static Result strategyModify(String id) {
        truffleDataManager.getStrategyById(id);
        return ok(strategy_modify.render("Modify {Strategy Title}"));
    }

    public static Result strategyModifyPost(String id) {
        // TODO
        return play.mvc.Results.TODO;
    }

    public static Result strategyRemove(String id) {
        truffleDataManager.deleteStrategy(id);
        return ok("Remove " + id);
    }

    public static Result stockView(String tickerSymbol) {
        Stock stock = adhocExecutionManager.retriveStockInformationByTicker(tickerSymbol);
        if (stock == null){
            Logger.warn("Cannot find stock information on {}", tickerSymbol);
        } else {
            List<Strategy> strategiesActive = new ArrayList<>();
            List<Strategy> strategiesClosed = new ArrayList<>();
            for(Strategy strategy : stock.strategies){
                if(strategy.isClose == null || !strategy.isClose){
                    strategiesActive.add(strategy);
                } else {
                    strategiesClosed.add(strategy);
                }
            }
            Quote quote = adhocExecutionManager.getLatestQuote(tickerSymbol);
            //TODO link up webpage
            return ok(stock_view.render(stock, strategiesActive, strategiesClosed, quote));
        }
        return Results.TODO;
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

        Trade trade = orderManager.submitTrade(tickerSymbol, isBuyOrder, size, price, null);

        if(trade!=null){
            return ok(test.render());
        } else {
            return internalServerError();
        }
    }

    // Web Services
    public static Result fetchPrice(String symbol) {
        ObjectNode result = Json.newObject();
        Quote quote = adhocExecutionManager.getLatestQuote(symbol);
        if (quote != null) {
            result.put("timestamp", quote.getTime().getTime());
            result.put("price", quote.getLastTradePrice());
            return ok(result);
        } else {
            return internalServerError();
        }
    }
}
