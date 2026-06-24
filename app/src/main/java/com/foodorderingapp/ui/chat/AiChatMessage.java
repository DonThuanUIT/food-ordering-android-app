package com.foodorderingapp.ui.chat;

import com.foodorderingapp.model.response.FoodResponse;
import java.util.List;

public class AiChatMessage {
    private boolean isUser;
    private String text;
    private List<FoodResponse> recommendations;

    public AiChatMessage(boolean isUser, String text) {
        this.isUser = isUser;
        this.text = text;
    }

    public AiChatMessage(boolean isUser, String text, List<FoodResponse> recommendations) {
        this.isUser = isUser;
        this.text = text;
        this.recommendations = recommendations;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<FoodResponse> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<FoodResponse> recommendations) {
        this.recommendations = recommendations;
    }
}
