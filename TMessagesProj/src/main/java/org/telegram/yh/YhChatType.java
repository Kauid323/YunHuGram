package org.telegram.yh;

public final class YhChatType {

    public static final int USER = 1;
    public static final int GROUP = 2;
    public static final int BOT = 3;

    private YhChatType() {
    }

    public static boolean isValid(int value) {
        return value == USER || value == GROUP || value == BOT;
    }
}
