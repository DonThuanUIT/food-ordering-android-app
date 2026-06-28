package com.foodorderingapp.model.request;

public class AIRecommendationRequest {
    private String query;
    private Double userLat;
    private Double userLng;
    private String buildingName;

    public AIRecommendationRequest() {}

    public AIRecommendationRequest(String query) {
        this.query = query;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Double getUserLat() { return userLat; }
    public void setUserLat(Double userLat) { this.userLat = userLat; }
    public Double getUserLng() { return userLng; }
    public void setUserLng(Double userLng) { this.userLng = userLng; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
}
