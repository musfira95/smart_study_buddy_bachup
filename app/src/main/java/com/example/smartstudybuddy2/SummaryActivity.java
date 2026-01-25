package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

public class SummaryActivity extends AppCompatActivity {

    private TextView tvSummary;
    private LinearLayout btnGenerateQuiz, btnSkipQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvSummary = findViewById(R.id.tvSummaryContent);
        btnGenerateQuiz = findViewById(R.id.btnGenerateQuiz); // LinearLayout reference
        btnSkipQuiz = findViewById(R.id.btnSkipQuiz);

        String transcriptionText = getIntent().getStringExtra("transcriptionText");

        // Dummy summary for demonstration
        String summaryText = "Summary: This is a dummy summary for the transcription.\n" + transcriptionText;
        tvSummary.setText(summaryText);

        // Generate quiz
        btnGenerateQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, QuizActivity.class);
            intent.putExtra("summaryText", summaryText);
            startActivity(intent);
        });

        // Skip quiz
        btnSkipQuiz.setOnClickListener(v -> {
            Toast.makeText(SummaryActivity.this, "Quiz Skipped", Toast.LENGTH_SHORT).show();
            // Go back to dashboard or previous activity
            Intent intent = new Intent(SummaryActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
