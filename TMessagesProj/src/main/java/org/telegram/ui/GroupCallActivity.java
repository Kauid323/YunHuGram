package org.telegram.ui;

import android.content.Context;
import android.widget.FrameLayout;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;

public class GroupCallActivity {

    public static GroupCallActivity groupCallInstance;
    public static boolean groupCallUiVisible;
    public static boolean isLandscapeMode;
    public static boolean isTabletMode;
    public static boolean paused;
    public static final float MAX_AMPLITUDE = 8500f;
    public static final long TRANSITION_DURATION = 220L;
    public static final int TABLET_LIST_SIZE = 320;

    private final FrameLayout container;

    private GroupCallActivity(Context context) {
        container = context == null ? null : new FrameLayout(context);
    }

    public static void create(LaunchActivity activity, AccountInstance account, TLRPC.Chat chat, TLRPC.InputPeer schedulePeer, boolean hasFewPeers, String scheduledHash) {
        groupCallInstance = null;
        groupCallUiVisible = false;
    }

    public static void create(LaunchActivity activity, AccountInstance account, ChatObject.Call groupCall, TLRPC.Chat chat, boolean hasFewPeers, String scheduledHash) {
        groupCallInstance = null;
        groupCallUiVisible = false;
    }

    public static void onLeaveClick(Context context, Runnable onDismiss, boolean canDrawOverlays) {
        if (onDismiss != null) {
            onDismiss.run();
        }
    }

    public void dismiss() {
        groupCallInstance = null;
        groupCallUiVisible = false;
    }

    public void dismissInternal() {
        dismiss();
    }

    public void onResume() {
        paused = false;
    }

    public void onPause() {
        paused = true;
    }

    public void enableCamera() {
    }

    public FrameLayout getContainer() {
        return container;
    }
}
