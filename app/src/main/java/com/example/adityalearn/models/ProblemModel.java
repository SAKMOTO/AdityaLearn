package com.example.adityalearn.models;

public class ProblemModel {
    private String id;
    private String title;
    private String statement;
    public ProblemModel(String id, String title, String statement) {
        this.id = id; this.title = title; this.statement = statement;
    }
    public String getId(){ return id; }
    public String getTitle(){ return title; }
    public String getStatement(){ return statement; }
}
