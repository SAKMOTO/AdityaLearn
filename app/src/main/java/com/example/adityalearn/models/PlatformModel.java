package com.example.adityalearn.models;

public class PlatformModel {
    private String name;
    private String url;

    public PlatformModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
