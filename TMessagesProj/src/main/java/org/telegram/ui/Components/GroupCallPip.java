package org.telegram.ui.Components;

import android.content.Context;

public class GroupCallPip {

    private static GroupCallPip instance;

    public boolean showAlert;

    public static GroupCallPip getInstance() {
        return instance;
    }

    public static boolean isShowing() {
        return false;
    }

    public static void clearForce() {
    }

    public static void updateVisibility(Context context) {
    }

    public static boolean onBackPressed() {
        return false;
    }
}
