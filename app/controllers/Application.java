package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.strategies;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Really? Your new application is ready."));
    }

    public static Result strategies() {
        return ok(strategies.render(""));
    }

}
