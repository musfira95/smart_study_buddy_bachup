package com.example.smartstudybuddy2.network;

/**
 * Request body for summarization API endpoint
 */
public class SummaryRequest {
    public String text;

    public SummaryRequest(String text) {
        this.text = text;
    }
}
