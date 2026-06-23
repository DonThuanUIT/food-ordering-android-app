package com.foodorderingapp.model.request;

public class SendChatMessageRequest {
    private final String shopId;
    private final String roomId;
    private final String content;

    public SendChatMessageRequest(String shopId, String roomId, String content) {
        this.shopId = shopId;
        this.roomId = roomId;
        this.content = content;
    }

    public String getShopId() {
        return shopId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getContent() {
        return content;
    }
}
