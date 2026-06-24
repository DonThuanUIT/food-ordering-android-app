package com.foodorderingapp.model.response;

public class ChatRoomResponse {
    private String roomId;
    private String partnerId;
    private String partnerName;
    private String lastMessage;
    private String lastMessageAt;
    private long unreadCount;

    public String getRoomId() {
        return roomId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public long getUnreadCount() {
        return unreadCount;
    }
}
