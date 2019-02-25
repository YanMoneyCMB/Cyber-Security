package wondough.controllers;

import java.util.*;
import java.net.*;
import java.sql.SQLException;

import spark.*;
import wondough.*;
import static wondough.SessionUtil.*;

public class AuthController {
    /** Serve the auth page (GET manually) */
    public static Route serveAuthPage = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();
        // Changed to app ID = 1 instead of relying on the GET request as there could be maicious links in there.
        // and we are only testing one application.
        String name = Program.getInstance().getDbConnection().lookupApp(1);

        if(name == null) {
            response.status(400);
            return "Invalid appid.";
        }
        ///Changed target to the link as there could only be one valid parameter for this application
        model.put("appname", name);
        model.put("target", "http://localhost:8080/oauth");

        return ViewUtil.render(request, model, "/velocity/auth.vm");
    };

    public static Route handleExchange = (Request request, Response response) -> {
        // retrieve the request token from the request
        String token = request.queryParams("token");

        String accessToken = Program.getInstance().getDbConnection().exchangeToken(token);

        if(accessToken == null) {
            response.status(400);
            return "Invalid request token.";
        }
        else {
            return accessToken;
        }
    };

    public static Route handleAuth = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();
        model.put("target", "http://localhost:8080/oauth");
        model.put("appname", request.queryParams("appname"));

        // retrieve the username and password from the request
        String username = request.queryParams("username");
        String password = request.queryParams("password");

        // make sure the username and password aren't empty
        if (username.isEmpty() || password.isEmpty()) {
            model.put("error", "Empty username or password!");
            return ViewUtil.render(request, model, "/velocity/auth.vm");
        }

        // try to find the user in the database
        WondoughUser user = null;

        try {
            user = Program.getInstance().getDbConnection().getUser(username);

            if(user == null) {
                model.put("error", "No such user!");
                return ViewUtil.render(request, model, "/velocity/auth.vm");
            }
        }
        catch(SQLException ex) {
            model.put("error", ex.toString());
            return ViewUtil.render(request, model, "/velocity/auth.vm");
        }

        // retrieve the global security configuration
        SecurityConfiguration config = Program.getInstance().getSecurityConfiguration();

        // hash the plain text password supplied by the client using the
        // security configuration for this particular user
        String hashedPassword = config.pbkdf2(password, user.getSalt(), user.getIterations(), user.getKeySize());

        // check that the hashed passwords match
        //NOTE: if not, handle it as an attempt and handle accodringly
        // Limit of attempts is set to 5 and time for which account will be blocked
        // is 10 minutes.
        long mill = System.currentTimeMillis();
        java.sql.Date now = new java.sql.Date(mill);
        long diff = (now.getTime() - user.getRestrictedDate().getTime())/60000; // difference in minutes
        if(diff<10){
          model.put("error","User is blocked for "+(10-diff)+" more minutes. Try again later.");
          return ViewUtil.render(request, model, "/velocity/auth.vm");
        }
        if(!user.getHashedPassword().equals(hashedPassword)) {

            Program.getInstance().getDbConnection().failedAttempt(user);
            model.put("error", "Incorrect password!");

            return ViewUtil.render(request, model, "/velocity/auth.vm");
        }

        // check that the user's configuration is up-to-date;
        // if not, re-hash the password
        //IMPLEMENTED
        if(user.getIterations() != config.getIterations() ||
            user.getKeySize() != config.getKeySize()) {
              user.setKeySize(config.getKeySize());
              user.setIterations(config.getIterations());
              user.setHashedPassword(config.pbkdf2(password,user.getSalt()));
              Program.getInstance().getDbConnection().updateUsersConfig(user);
        }

        // authorise an app
        WondoughApp app = null;

        try {
            // create an authorisation for this user
            app = Program.getInstance().getDbConnection().createApp(user);

            if(app == null) {
                model.put("error", "Couldn't authorise application!");
                return ViewUtil.render(request, model, "/velocity/auth.vm");
            }
        }
        catch(SQLException ex) {
            model.put("error", ex.toString());
            return ViewUtil.render(request, model, "/velocity/auth.vm");
        }

        // redirect the user somewhere, if this was requested
        if (getQueryLoginRedirect(request) != null) {
            // redirect to the target URL and append the token;
            // the token is hashed for security so that its
            // value cannot be read
            response.redirect(
                getQueryLoginRedirect(request) +
                "?token=" + URLEncoder.encode(config.md5(app.getRequestToken())));
        }

        return ViewUtil.render(request, model, "/velocity/auth.vm");
    };

}
