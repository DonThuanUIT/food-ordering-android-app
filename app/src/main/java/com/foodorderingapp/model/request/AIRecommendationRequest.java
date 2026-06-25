package com.foodorderingapp.model.request;

public class AIRecommendationRequest {
    private String query;

    public AIRecommendationRequest() {}

    public AIRecommendationRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
