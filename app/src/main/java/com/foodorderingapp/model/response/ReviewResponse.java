package com.foodorderingapp.model.response;

public class ReviewResponse {
    private String id;
    private Integer rating;
    private String comment;
    private String createdAt;
    private UserShort user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public UserShort getUser() {
        return user;
    }

    public void setUser(UserShort user) {
        this.user = user;
    }

    public static class UserShort {
        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
