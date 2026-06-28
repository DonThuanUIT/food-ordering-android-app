package com.foodorderingapp.model.response;

public class AdminUserResponse {
    private String id;
    private String phone;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String role;
    private Boolean isLocked;

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public boolean isLocked() {
        return isLocked != null && isLocked;
    }
}
