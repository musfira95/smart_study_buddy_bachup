package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartstudybuddy2.utils.NotificationManager;

public class QuizResultActivity extends BaseActivity {

    private static final String TAG = "QuizResultActivity";

    TextView tvScoreTitle, tvScore, tvCorrect, tvWrong;
    LinearLayout btnBackToDashboard;
    android.widget.ImageView btnBackArrow;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Connect XML views
        tvScoreTitle = findViewById(R.id.tvScoreTitle);
        tvScore = findViewById(R.id.tvScore);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvWrong = findViewById(R.id.tvWrong);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        btnBackArrow = findViewById(R.id.btnBack);
        
        dbHelper = new DatabaseHelper(this);

        // Get score values from intent
        int correct = getIntent().getIntExtra("correctCount", 0);
        int wrong = getIntent().getIntExtra("wrongCount", 0);
        int durationSeconds = getIntent().getIntExtra("durationSeconds", 0);  // ✅ Get duration
        String category = getIntent().getStringExtra("category");  // ✅ Get category
        
        if (category == null) {
            category = "General";
        }
        
        int total = correct + wrong;
        double scorePercentage = total > 0 ? (correct * 100.0) / total : 0;

        // Set text dynamically
        tvScore.setText("Score: " + correct + "/" + total);
        tvCorrect.setText("Correct Answers: " + correct);
        tvWrong.setText("Wrong Answers: " + wrong);

        // ✅ SAVE QUIZ RESULT TO DATABASE
        boolean saved = dbHelper.insertQuizResult(category, correct, wrong, durationSeconds);
        if (saved) {
            Log.d(TAG, "✅ Quiz result saved to database");
            Toast.makeText(this, "📊 Results saved!", Toast.LENGTH_SHORT).show();
            
            // ✅ SMART NOTIFICATION: Alert if score is low
            if (scorePercentage < 60) {
                NotificationManager.notifyWeakTopic(QuizResultActivity.this, category, scorePercentage);
                Toast.makeText(this, "⚠️ Your score is low. Consider reviewing this topic.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "❌ Failed to save quiz result");
        }

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
