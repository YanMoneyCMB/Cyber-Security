package wondough;

public class WondoughApp {
    private int user;
    private String requestToken;
    private String accessToken;
    private String CSRFtoken;

    public WondoughApp(int user) {
        this.user = user;
    }

    public int getUserID() {
        return this.user;
    }

    public String getRequestToken() {
        return this.requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setCSRFtoken(String CSRFtoken){
      this.CSRFtoken = CSRFtoken;
    }
    public String getCSRFtoken(){
      return this.CSRFtoken;
    }
}
