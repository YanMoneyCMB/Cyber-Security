package wondough;
import java.sql.Date;

public class WondoughUser {
    private int id;
    private String username;
    private String password;
    private String salt;
    private int iterations;
    private int keySize;
    private int attempts;
    private java.sql.Date restrictedDate;

    public WondoughUser(int id, String username) {
        this.username = username;
        this.id=id;
    }

    public int getID() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHashedPassword() {
        return this.password;
    }

    public void setHashedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    public int getIterations() {
        return this.iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }
  //Date the user was restricted. Use to track time passed from restriction in authentication.
    public void setRestrictedDate(java.sql.Date date){
        this.restrictedDate= date;
    }
    public java.sql.Date getRestrictedDate(){
        return this.restrictedDate;
    }
    public void setAttempts(int attempts){
        this.attempts=attempts;
    }
    public int getAttempts(){
      return this.attempts;
    }
}
