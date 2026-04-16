package org.telegram.messenger.call;

public class CallConfig {

    public static final int VIDEO_STATE_INACTIVE = 0;
    public static final int VIDEO_STATE_ACTIVE = 1;
    public static final int VIDEO_STATE_PAUSED = 2;

    public static final int AUDIO_STATE_ACTIVE = 0;
    public static final int AUDIO_STATE_MUTED = 1;

    public static final int DATA_SAVING_NEVER = 0;
    public static final int DATA_SAVING_ROAMING = 1;
    public static final int DATA_SAVING_MOBILE = 2;
    public static final int DATA_SAVING_ALWAYS = 3;

    public static final String ERROR_UNKNOWN = "unknown";
    public static final String ERROR_INCOMPATIBLE = "incompatible";
    public static final String ERROR_PEER_OUTDATED = "peer_outdated";
    public static final String ERROR_PRIVACY = "privacy";
    public static final String ERROR_AUDIO_IO = "audio_io";
    public static final String ERROR_LOCALIZED = "localized";
    public static final String ERROR_CONNECTION_SERVICE = "connection_service";

    private static final ServerConfig GLOBAL_SERVER_CONFIG = new ServerConfig();

    public static ServerConfig getGlobalServerConfig() {
        return GLOBAL_SERVER_CONFIG;
    }

    public static class ServerConfig {
        public boolean enable_vp8_encoder;
        public boolean enable_vp9_encoder;
        public boolean enable_h264_encoder;
        public boolean enable_h265_encoder;
        public boolean useSystemNs;
        public boolean useSystemAec;

        public String getString(String key) {
            return "";
        }
    }
}
