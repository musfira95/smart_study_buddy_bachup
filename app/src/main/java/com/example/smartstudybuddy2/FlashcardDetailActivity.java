package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ✅ Flashcard Detail Activity with Simple Learning Progress
 * 
 * Features:
 * - Shows learning progress: X Learning, Y Mastered
 * - Increments review count when card is viewed
 * - Two buttons: "Again" (review again), "Mastered" (mark as mastered)
 * - Updates database and recalculates counters
 * - Simple tracking without complex spaced repetition
 */
public class FlashcardDetailActivity extends AppCompatActivity {
    private static final String TAG = "FlashcardDetail";
    
    private TextView tvProgress, tvQuestion, tvAnswer;
    private Button btnAgain, btnMastered;
    private DatabaseHelper dbHelper;
    
    private int flashcardId;
    private int totalCards = 0;
    private String question, answer, topic;
    private int reviewCount = 0;
    private boolean isMastered = false;
    private int masteredCount = 0;  // Track mastered count locally

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        
        // Get passed data
        flashcardId = getIntent().getIntExtra("flashcard_id", -1);
        question = getIntent().getStringExtra("question");
        answer = getIntent().getStringExtra("answer");
        topic = getIntent().getStringExtra("topic");
        totalCards = getIntent().getIntExtra("total_cards", 0);
        reviewCount = getIntent().getIntExtra("review_count", 0);
        isMastered = getIntent().getBooleanExtra("is_mastered", false);
        masteredCount = getIntent().getIntExtra("mastered_count", 0);

        // Initialize UI
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnAgain = findViewById(R.id.btnAgain);
        btnMastered = findViewById(R.id.btnMastered);

        // Set content
        if (tvQuestion != null) tvQuestion.setText("Q: " + (question != null ? question : ""));
        if (tvAnswer != null) tvAnswer.setText("A: " + (answer != null ? answer : ""));
        
        // ✅ Increment review count when card is shown (first view)
        if (reviewCount == 0) {
            reviewCount = 1;
            Log.d(TAG, "📖 Card shown - First time, setting review count to 1");
        }
        
        // Update UI
        updateProgressDisplay();
        
        // Setup buttons
        setupButtons();
        
        // Back button handler
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Log.d(TAG, "✅ FlashcardDetailActivity opened for card ID: " + flashcardId);
    }

    private void updateProgressDisplay() {
        if (tvProgress == null) return;
        
        int learningCount = totalCards - masteredCount;
        String progressText = "📚 Learning: " + learningCount + " | ✅ Mastered: " + masteredCount;
        tvProgress.setText(progressText);
        
        Log.d(TAG, "📊 Progress: " + progressText);
    }

    private void setupButtons() {
        if (btnAgain == null || btnMastered == null) {
            Log.e(TAG, "❌ Buttons not found in layout");
            return;
        }
        
        // ✅ "Again" button - increment review count, update database, stay on screen
        btnAgain.setOnClickListener(v -> {
            reviewCount++;
            Log.d(TAG, "🔄 'Again' pressed - Review count incremented to: " + reviewCount);
            
            // Update database
            updateCardInDatabase();
            
            // Update UI
            updateProgressDisplay();
            
            Toast.makeText(this, "📝 Review count: " + reviewCount, Toast.LENGTH_SHORT).show();
        });
        
        // ✅ "Mastered" button - mark as mastered, update database, recalculate counters
        btnMastered.setOnClickListener(v -> {
            isMastered = true;
            masteredCount++;  // Increment mastered count
            Log.d(TAG, "✅ Card marked as mastered");
            
            // Update database
            updateCardInDatabase();
            
            // Update UI
            updateProgressDisplay();
            
            Toast.makeText(this, "🎉 Card mastered! (" + masteredCount + "/" + totalCards + ")", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * ✅ NEW: Update card in database with current state
     */
    private void updateCardInDatabase() {
        if (flashcardId == -1 || dbHelper == null) {
            Log.w(TAG, "⚠️ Cannot update database: flashcard_id=" + flashcardId);
            return;
        }
        
        try {
            // Determine mastery level: 5 if mastered, 1 if still learning
            int masteryLevel = isMastered ? 5 : 1;
            
            // Use recordFlashcardReview which handles both insert and update
            dbHelper.recordFlashcardReview(flashcardId, masteryLevel);
            
            Log.d(TAG, "📝 Updated FlashcardStat for card " + flashcardId + " - reviewCount: " + reviewCount + ", mastery: " + masteryLevel);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating database: " + e.getMessage());
            Toast.makeText(this, "⚠️ Error saving changes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
