package com.foodorderingapp.utils.constants;

public class AppConstants {
    // 1. Dùng http://10.0.2.2:8080/api/ nếu bạn đang dùng Android Emulator
    // 2. Dùng IP máy tính (VD: http://192.168.1.x:8080/api/) nếu bạn dùng điện thoại thật
    // Lưu ý: Tuyệt đối không dùng 127.0.0.1 hoặc localhost
    public static final String BASE_URL = "http://192.168.1.105:8080/api/";


    public static String getWsChatUrl() {
        return BASE_URL.replaceFirst("^http", "ws") + "ws-chat";
    }
}
