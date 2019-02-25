package sample;

import static spark.Spark.*;

import sample.controllers.*;

/**
* This class contains the main entry point for the application.
* @author  The Intern
* @version 0.1
*/
public class Program {
    /** Stores the singleton instance of this class. */
    private static Program program;

    /** Gets the singleton instance of this class. */
    public static Program getInstance() {
        return program;
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
        // we will run on port 8080
        port(8080);

        // tell the Spark framework where to find static files
        staticFiles.location("/static");

        // map routes to controllers
        get("/", IndexController.serveIndexPage);
        get("/transaction/new", IndexController.serveAddTransactionPage);
        get("/oauth", IndexController.serveOAuthPage);
    }
}
