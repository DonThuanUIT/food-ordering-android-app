package com.foodorderingapp.model.response;

public class ChatMessageResponse {
    private String id;
    private String roomId;
    private String senderId;
    private String content;
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
