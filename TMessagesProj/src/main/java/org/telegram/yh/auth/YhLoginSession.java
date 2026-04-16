package org.telegram.yh.auth;

public class YhLoginSession {

    private final String token;
    private final String mobile;
    private final String deviceId;

    public YhLoginSession(String token, String mobile, String deviceId) {
        this.token = token;
        this.mobile = mobile;
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public String getMobile() {
        return mobile;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
