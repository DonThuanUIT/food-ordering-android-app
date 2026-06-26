package com.foodorderingapp.utils;

import com.foodorderingapp.utils.constants.AppConstants;

public final class ImageUrlUtils {

    private ImageUrlUtils() {
    }

    public static String resolveImageUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String url = value.trim();
        if (url.startsWith("http://") || url.startsWith("https://")
                || url.startsWith("content://") || url.startsWith("file://")) {
            return url;
        }

        String rootUrl = AppConstants.BASE_URL;
        int apiIndex = rootUrl.indexOf("/api/");
        if (apiIndex >= 0) {
            rootUrl = rootUrl.substring(0, apiIndex + 1);
        }

        if (url.startsWith("/")) {
            return trimTrailingSlash(rootUrl) + url;
        }
        return trimTrailingSlash(rootUrl) + "/" + url;
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
