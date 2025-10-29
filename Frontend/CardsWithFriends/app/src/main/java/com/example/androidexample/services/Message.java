package com.example.androidexample.services;

public class Message {
    private final String sender;
    private final String text;
    private final long timestamp;

    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return sender + ": " + text;
    }
}
