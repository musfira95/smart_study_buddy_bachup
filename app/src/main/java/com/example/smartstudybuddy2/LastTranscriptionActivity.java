package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class LastTranscriptionActivity extends AppCompatActivity {

    private EditText etTitle;
    private TextView tvText, tvTime;
    private LinearLayout btnAllTranscripts;

    // ✅ Bottom Nav Icons (ADDED)
    private ImageView navHome, navAnalytics, navSettings, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_transcription);
        Toast.makeText(this, "THIS IS LAST TRANSCRIPTION ACTIVITY", Toast.LENGTH_LONG).show();

        // FIND VIEWS
        etTitle = findViewById(R.id.etLastTitle);
        tvText = findViewById(R.id.tvLastText);
        tvTime = findViewById(R.id.tvLastTime);
        btnAllTranscripts = findViewById(R.id.btnAllTranscripts);

        // ✅ FIND BOTTOM NAV VIEWS (ADDED)
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        // GET DATA SAFELY
        String title = getIntent().getStringExtra("LAST_TITLE");
        String text = getIntent().getStringExtra("LAST_TEXT");
        String time = getIntent().getStringExtra("LAST_TIME");

        if (title == null || title.trim().isEmpty()) title = "Untitled";
        if (text == null) text = "";
        if (time == null) time = "";

        // SET UI VALUES
        etTitle.setText(title);
        tvText.setText(text);
        tvTime.setText("Saved On: " + time);

        // OPEN ALL TRANSCRIPTIONS
        btnAllTranscripts.setOnClickListener(v ->
                startActivity(new Intent(this, AllTranscriptionsActivity.class))
        );

        // AUTO-SAVE TITLE WHEN FOCUS LOST
        etTitle.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String updatedTitle = etTitle.getText().toString().trim();
                if (updatedTitle.isEmpty()) updatedTitle = "Untitled";
                etTitle.setText(updatedTitle);
                Toast.makeText(this, "Title updated", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ BOTTOM NAV CLICK ACTIONS (ADDED ONLY)
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
}
