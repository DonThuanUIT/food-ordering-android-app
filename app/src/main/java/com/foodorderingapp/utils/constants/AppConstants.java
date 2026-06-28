package com.foodorderingapp.utils.constants;

import android.os.Build;

public class AppConstants {
    // Emulator uses 10.0.2.2 to reach the host machine.
    // Real devices over USB use 127.0.0.1 after running: adb reverse tcp:8080 tcp:8080
    // For real devices over Wi-Fi, switch REAL_DEVICE_BASE_URL to WIFI_BASE_URL.
    private static final String EMULATOR_BASE_URL = "http://10.0.2.2:8080/api/";
    private static final String USB_REVERSE_BASE_URL = "http://127.0.0.1:8080/api/";
    private static final String WIFI_BASE_URL = "http://192.168.1.105:8080/api/";
    private static final String REAL_DEVICE_BASE_URL = USB_REVERSE_BASE_URL;

    public static final String BASE_URL = isProbablyEmulator()
            ? EMULATOR_BASE_URL
            : REAL_DEVICE_BASE_URL;


    public static String getWsChatUrl() {
        return BASE_URL.replaceFirst("^http", "ws") + "ws-chat";
    }

    private static boolean isProbablyEmulator() {
        String fingerprint = Build.FINGERPRINT != null ? Build.FINGERPRINT.toLowerCase() : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase() : "";
        String product = Build.PRODUCT != null ? Build.PRODUCT.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String device = Build.DEVICE != null ? Build.DEVICE.toLowerCase() : "";

        return fingerprint.contains("generic")
                || fingerprint.contains("emulator")
                || model.contains("sdk")
                || model.contains("emulator")
                || model.contains("android sdk built for")
                || product.contains("sdk")
                || product.contains("emulator")
                || (brand.startsWith("generic") && device.startsWith("generic"));
    }
}
