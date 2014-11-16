package models;

import java.util.StringTokenizer;

public class ApiResponse {

    private boolean success = false;

    private String token = null;

    private String payerId = null;

    public ApiResponse(String body) {
        StringTokenizer st = new StringTokenizer(body, "&");
        String[] kv = null;
        boolean success = false;
        String token = null;
        String payerId = null;
        while (st.hasMoreTokens()) {
            kv = st.nextToken().split("=");
            if (kv[0].equals("ACK") && kv[1].equals("Success"))
                success = true;
            if (kv[0].equals("TOKEN"))
                token = kv[1];
            if (kv[0].equals("PAYERID"))
                payerId = kv[1];
        }
        this.success = success;
        this.token = token;
        this.payerId = payerId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public String getPayerId() {
        return payerId;
    }

}