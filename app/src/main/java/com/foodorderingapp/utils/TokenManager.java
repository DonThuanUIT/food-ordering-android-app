package com.foodorderingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "food_ordering_app";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FULL_NAME = "full_name";

    private static TokenManager instance;
    private final SharedPreferences sharedPreferences;

    private TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
    }

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("TokenManager must be initialized with context before use.");
        }
        return instance;
    }

    public void saveTokens(String accessToken, String refreshToken) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveUserSession(String phone, String role) {
        saveUserSession(phone, role, null);
    }

    public void saveUserSession(String phone, String role, String fullName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String existingPhone = getPhone();
        boolean hasNewPhone = !isBlank(phone);
        boolean sameUser = existingPhone == null || existingPhone.equals(phone);

        if (hasNewPhone) {
            editor.putString(KEY_PHONE, phone);
        }
        if (!isBlank(role)) {
            editor.putString(KEY_ROLE, role);
        }
        if (!isBlank(fullName)) {
            editor.putString(KEY_FULL_NAME, fullName);
        } else if (hasNewPhone && !sameUser) {
            editor.remove(KEY_FULL_NAME);
        }

        editor.apply();
    }

    public void saveAccessToken(String token) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply();
    }

    public void saveRefreshToken(String token) {
        sharedPreferences.edit()
                .putString(KEY_REFRESH_TOKEN, token)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }

    public void clearTokens() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_PHONE)
                .remove(KEY_ROLE)
                .remove(KEY_FULL_NAME)
                .apply();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
