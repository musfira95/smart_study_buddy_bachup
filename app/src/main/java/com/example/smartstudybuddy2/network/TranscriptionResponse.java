package com.example.smartstudybuddy2.network;

public class TranscriptionResponse {
    public String text;

    public TranscriptionResponse() {
    }

    public TranscriptionResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
