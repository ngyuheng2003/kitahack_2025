package com.example.kitahack2025;

public class ChatModel {
    public static String SENT_BY_USER="USER";
    public static String SENT_BY_AI="AI";
    String text,sender;

    public ChatModel(String text, String sender) {
        this.text = text;
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }
}
