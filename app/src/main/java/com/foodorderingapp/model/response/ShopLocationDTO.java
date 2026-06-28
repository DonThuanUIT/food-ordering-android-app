package com.foodorderingapp.model.response;

public class ShopLocationDTO {
    private String id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String coverUrl;
    private Double rating;
    private boolean currentlyOpen;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getCoverUrl() { return coverUrl; }
    public Double getRating() { return rating; }
    public boolean isCurrentlyOpen() { return currentlyOpen; }
}