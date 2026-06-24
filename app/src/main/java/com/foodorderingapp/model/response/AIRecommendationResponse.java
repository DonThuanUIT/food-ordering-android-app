package com.foodorderingapp.model.response;

public class AIRecommendationResponse {
    private FoodResponse food;
    private String reason;

    public AIRecommendationResponse() {}

    public AIRecommendationResponse(FoodResponse food, String reason) {
        this.food = food;
        this.reason = reason;
    }

    public FoodResponse getFood() {
        return food;
    }

    public void setFood(FoodResponse food) {
        this.food = food;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
