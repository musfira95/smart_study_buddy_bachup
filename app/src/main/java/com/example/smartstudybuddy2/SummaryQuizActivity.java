package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class SummaryQuizActivity extends BaseActivity {

    TextView summaryText, quizText;
    Button backButton, submitButton;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_quiz);

        summaryText = findViewById(R.id.tvSummary);
        quizText = findViewById(R.id.tvQuiz);
        backButton = findViewById(R.id.btnBack);
        submitButton = findViewById(R.id.btnSubmit);

        dbHelper = new DatabaseHelper(this);

        // Get transcription from intent
        String transcription = getIntent().getStringExtra("transcription");

        // Generate summary and quiz dynamically
        if (transcription != null && !transcription.isEmpty()) {
            String summary = generateSummary(transcription);
            String quiz = generateQuizFromTranscription(transcription);

            summaryText.setText(summary);
            quizText.setText(quiz);
        } else {
            summaryText.setText("No transcription available. Please record audio first.");
            quizText.setText("Unable to generate quiz without transcription.");
        }

        // Back Button
        backButton.setOnClickListener(v -> finish());

        // Submit button → goes to ViewResultActivity
        submitButton.setOnClickListener(v -> {
            int correct = 1;
            int wrong = 2;

            Intent intent = new Intent(SummaryQuizActivity.this, QuizResultActivity.class);
            intent.putExtra("correctAnswers", correct);
            intent.putExtra("wrongAnswers", wrong);
            startActivity(intent);
        });
    }

    // Generate summary from transcription
    private String generateSummary(String transcription) {
        // This is a placeholder. In production, this would call an AI API
        if (transcription.length() > 100) {
            return "Summary: " + transcription.substring(0, Math.min(100, transcription.length())) + "...";
        }
        return "Summary: " + transcription;
    }

    // Generate quiz from transcription
    private String generateQuizFromTranscription(String transcription) {
        // This is a placeholder. In production, this would call an AI API
        return "Quiz: Based on the transcription provided, answer the following:\n\n" +
               "Q1. What was the main topic discussed?\n" +
               "(a) Topic A  (b) Topic B  (c) Topic C  (d) Topic D\n\n" +
               "Q2. What key points were mentioned?\n" +
               "(a) Point 1  (b) Point 2  (c) Point 3  (d) Point 4";
    }
}