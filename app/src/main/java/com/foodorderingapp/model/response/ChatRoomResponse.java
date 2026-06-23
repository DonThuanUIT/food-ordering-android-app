package com.foodorderingapp.model.response;

public class ChatRoomResponse {
    private String id;
    private ChatUserResponse student;
    private ChatShopResponse shop;
    private String createdAt;
    private String updatedAt;

    public String getId() {
        return id;
    }

    public ChatUserResponse getStudent() {
        return student;
    }

    public ChatShopResponse getShop() {
        return shop;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
