package com.foodorderingapp;

import android.app.Application;
import com.foodorderingapp.utils.TokenManager;

public class FoodOrderingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo TokenManager ngay khi ứng dụng bắt đầu
        TokenManager.init(this);
    }
}
