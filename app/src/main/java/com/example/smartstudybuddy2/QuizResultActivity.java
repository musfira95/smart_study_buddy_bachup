package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends BaseActivity {

    TextView tvScoreTitle, tvScore, tvCorrect, tvWrong;
    LinearLayout btnBackToDashboard; // Renamed to avoid name conflict with btnBack arrow
    android.widget.ImageView btnBackArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Connect XML views
        tvScoreTitle = findViewById(R.id.tvScoreTitle);
        tvScore = findViewById(R.id.tvScore);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvWrong = findViewById(R.id.tvWrong);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard); // LinearLayout
        btnBackArrow = findViewById(R.id.btnBack);

        // Get score values from intent
        int correct = getIntent().getIntExtra("correctCount", 0);
        int wrong = getIntent().getIntExtra("wrongCount", 0);
        int total = correct + wrong;

        // Set text dynamically
        tvScore.setText("Score: " + correct + "/" + total);
        tvCorrect.setText("Correct Answers: " + correct);
        tvWrong.setText("Wrong Answers: " + wrong);

        // Back to Dashboard button action
        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, DashboardActivity.class);
            // Clear previous activities so user doesn't return to summary/quiz by back press
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish current QuizResultActivity
        });
        
        // Manual Back Arrow Logic
        if (btnBackArrow != null) {
            btnBackArrow.setOnClickListener(v -> finish());
        }
    }
}
