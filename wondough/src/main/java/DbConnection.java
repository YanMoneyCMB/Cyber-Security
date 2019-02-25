package wondough;

import java.sql.*;
import java.lang.StringBuilder;

/**
* Represents a connection to the not-quite-as-volatile database.
* @author  The Intern
* @version 0.1
*/

public class DbConnection {
    /** The database connection to use. */
    private Connection connection;
private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    /**
    * Initialises a new database connection.
    * @param filename The name of the SQLite database file.
    */
    public DbConnection(String filename) throws SQLException {
        // construct the connection string
        String url = "jdbc:sqlite:" + filename;

        // connect to the database
        this.connection = DriverManager.getConnection(url);
    }

    /**
    * Retrieves the next User ID to use.
    */
    private int largestUserID() throws SQLException {
        Statement stmt = null;
        String query = "SELECT id FROM users ORDER BY id DESC LIMIT 1;";

        try {
            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if(rs.next()) {
                return rs.getInt("id") + 1;
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return 0;
    }

    /**
    * Retrieves the next request token ID to use.
    */
    // private int largestRequestToken() throws SQLException {
    //     Statement stmt = null;
    //     String query = "SELECT requestToken FROM authorised_apps ORDER BY requestToken DESC LIMIT 1;";
    //
    //     try {
    //         stmt = this.connection.createStatement();
    //         ResultSet rs = stmt.executeQuery(query);
    //
    //         if(rs.next()) {
    //             return rs.getInt("requestToken") + 1;
    //         }
    //     } catch (SQLException e ) {
    //         throw e;
    //     } finally {
    //         if (stmt != null) { stmt.close(); }
    //     }
    //
    //     return 0;
    // }


      private String generateToken() throws SQLException{
        int count = 20;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
          int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
          builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    // /**
    // * Retrieves the next access token ID to use.
    // */
    // private int largestAccessToken() throws SQLException {
    //     Statement stmt = null;
    //     String query = "SELECT accessToken FROM authorised_apps ORDER BY accessToken DESC LIMIT 1;";
    //
    //     try {
    //         stmt = this.connection.createStatement();
    //         ResultSet rs = stmt.executeQuery(query);
    //
    //         if(rs.next()) {
    //             return rs.getInt("accessToken") + 1;
    //         }
    //     } catch (SQLException e ) {
    //         throw e;
    //     } finally {
    //         if (stmt != null) { stmt.close(); }
    //     }
    //
    //     return 0;
    // }

    /**
    * Inserts the specified user account into the database. This method
    * assumes that the ID of the user is not set to anything.
    * @param user The user account to insert.
    */
    public void createUser(WondoughUser user) throws SQLException {
        // get the next available ID for this user
        int id = this.largestUserID();

        // create a prepared statement to insert the user account
        // into the database
        Statement stmt = null;
        String query = "INSERT INTO users (id,username,password,salt,iterations,keySize,attempts,restrictedDate) VALUES (" + id + ", '" + user.getUsername() + "' , '" + user.getHashedPassword() + "' , '" + user.getSalt() + "' ," + user.getIterations() + "," + user.getKeySize() + "," + user.getAttempts()+"," + user.getRestrictedDate()+ ");";

        // try to insert the user into the database
        try {
            stmt = this.connection.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }
    }

    /**
    * Looks up a user by their username.
    * @param username The username to lookup.
    */
    //NOTE: Prepared statement to avoid SQL injections
    public WondoughUser getUser(String username) throws SQLException {
        PreparedStatement stmt = null;
        String query = "SELECT * FROM users WHERE username=? LIMIT 1;";

        try {
            stmt = this.connection.prepareStatement(query);
            stmt.setString(1,username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                WondoughUser user = new WondoughUser(rs.getInt("id"), rs.getString("username"));
                user.setHashedPassword(rs.getString("password"));
                user.setSalt(rs.getString("salt"));
                user.setIterations(rs.getInt("iterations"));
                user.setKeySize(rs.getInt("keySize"));
                user.setAttempts(rs.getInt("attempts"));
                user.setRestrictedDate(rs.getDate("restrictedDate"));
                return user;
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return null;
    }

    /**
    * Looks up whether an app exists and returns the display name of the
    * application if successful.
    * @param id The ID of the application.
    */
    public String lookupApp(int id) throws SQLException {
        PreparedStatement stmt = null;
        String query = "SELECT name FROM apps WHERE appid=? LIMIT 1;";

        try {
            stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return null;
    }

    /**
    * Authorises a new application to perform actions on behalf
    * of the specified user.
    * @param user The user for whom the app should be registered.
    */
    public WondoughApp createApp(WondoughUser user) throws SQLException {
        PreparedStatement stmt = null;
        String query = "INSERT INTO authorised_apps (user,requestToken,accessToken,CSRFtoken) VALUES (?,?,?,?);";

        try {
            WondoughApp app = new WondoughApp(user.getID());
            // app.setRequestToken(Integer.toString(this.largestRequestToken(), 10));
            // app.setAccessToken(Integer.toString(this.largestAccessToken(), 10));
            app.setRequestToken(generateToken());
            app.setAccessToken(generateToken());
            app.setCSRFtoken(generateToken());
            stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, user.getID());
            stmt.setString(2, app.getRequestToken());
            stmt.setString(3, app.getAccessToken());
            stmt.setString(4,app.getCSRFtoken());
            stmt.executeUpdate();

            return app;
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }
    }


    public String getCSRF(Integer user) throws SQLException{
      PreparedStatement stmt = null;
      ResultSet rs = null;
      SecurityConfiguration config = Program.getInstance().getSecurityConfiguration();
      String sql = "SELECT CSRFtoken from authorised_apps where user=?";

      try{
        stmt = this.connection.prepareStatement(sql);
        stmt.setInt(1,user);
        rs=stmt.executeQuery();
        rs.next();
        return rs.getString(1);
      }catch(SQLException e){
        throw e;
      }finally{if(stmt!=null) stmt.close();}

    }

    public boolean isValidCSRFToken(int user, String token) throws SQLException{
      PreparedStatement stmt = null;
      String sql = "SELECT CSRFtoken from authorised_apps where user=?";
      ResultSet rs = null;
      
      try{
        stmt=this.connection.prepareStatement(sql);
        stmt.setInt(1,user);
        rs=stmt.executeQuery();
        while(rs.next()){
          String got = rs.getString(1);

          if(got.equals(token)){
            return true;
          }
        }
      }catch(SQLException e){
        throw e;
      }finally{if(stmt!=null) stmt.close();}
      return false;
    }
    /**
    * Exchanges a request token for an access token.
    * @param requestToken The request token to exchange.
    */
    public String exchangeToken(String requestToken) throws SQLException {
        SecurityConfiguration config = Program.getInstance().getSecurityConfiguration();
        Statement stmt = null;
        String query = "SELECT requestToken, accessToken FROM authorised_apps;";

        try {
            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while(rs.next()) {
                String token = config.md5(rs.getString("requestToken"));

                if(token.equals(requestToken)) {
                    return config.md5(rs.getString("accessToken"));
                }
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return null;
    }

    /**
    * Validates whether the specified string is a valid access token and returns
    * the unique ID of the user it belongs to.
    * @param accessToken The access token to validate.
    */
    public Integer isValidAccessToken(String accessToken) throws SQLException {
        SecurityConfiguration config = Program.getInstance().getSecurityConfiguration();
        Statement stmt = null;
        String query = "SELECT user, accessToken FROM authorised_apps;";

        try {
            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while(rs.next()) {
                String token = config.md5(rs.getString("accessToken"));

                if(token.equals(accessToken)) {
                    return rs.getInt("user");
                }
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return null;
    }

    /**
    * Looks up a user by their username and returns their unique ID.
    * @param username The username to lookup.
    */
    public Integer findUserByName(String username) throws SQLException {
        PreparedStatement stmt = null;
        String query = "SELECT id FROM users WHERE username=? LIMIT 1;";

        try {
            stmt = this.connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        return null;
    }
//ADDED METHOD TO UPDATE DATABASE WITH THE NEW SECURITY CONFIGURATION FOR THE USER
    public void updateUsersConfig(WondoughUser user) throws SQLException{
      PreparedStatement stmt = null;
      String sql = "UPDATE users SET password=?,iterations=?,keySize=? WHERE id = ?;";
      try{
        stmt=this.connection.prepareStatement(sql);
        stmt.setString(1,user.getHashedPassword());
        stmt.setInt(2,user.getIterations());
        stmt.setInt(3,user.getKeySize());
        stmt.setInt(4,user.getID());
        stmt.executeUpdate();
      }catch(SQLException e){
        throw e;
      }finally{
        if(stmt!=null){stmt.close();}
      }


    }
    public void failedAttempt(WondoughUser user) throws SQLException{
      if(user.getAttempts() == 4){
        PreparedStatement stmt = null;
        String sql = "UPDATE users SET restrictedDate=?,attempts=? WHERE id = ?;";
        long mil=System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(mil);
        try{
          stmt=this.connection.prepareStatement(sql);
          stmt.setDate(1,date);
          stmt.setInt(2,0);
          stmt.setInt(3,user.getID());
          stmt.executeUpdate();
          user.setRestrictedDate(date);
        }catch(SQLException e){
          throw e;
        }finally{
          if(stmt!=null){stmt.close();}
        }
      }else{
        PreparedStatement stmt = null;
        String sql = "UPDATE users SET attempts=? where id= ?;";
        int new_attempts = user.getAttempts()+1;
        try{
          stmt = this.connection.prepareStatement(sql);
          stmt.setInt(1,new_attempts);
          stmt.setInt(2,user.getID());
          stmt.executeUpdate();
        }finally{
          if(stmt!=null){stmt.close();}
        }
      }
    }

    /**
    * Creates a new transaction.
    * @param user The ID of the user sending the money.
    * @param recipient The ID of the recipient of the money.
    * @param description The description of the transaction.
    * @param amount The amount that is being transferred.
    */
    public boolean createTransaction(int user, int recipient, String description, float amount) throws SQLException {
        // don't allow users to send negative amounts
        if(amount < 0) {
            return false;
        }

        PreparedStatement creditStmt = null;
        PreparedStatement debitStmt = null;
        String creditQuery = "INSERT INTO transactions (uid,value,description) VALUES (?,?,?)";
        String debitQuery = "INSERT INTO transactions (uid,value,description) VALUES (?,?,?)";

        try {
            creditStmt = this.connection.prepareStatement(creditQuery);
            debitStmt = this.connection.prepareStatement(debitQuery);

            debitStmt.setInt(1, user);
            debitStmt.setFloat(2, -amount);
            debitStmt.setString(3, description);

            debitStmt.executeUpdate();

            creditStmt.setInt(1, recipient);
            creditStmt.setFloat(2, amount);
            creditStmt.setString(3, description);

            creditStmt.executeUpdate();

            return true;
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (creditStmt != null) { creditStmt.close(); }
            if (debitStmt != null) { debitStmt.close(); }
        }
    }

    /**
    * Gets all transactions for a user.
    * @param user The unique ID of the user to look up transactions for.
    */
    public Transactions getTransactions(int user) throws SQLException {
        PreparedStatement stmt = null;
        String query = "SELECT * FROM transactions WHERE uid=? ORDER BY tid DESC;";

        try {
            stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, user);
            ResultSet rs = stmt.executeQuery();

            Transactions result = new Transactions();
            float total = 0.0f;

            while(rs.next()) {
                Transaction t = new Transaction(rs.getInt("tid"));
                t.setAmount(rs.getFloat("value"));
                t.setDescription(rs.getString("description"));
                result.addTransaction(t);

                total += t.getAmount();
            }

            result.setAccountBalance(total);

            return result;
        } catch (SQLException e ) {
            throw e;
        } finally {
            if (stmt != null) { stmt.close(); }
        }
    }

    /**
    * Closes the database connection.
    */
    public void close() throws SQLException {
        this.connection.close();
    }
}
