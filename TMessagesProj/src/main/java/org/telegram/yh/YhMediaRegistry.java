package org.telegram.yh;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

public final class YhMediaRegistry {
    public static final int YH_SYNTHETIC_DC_ID = Integer.MIN_VALUE;

    public static final class MediaEntry {
        public final String url;
        public final String previewUrl;
        public final String mimeType;
        public final String fileName;
        public final long size;

        private MediaEntry(String url, String previewUrl, String mimeType, String fileName, long size) {
            this.url = url;
            this.previewUrl = previewUrl;
            this.mimeType = mimeType;
            this.fileName = fileName;
            this.size = size;
        }
    }

    private static final ConcurrentHashMap<Long, MediaEntry> PHOTO_ENTRIES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, MediaEntry> DOCUMENT_ENTRIES = new ConcurrentHashMap<>();

    private YhMediaRegistry() {
    }

    public static void registerPhoto(long photoId, String url, String mimeType, long size) {
        registerPhoto(photoId, url, url, mimeType, null, size);
    }

    public static void registerPhoto(long photoId, String url, String previewUrl, String mimeType, String fileName, long size) {
        put(PHOTO_ENTRIES, photoId, url, previewUrl, mimeType, fileName, size);
    }

    public static void registerDocument(long documentId, String url, String previewUrl, String mimeType, String fileName, long size) {
        put(DOCUMENT_ENTRIES, documentId, url, previewUrl, mimeType, fileName, size);
    }

    public static MediaEntry findPhoto(long photoId) {
        return photoId == 0 ? null : PHOTO_ENTRIES.get(photoId);
    }

    public static MediaEntry findDocument(long documentId) {
        return documentId == 0 ? null : DOCUMENT_ENTRIES.get(documentId);
    }

    public static String findPhotoUrl(long photoId) {
        MediaEntry entry = findPhoto(photoId);
        return entry == null ? null : entry.url;
    }

    public static String findPhotoPreviewUrl(long photoId) {
        MediaEntry entry = findPhoto(photoId);
        return entry == null ? null : firstNonEmpty(entry.previewUrl, entry.url);
    }

    public static String findDocumentUrl(long documentId) {
        MediaEntry entry = findDocument(documentId);
        return entry == null ? null : entry.url;
    }

    public static String findDocumentPreviewUrl(long documentId) {
        MediaEntry entry = findDocument(documentId);
        return entry == null ? null : firstNonEmpty(entry.previewUrl, entry.url);
    }

    private static void put(ConcurrentHashMap<Long, MediaEntry> map, long id, String url, String previewUrl, String mimeType, String fileName, long size) {
        if (id == 0) {
            return;
        }
        String normalizedUrl = YhImageUrlHelper.getDetailImageUrl(url);
        String normalizedPreviewUrl = YhImageUrlHelper.getDetailImageUrl(firstNonEmpty(previewUrl, url));
        if (TextUtils.isEmpty(normalizedUrl) && TextUtils.isEmpty(normalizedPreviewUrl)) {
            map.remove(id);
            return;
        }
        map.put(id, new MediaEntry(
                normalizedUrl,
                normalizedPreviewUrl,
                TextUtils.isEmpty(mimeType) ? "application/octet-stream" : mimeType,
                fileName,
                Math.max(size, 0L)
        ));
    }

    private static String firstNonEmpty(String first, String second) {
        return !TextUtils.isEmpty(first) ? first : second;
    }
}
