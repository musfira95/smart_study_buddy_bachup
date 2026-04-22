package com.example.smartstudybuddy2.network;

/**
 * Request body for summarization API endpoint
 */
public class SummaryRequest {
    public String text;
    public Integer recording_id;  // ✅ NEW: Pass recording_id for database persistence

    public SummaryRequest(String text) {
        this(text, null);
    }
    
    public SummaryRequest(String text, Integer recordingId) {
        this.text = text;
        this.recording_id = recordingId;
    }
}
