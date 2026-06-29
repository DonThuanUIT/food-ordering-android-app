package com.foodorderingapp.model.request;

public class ReviewReplyRequest {
    private String replyText;
    private String reviewType;

    public ReviewReplyRequest() {}

    public ReviewReplyRequest(String replyText) {
        this.replyText = replyText;
    }

    public ReviewReplyRequest(String replyText, String reviewType) {
        this.replyText = replyText;
        this.reviewType = reviewType;
    }

    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public String getReviewType() { return reviewType; }
    public void setReviewType(String reviewType) { this.reviewType = reviewType; }
}
