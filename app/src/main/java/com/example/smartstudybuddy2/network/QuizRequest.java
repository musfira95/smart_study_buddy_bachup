package com.example.smartstudybuddy2.network;

/**
 * Request body for quiz generation API endpoint
 */
public class QuizRequest {
    public String text;
    public int num_questions;
    public Integer recording_id;  // ✅ NEW: Pass recording_id for database persistence

    public QuizRequest(String text, int numQuestions) {
        this(text, numQuestions, null);
    }
    
    public QuizRequest(String text, int numQuestions, Integer recordingId) {
        this.text = text;
        this.num_questions = numQuestions;
        this.recording_id = recordingId;
    }
}
