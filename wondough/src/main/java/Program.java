package wondough;

import static spark.Spark.*;
import wondough.controllers.*;

/**
* This class contains the main entry point for the application.
* @author  The Intern
* @version 0.1
*/
public class Program {
    /** Stores the singleton instance of this class. */
    private static Program program;

    /** Stores the security configuration for this application. */
    private SecurityConfiguration securityConfiguration;

    /** Stores the database connection. */
    private DbConnection connection;

    /** Gets the singleton instance of this class. */
    public static Program getInstance() {
        return program;
    }

    /** Gets the security configuration for this program. */
    public SecurityConfiguration getSecurityConfiguration() {
        return this.securityConfiguration;
    }

    /** Gets the database connection for this program. */
    public DbConnection getDbConnection() {
        return this.connection;
    }

    /** Explicitly mark constructor as private so no instances of this
    * class can be created elsewhere. */
    private Program() {

    }

    /**
    * The main entry point for the application.
    * @param args The command-line arguments supplied by the OS.
    */
    public static void main(String[] args) {
        // initialise and run the program
        program = new Program();
        program.run();
    }

    /**
    * Runs the program.
    */
    private void run() {
        try
        {
            // load the security configuration from a file
            this.securityConfiguration =
                SecurityConfiguration.fromFile("security.json");

            // initialise the database connection
            this.connection = new DbConnection("wondough.db");

            // WondoughUser intern = new WondoughUser(1,"intern@wondoughbank.com");
            // intern.setSalt(this.securityConfiguration.generateSalt());
            // intern.setHashedPassword(this.securityConfiguration.pbkdf2("password", intern.getSalt()));
            // intern.setIterations(this.securityConfiguration.getIterations());
            // intern.setKeySize(this.securityConfiguration.getKeySize());
            // intern.setAttempts(0);
            // intern.setRestrictedDate(new java.sql.Date(0));
            // connection.createUser(intern);
            // WondoughUser intern = new WondoughUser(2,"ymcmb");
            // intern.setSalt(this.securityConfiguration.generateSalt());
            // intern.setHashedPassword(this.securityConfiguration.pbkdf2("1", intern.getSalt()));
            // intern.setIterations(this.securityConfiguration.getIterations());
            // intern.setKeySize(this.securityConfiguration.getKeySize());
            // intern.setAttempts(0);
            // intern.setRestrictedDate(new java.sql.Date(0));
            // connection.createUser(intern);

            // we will run on port 8000
            port(8000);

            // tell the Spark framework where to find static files
            staticFiles.location("/static");

            // map routes to controllers
            get("/auth", AuthController.serveAuthPage);
            post("/auth", AuthController.handleAuth);
            post("/exchange", AuthController.handleExchange);

            get("/transactions", "application/json",
                APIController.getTransactions, new JSONTransformer());
            post("/transactions/new", "application/json",
                APIController.postTransaction, new JSONTransformer());
            post("/transactions/csrf","appplication/json",
                APIController.handlecsrf, new JSONTransformer());
        }
        catch(Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
