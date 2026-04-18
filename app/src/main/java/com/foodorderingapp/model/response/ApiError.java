package com.foodorderingapp.model.response;

public class ApiError {
    private String timestamp;
    private int status;
    private String message;
    private String path;

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
