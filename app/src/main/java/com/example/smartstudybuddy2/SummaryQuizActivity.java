package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;   // ✅ MUST ADD
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class SummaryQuizActivity extends AppCompatActivity {

    TextView summaryText, quizText;
    Button backButton, submitButton; // Add submit button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_quiz);

        summaryText = findViewById(R.id.tvSummary);
        quizText = findViewById(R.id.tvQuiz);
        backButton = findViewById(R.id.btnBack);
        submitButton = findViewById(R.id.btnSubmit);

        // Get transcription from intent
        String transcription = getIntent().getStringExtra("transcription");

        // ---------------- Dummy Summary & Quiz ----------------
        String summary = "Summary: This is a dummy summary for the transcription.";
        String quiz = "Quiz: Q1. What does AI help with? (a) Cooking (b) Note-taking (c) Sleeping";

        summaryText.setText(summary);
        quizText.setText(quiz);

        // Back Button
        backButton.setOnClickListener(v -> finish());

        // Submit button → goes to ViewResultActivity
        submitButton.setOnClickListener(v -> {

            // Dummy values for now
            int correct = 1;
            int wrong = 2;

            Intent intent = new Intent(SummaryQuizActivity.this, QuizResultActivity.class);

            intent.putExtra("correctAnswers", correct);
            intent.putExtra("wrongAnswers", wrong);
            startActivity(intent);
        });
    }
}