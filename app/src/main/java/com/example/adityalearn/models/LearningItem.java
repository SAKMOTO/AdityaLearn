package com.example.adityalearn.models;

public class LearningItem {
    private String title;
    private String url;

    public LearningItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
