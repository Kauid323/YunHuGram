package org.telegram.yh;

import java.io.IOException;

public class YhApiException extends IOException {

    private final int httpCode;

    public YhApiException(String message) {
        this(-1, message);
    }

    public YhApiException(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
