package com.foodorderingapp.model.response;

public class ChatMessageResponse {
    private String id;
    private ChatRoomResponse room;
    private ChatUserResponse sender;
    private String content;
    private Boolean isRead;
    private String createdAt;

    public String getId() {
        return id;
    }

    public ChatRoomResponse getRoom() {
        return room;
    }

    public ChatUserResponse getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
