package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LastTranscriptionActivity extends AppCompatActivity {

    private static final String TAG = "LastTranscriptionActivity";

    private EditText etTitle;
    private TextView tvText, tvTime;
    private LinearLayout btnAllTranscripts;
    private DatabaseHelper dbHelper;

    // ✅ Bottom Nav Icons
    private ImageView navHome, navAnalytics, navSettings, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_transcription);

        // Setup Toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Log.d(TAG, "LastTranscriptionActivity created");

        // FIND VIEWS
        etTitle = findViewById(R.id.etLastTitle);
        tvText = findViewById(R.id.tvLastText);
        tvTime = findViewById(R.id.tvLastTime);
        btnAllTranscripts = findViewById(R.id.btnAllTranscripts);

        // ✅ FIND BOTTOM NAV VIEWS
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // ✅ COMPREHENSIVE cleanup of ALL dummy data
        Log.d(TAG, "🗑️ Cleaning up ALL dummy/test recordings...");
        dbHelper.cleanupAllDummyData();
        dbHelper.deleteDummyRecordings();

        // ✅ GET LATEST RECORDING DYNAMICALLY FROM DATABASE (ORDER BY timestamp DESC LIMIT 1)
        Log.d(TAG, "📥 Fetching latest recording from database using: SELECT * FROM audio_files ORDER BY timestamp DESC LIMIT 1");
        Recording latestRecording = dbHelper.getLastRecording();

        if (latestRecording != null) {
            String title = latestRecording.getTitle();
            String transcription = latestRecording.getTranscription(); // ✅ Get transcription from database
            String time = latestRecording.getDate();

            Log.d(TAG, "✅ Latest recording found");
            Log.d(TAG, "   ID: " + latestRecording.getId());
            Log.d(TAG, "   Title: " + title);
            Log.d(TAG, "   Date: " + time);
            Log.d(TAG, "   Transcription available: " + (transcription != null && !transcription.isEmpty()));
            if (transcription != null && !transcription.isEmpty()) {
                Log.d(TAG, "   Transcription length: " + transcription.length() + " chars");
            }

            // Display transcription only if it exists and is not empty
            String text;
            if (transcription != null && !transcription.isEmpty()) {
                text = transcription;
                Log.i(TAG, "🎯 Displaying transcription from database");
            } else {
                text = "Recording: " + latestRecording.getFilePath();
                Log.i(TAG, "⏳ No transcription yet, showing file path");
            }

            // SET UI VALUES FROM DATABASE
            etTitle.setText(title != null ? title : "Untitled");
            tvText.setText(text);
            tvTime.setText("Saved On: " + time);

            Log.i(TAG, "✅ Latest transcription loaded successfully from database");
            Toast.makeText(this, "✅ Latest transcription loaded from database", Toast.LENGTH_SHORT).show();
        } else {
            // No recordings in database
            Log.w(TAG, "⚠️ No recordings found in database");
            etTitle.setText("Untitled");
            tvText.setText("❌ No transcriptions available yet. Create a new recording to get started.");
            tvTime.setText("Saved On: N/A");
            Toast.makeText(this, "No recordings found", Toast.LENGTH_SHORT).show();
        }

        // OPEN ALL TRANSCRIPTIONS
        btnAllTranscripts.setOnClickListener(v -> {
            Log.d(TAG, "Opening AllTranscriptionsActivity");
            startActivity(new Intent(this, AllTranscriptionsActivity.class));
        });

        // AUTO-SAVE TITLE WHEN FOCUS LOST
        etTitle.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String updatedTitle = etTitle.getText().toString().trim();
                if (updatedTitle.isEmpty()) updatedTitle = "Untitled";
                etTitle.setText(updatedTitle);
                Toast.makeText(this, "Title updated", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ BOTTOM NAV CLICK ACTIONS
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        navAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSettingsActivity.class))
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
