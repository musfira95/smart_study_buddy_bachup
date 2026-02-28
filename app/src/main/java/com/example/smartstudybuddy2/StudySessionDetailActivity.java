package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * StudySessionDetailActivity - Display full session details
 * Shows: Title, Transcription, Summary, Quiz Questions & Answers
 * Allows updating quiz score and deleting session
 */
public class StudySessionDetailActivity extends AppCompatActivity {

    private static final String TAG = "SessionDetailActivity";

    private TextView tvSessionTitle, tvCreatedDate, tvDuration, tvWordCount;
    private TextView tvTranscriptionLabel, tvTranscription;
    private TextView tvSummaryLabel, tvSummary;
    private TextView tvQuizLabel, tvQuizContent;
    private Button btnUpdateScore, btnDelete, btnBack;
    private ScrollView scrollView;

    private DatabaseHelper dbHelper;
    private StudySession session;
    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_session_detail);

        try {
            initializeViews();
            dbHelper = new DatabaseHelper(this);

            // Get session ID from intent
            sessionId = getIntent().getIntExtra("session_id", -1);
            if (sessionId == -1) {
                Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Load session from database
            session = dbHelper.getStudySessionById(sessionId);
            if (session == null) {
                Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Display session details
            displaySessionDetails();

            // Setup button listeners
            setupButtonListeners();

            Log.d(TAG, "✅ Session details loaded: " + session.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error loading session details", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialize all UI views
     */
    private void initializeViews() {
        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvDuration = findViewById(R.id.tvDuration);
        tvWordCount = findViewById(R.id.tvWordCount);

        tvTranscriptionLabel = findViewById(R.id.tvTranscriptionLabel);
        tvTranscription = findViewById(R.id.tvTranscription);

        tvSummaryLabel = findViewById(R.id.tvSummaryLabel);
        tvSummary = findViewById(R.id.tvSummary);

        tvQuizLabel = findViewById(R.id.tvQuizLabel);
        tvQuizContent = findViewById(R.id.tvQuizContent);

        btnUpdateScore = findViewById(R.id.btnUpdateScore);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        scrollView = findViewById(R.id.scrollView);
    }

    /**
     * Display all session details in UI
     */
    private void displaySessionDetails() {
        // Header info
        tvSessionTitle.setText(session.getTitle());
        tvCreatedDate.setText("📅 " + session.getCreatedDate());
        tvDuration.setText("⏱ Duration: " + formatDuration(session.getDuration()));
        tvWordCount.setText("📝 Words: " + session.getWordCount());

        // Transcription
        String transcription = session.getTranscription();
        if (transcription != null && !transcription.trim().isEmpty()) {
            tvTranscriptionLabel.setText("📋 Transcription");
            tvTranscription.setText(transcription);
        } else {
            tvTranscriptionLabel.setText("📋 Transcription (Not available)");
            tvTranscription.setText("");
        }

        // Summary
        String summary = session.getSummary();
        if (summary != null && !summary.trim().isEmpty()) {
            tvSummaryLabel.setText("📌 Summary");
            tvSummary.setText(summary);
        } else {
            tvSummaryLabel.setText("📌 Summary (Not available)");
            tvSummary.setText("");
        }

        // Quiz Results
        String quizJson = session.getQuizJson();
        if (quizJson != null && !quizJson.trim().isEmpty() && !quizJson.equals("[]")) {
            tvQuizLabel.setText("❓ Quiz Results (Score: " + String.format("%.1f", session.getQuizScore()) + "%)");
            displayQuizQuestions(quizJson);
        } else {
            tvQuizLabel.setText("❓ Quiz (No questions)");
            tvQuizContent.setText("No quiz questions generated for this session");
        }
    }

    /**
     * Parse and display quiz questions from JSON
     */
    private void displayQuizQuestions(String quizJson) {
        try {
            StringBuilder quizText = new StringBuilder();
            JSONArray questions = new JSONArray(quizJson);

            for (int i = 0; i < questions.length(); i++) {
                JSONObject q = questions.getJSONObject(i);
                quizText.append("\n✓ Q").append(i + 1).append(": ").append(q.optString("question", "N/A")).append("\n");
                quizText.append("  Answer: ").append(q.optString("answer", "N/A")).append("\n");
            }

            tvQuizContent.setText(quizText.toString().trim());
        } catch (JSONException e) {
            Log.e(TAG, "❌ Error parsing quiz JSON: " + e.getMessage());
            tvQuizContent.setText("Error parsing quiz data");
        }
    }

    /**
     * Format duration from seconds to readable string
     */
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    /**
     * Setup button click listeners
     */
    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnUpdateScore.setOnClickListener(v -> {
            // Show score update dialog
            showScoreDialog();
        });

        btnDelete.setOnClickListener(v -> {
            // Show delete confirmation dialog
            showDeleteDialog();
        });
    }

    /**
     * Show dialog to update quiz score
     */
    private void showScoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Quiz Score")
                .setMessage("Enter new quiz score (0-100):")
                .setNegativeButton("Cancel", null);

        // Create input field
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf((int) session.getQuizScore()));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            try {
                double newScore = Double.parseDouble(input.getText().toString());
                if (newScore < 0 || newScore > 100) {
                    Toast.makeText(this, "Score must be between 0-100", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update in database
                if (dbHelper.updateStudySession(sessionId, newScore, session.isPdfGenerated())) {
                    session.setQuizScore(newScore);
                    tvQuizLabel.setText("❓ Quiz Results (Score: " + String.format("%.1f", newScore) + "%)");
                    Toast.makeText(this, "Score updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Score updated: " + newScore);
                } else {
                    Toast.makeText(this, "Failed to update score", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid score format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Session")
                .setMessage("Are you sure you want to delete this session? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteStudySession(sessionId)) {
                        Toast.makeText(this, "Session deleted successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "✅ Session deleted: " + sessionId);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete session", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }
}
