package com.foodorderingapp.model.response;

public class AuthResponse {
    private String message;
    private String phone;
    private String accessToken;
    private String refreshToken;

    private String role;

    public String getMessage() {
        return message;
    }

    public String getPhone() {
        return phone;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getRole() {
        return role;
    }
}
