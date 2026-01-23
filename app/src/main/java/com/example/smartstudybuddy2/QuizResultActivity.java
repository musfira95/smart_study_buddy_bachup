package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends AppCompatActivity {

    TextView tvScoreTitle, tvScore, tvCorrect, tvWrong;
    LinearLayout btnBack; // Changed from Button to LinearLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Connect XML views
        tvScoreTitle = findViewById(R.id.tvScoreTitle);
        tvScore = findViewById(R.id.tvScore);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvWrong = findViewById(R.id.tvWrong);
        btnBack = findViewById(R.id.btnBackToDashboard); // LinearLayout

        // Get score values from intent
        int correct = getIntent().getIntExtra("correctCount", 0);
        int wrong = getIntent().getIntExtra("wrongCount", 0);
        int total = correct + wrong;

        // Set text dynamically
        tvScore.setText("Score: " + correct + "/" + total);
        tvCorrect.setText("Correct Answers: " + correct);
        tvWrong.setText("Wrong Answers: " + wrong);

        // Back button action
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, DashboardActivity.class);
            // Clear previous activities so user doesn't return to summary/quiz by back press
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish current QuizResultActivity
        });
    }
}
