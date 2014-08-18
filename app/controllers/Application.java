package controllers;

import OrderManagement.TruffleOrderManager;
import OrderManagement.datatransferobjects.Trade;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;
import java.util.Map;

public class Application extends Controller {

    private static TruffleOrderManager orderManager = new TruffleOrderManager();

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

    public static Result strategyModify(long id) {
        return ok(strategy_modify.render("Modify {Strategy Title}"));
    }

    public static Result strategyRemove(long id) {
        return ok("Remove " + id);
    }

    public static Result stockView(String tickerSymbol) {
        return ok(stock_view.render(tickerSymbol));
    }

    public static Result test() {
        return ok(test.render());
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
}
