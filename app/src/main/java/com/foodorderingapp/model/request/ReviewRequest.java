package com.foodorderingapp.model.request;

public class ReviewRequest {
    private int rating;
    private String comment;

    public ReviewRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
