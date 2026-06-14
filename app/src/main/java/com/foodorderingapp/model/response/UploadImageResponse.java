package com.foodorderingapp.model.response;

import com.google.gson.annotations.SerializedName;
public class UploadImageResponse {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
