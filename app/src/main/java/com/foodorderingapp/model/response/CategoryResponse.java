package com.foodorderingapp.model.response;

import java.util.UUID;

public class CategoryResponse {
    private UUID id;
    private String name;

    public CategoryResponse() {}

    public CategoryResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
