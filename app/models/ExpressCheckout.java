package models;

import play.Logger;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

/**
 * PayPal Express Checkout API transaction.
 *
 */
public class ExpressCheckout {

    private static final String API_URL = play.Play
            .application()
            .configuration()
            .getString("paypal.api.url",
                    "https://api-3t.sandbox.paypal.com/nvp");

    private static final String API_VERSION = play.Play.application()
            .configuration().getString("paypal.api.version", "109.0");

    private static final String API_USER = System.getenv("PP_API_USER");

    private static final String API_PWD = System.getenv("PP_API_PWD");

    private static final String API_SIG = System.getenv("PP_API_SIG");

    private static final String LOGIN_URL = play.Play
            .application()
            .configuration()
            .getString("paypal.login.url",
                    "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=");

    private static final String BASE_QUERY = "USER=" + API_USER + "&PWD="
            + API_PWD + "&SIGNATURE=" + API_SIG + "&VERSION=" + API_VERSION;

    /**
     * Call SetExpressCheckout as async processing.
     */
    public static Promise<String> set(String returnUrl, String cancelUrl,
            double amount) {
        String query = BASE_QUERY + "&METHOD=" + "SetExpressCheckout"
                + "&RETURNURL=" + returnUrl + "&CANCELURL=" + cancelUrl
                + "&PAYMENTREQUEST_0_AMT=" + amount
                + "&PAYMENTREQUEST_0_PAYMENTACTION=" + "Sale";
        Logger.debug(query);
        return WS.url(API_URL).post(query)
                .map(new Function<WSResponse, String>() {
                    public String apply(WSResponse response) throws Throwable {
                        String body = response.getBody();
                        Logger.debug(body);
                        ApiResponse res = new ApiResponse(body);
                        if (res.isSuccess()) {
                            return LOGIN_URL + res.getToken();
                        } else {
                            throw new Throwable(body);
                        }
                    }
                });
    }

    /**
     * 
     * Call GetExpressCheckoutDetails and DoExpressCheckoutPayment as async
     * processing.
     */
    public static Promise<String> doPayment(String token, double amount) {
        String query = BASE_QUERY + "&METHOD=" + "GetExpressCheckoutDetails"
                + "&TOKEN=" + token;
        Logger.debug(query);
        final double amt = amount;
        return WS.url(API_URL).post(query)
                .flatMap(new Function<WSResponse, Promise<String>>() {
                    public Promise<String> apply(WSResponse response) {
                        final String body = response.getBody();
                        Logger.debug(body);
                        ApiResponse res = new ApiResponse(body);
                        if (res.isSuccess()) {
                            String query = BASE_QUERY + "&METHOD="
                                    + "DoExpressCheckoutPayment" + "&TOKEN="
                                    + res.getToken() + "&PAYERID="
                                    + res.getPayerId()
                                    + "&PAYMENTREQUEST_0_PAYMENTACTION="
                                    + "Sale" + "&PAYMENTREQUEST_0_AMT=" + amt;
                            Logger.debug(query);
                            return WS.url(API_URL).post(query)
                                    .map(new Function<WSResponse, String>() {
                                        public String apply(WSResponse response)
                                                throws Throwable {
                                            String body = response.getBody();
                                            Logger.debug(body);
                                            ApiResponse res = new ApiResponse(
                                                    body);
                                            if (res.isSuccess()) {
                                                return body;
                                            } else {
                                                throw new Throwable(body);
                                            }
                                        }
                                    });
                        } else {
                            Promise<String> promiseOfRes = Promise
                                    .promise(new Function0<String>() {
                                        public String apply() {
                                            return body;
                                        }
                                    });
                            return promiseOfRes
                                    .map(new Function<String, String>() {
                                        public String apply(String body)
                                                throws Throwable {
                                            throw new Throwable(body);
                                        }
                                    });
                        }
                    }
                });
    }

}