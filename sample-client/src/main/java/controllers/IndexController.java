package sample.controllers;

import java.util.*;
import java.net.*;
import java.io.*;
import spark.*;
import sample.*;
import static sample.SessionUtil.*;

/**
* Contains route handlers for the sample application.
* @author The Intern
*/
public class IndexController {
    /** Serve the index page (GET request) */
    public static Route serveIndexPage = (Request request, Response response) -> {
        // redirect the user to Wondough's authentication page if we don't
        // have an access token stored in the session
        if(getSessionCurrentUser(request) == null) {
            response.redirect("http://localhost:8000/auth?app=1&target=http://localhost:8080/oauth");
        }

        Map<String, Object> model = new HashMap<>();
        return ViewUtil.render(request, model, "/velocity/index.vm");
    };

    /** Serve the transaction page (GET request) */
    public static Route serveAddTransactionPage = (Request request, Response response) -> {
        // redirect the user to Wondough's authentication page if we don't
        // have an access token stored in the session
        if(getSessionCurrentUser(request) == null) {
            response.redirect("http://localhost:8000/auth?app=1&target=http://localhost:8080/oauth");
        }

        Map<String, Object> model = new HashMap<>();
        String token = getCSRFtoken(getSessionCurrentUser(request));
        token = token.substring(1,token.length()-1);
        System.out.println(token);
        model.put("csrftoken", token);
        return ViewUtil.render(request, model, "/velocity/transaction.vm");
    };

    /** Handle the OAuth endpoint for this application */
    public static Route serveOAuthPage = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        // exchange the request token for an access token
        String accessToken = exchangeToken(request.queryParams("token"));

        // check that an access token was received; redirect back to Wondough's
        // authentication page
        if(accessToken == null) {
            response.redirect("http://localhost:8000/auth?app=1&target=http://localhost:8080/oauth");
        }

        // store the access token in the session and make it available to
        // the browser so that the API can be accessed from JS
        request.session().attribute("currentUser", accessToken);
        response.cookie("accessToken", accessToken);

        // redirect back to the front page of the sample application to
        // discard of any remaining query parameters
        response.redirect("http://localhost:8080/");
        return null;
    };
    private static String getCSRFtoken(String accessToken) {
        try {
            // initialise the HTTP request
            URL url = new URL("http://localhost:8000/transactions/csrf");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");

            // write the access token to the request body
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(URLEncoder.encode("token") + "=" + URLEncoder.encode(accessToken));
            out.flush();
            out.close();

            // get the response status code and check that it's OK
            int status = con.getResponseCode();

            if(status != 200) {
                return null;
            }

            // read the response, which should contain the csrf token
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // close the HTTP connection
            con.disconnect();

            // return the access token
            return content.toString();
        }
        catch(IOException ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
    /**
    * Exchanges a request token for an access token.
    * @param requestToken The request token to exchange.
    */
    private static String exchangeToken(String requestToken) {
        try {
            // initialise the HTTP request
            URL url = new URL("http://localhost:8000/exchange");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");

            // write the request token to the request body
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(URLEncoder.encode("token") + "=" + URLEncoder.encode(requestToken));
            out.flush();
            out.close();

            // get the response status code and check that it's OK
            int status = con.getResponseCode();

            if(status != 200) {
                return null;
            }

            // read the response, which should contain the access token
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // close the HTTP connection
            con.disconnect();

            // return the access token
            return content.toString();
        }
        catch(IOException ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
}
