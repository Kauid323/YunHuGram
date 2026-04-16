package org.telegram.yh;

public class YhApiStatus {

    private final long number;
    private final int code;
    private final String message;

    public YhApiStatus(long number, int code, String message) {
        this.number = number;
        this.code = code;
        this.message = message;
    }

    public long getNumber() {
        return number;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return code == 1;
    }
}
