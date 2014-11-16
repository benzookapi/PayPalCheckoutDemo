package controllers;

import models.ExpressCheckout;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.Cookie;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    public static Promise<Result> checkout(Integer id) {

        double amount = getAmountById(id);

        String redirectUrl = routes.Application.pay(id, "").absoluteURL(
                request());
        String cancelUrl = routes.Application.index().absoluteURL(request());

        return ExpressCheckout.set(redirectUrl, cancelUrl, amount)
                .map(new Function<String, Result>() {
                    public Result apply(String res) {
                        return redirect(res);
                    }
                }).recover(new Function<Throwable, Result>() {
                    public Result apply(Throwable e) {
                        return ok(e.getMessage());
                    }
                });
    }

    public static Promise<Result> pay(Integer id, String token) {

        double amount = getAmountById(id);

        final int idInt = id;

        return ExpressCheckout.doPayment(token, amount)
                .map(new Function<String, Result>() {
                    public Result apply(String res) {
                        return redirect(routes.Application.index().absoluteURL(
                                request())
                                + "?pay=complete");
                    }
                }).recover(new Function<Throwable, Result>() {
                    public Result apply(Throwable e) {
                        return ok(e.getMessage());
                    }
                });
    }

    private static double getAmountById(int id) {
        switch (id) {
        case 1:
            return 120.00;
        case 2:
            return 31.00;

        case 3:
            return 8.00;

        default:
            return 0;
        }
    }

}
