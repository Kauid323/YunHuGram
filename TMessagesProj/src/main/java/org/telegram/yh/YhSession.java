package org.telegram.yh;

public class YhSession {

    private final String token;
    private final String userId;

    public YhSession(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isValid() {
        return token != null && token.length() > 0;
    }
}
