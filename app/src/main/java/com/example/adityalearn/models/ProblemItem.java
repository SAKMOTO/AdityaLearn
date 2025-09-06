package com.example.adityalearn.models;

public class ProblemItem {
    private String title;
    private String difficulty;
    private String link;

    public ProblemItem(String title, String difficulty, String link) {
        this.title = title;
        this.difficulty = difficulty;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getLink() {
        return link;
    }
}
