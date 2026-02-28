package com.example.smartstudybuddy2.network;

/**
 * Request body for quiz generation API endpoint
 */
public class QuizRequest {
    public String text;
    public int num_questions;

    public QuizRequest(String text, int numQuestions) {
        this.text = text;
        this.num_questions = numQuestions;
    }
}
