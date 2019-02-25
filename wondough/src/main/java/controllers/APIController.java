package wondough;

import java.util.*;
import java.sql.SQLException;

import static spark.Spark.*;
import spark.*;
import wondough.*;
import static wondough.SessionUtil.*;
import net.sf.xsshtmlfilter.HTMLFilter;

/**
* Contains route handlers for API endpoints.
* @author The Intern
*/
public class APIController {
    /** Lists all transactions for a user. */
    public static Route getTransactions = (Request request, Response response) -> {
        // allow requests from anywhere
        response.header("Access-Control-Allow-Origin", "*");

        // retrieve the access token from the request
        String token = request.queryParams("token");

        try {
            Integer user = Program.getInstance().getDbConnection().isValidAccessToken(token);

            if(user == null) {
                return "Not a valid access token!";
            }

            return Program.getInstance().getDbConnection().getTransactions(user);
        }
        catch(SQLException ex) {
            return ex.toString();
        }
    };
    public static Route handlecsrf = (Request request, Response response) ->{
      String token = request.queryParams("token");
      try{
        Integer user = Program.getInstance().getDbConnection().isValidAccessToken(token);

        if(user == null){
          return "Not a valid access token!";
        }

        String csrf = Program.getInstance().getDbConnection().getCSRF(user);
        return csrf;
      }catch(SQLException e){
        return e.toString();
      }
    };
    /** Transfers money from the user's account to another. */
    public static Route postTransaction = (Request request, Response response) -> {
        // allow requests from anywhere
        response.header("Access-Control-Allow-Origin", "*");

        // retrieve the access token from the request
        String token = request.queryParams("token");
        String csrf = request.queryParams("csrf");
        try {
            Integer user = Program.getInstance().getDbConnection().isValidAccessToken(token);

            if(user == null) {
                return "Not a valid access token!";
            }

            if(!Program.getInstance().getDbConnection().isValidCSRFToken(user,csrf)){
              return "Not a valid CSRF token!";
            }



            Integer recipient = Program.getInstance().getDbConnection().findUserByName(request.queryParams("recipient"));

            if(recipient == null) {
                halt(400, "Not a valid recipient!");
            }
            HTMLFilter filter = new HTMLFilter();
            String description = filter.filter(request.queryParams("description"));
            return Program.getInstance().getDbConnection().createTransaction(
                user, recipient, description, Float.parseFloat(request.queryParams("amount")));

            // create transaction
        }
        catch(SQLException ex) {
            return ex.toString();
        }
    };
}
