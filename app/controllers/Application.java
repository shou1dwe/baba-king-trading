package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

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
}
