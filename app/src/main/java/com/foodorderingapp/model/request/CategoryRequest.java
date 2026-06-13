package com.foodorderingapp.model.request;

public class CategoryRequest {
    private String name;

    public CategoryRequest() {}

    public CategoryRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
