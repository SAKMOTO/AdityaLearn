package com.example.adityalearn;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private String message;
    private int type;

    // Constructor using boolean
    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.type = isUser ? TYPE_USER : TYPE_AI;
    }

    // Constructor using int type
    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() { return message; }
    public int getType() { return type; }
}
