package controllers;

import models.ExpressCheckout;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.Cookie;
import views.html.*;

public class Application extends Controller {

    /**
     * Show the cart page.
     */
    public static Result index() {
        return ok(index.render());
    }

    /**
     * Begin the checkout redirecting to the PayPal login page.
     */
    public static Promise<Result> checkout(Integer id) {

        double amount = getAmountById(id);

        String redirectUrl = routes.Application.pay(id, "").absoluteURL(
                request());
        String cancelUrl = routes.Application.index().absoluteURL(request());

        // Calling setExpressCheckout and redirect to the login page.
        return ExpressCheckout.set(redirectUrl, cancelUrl, amount)
                .map(new Function<String, Result>() {
                    public Result apply(String res) {
                        return redirect(res);
                    }
                }).recover(new Function<Throwable, Result>() {
                    public Result apply(Throwable e) {
                        return badRequest("ERROR");
                    }
                });
    }

    /**
     * Execute the payment redirected by the PayPal login and go back to the
     * cart page.
     */
    public static Promise<Result> pay(Integer id, String token) {

        double amount = getAmountById(id);

        // Execute the payment and redirect to the cart page.
        return ExpressCheckout.doPayment(token, amount)
                .map(new Function<String, Result>() {
                    public Result apply(String res) {
                        return redirect(routes.Application.index().absoluteURL(
                                request())
                                + "?pay=complete");
                    }
                }).recover(new Function<Throwable, Result>() {
                    public Result apply(Throwable e) {
                        return badRequest("ERROR");
                    }
                });
    }

    /**
     * Get each amount of items.
     */
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
