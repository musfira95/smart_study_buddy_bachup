package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    private CardView uploadAudioBtn, btnLastTranscription, btnRecordingList, btnTimetable;
    private CardView exportPdfBtn;
    private CardView btnHistoryTracking;
    private LinearLayout logoutBtn;

    // UPDATED VIEWS
    private LinearLayout searchBar;
    private ImageView navHome, navAnalytics, navSettings, navProfile;

    // 🔔 NEW: Notification Icon
    private ImageView btnNotification;

    private SessionManager session;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // CardViews
        uploadAudioBtn = findViewById(R.id.uploadAudioBtn);
        btnLastTranscription = findViewById(R.id.btnLastTranscription);
        btnRecordingList = findViewById(R.id.btnRecordingList);
        btnTimetable = findViewById(R.id.btnTimetable);
        exportPdfBtn = findViewById(R.id.btnBookmarks);
        btnHistoryTracking = findViewById(R.id.btnHistoryTracking);
        CardView btnFlashcards = findViewById(R.id.btnFlashcards);

        // Search bar
        searchBar = findViewById(R.id.searchBar);

        // Bottom navigation icons
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        // 🔔 Notification Icon
        btnNotification = findViewById(R.id.btnNotification);

        // Logout
        logoutBtn = findViewById(R.id.logoutBtn);

        // Upload Audio
        uploadAudioBtn.setOnClickListener(v ->
                startActivity(new Intent(this, UploadAudioActivity.class))
        );

        // Recording List
        btnRecordingList.setOnClickListener(v ->
                startActivity(new Intent(this, RecordingListActivity.class))
        );

        // Last Transcription
        btnLastTranscription.setOnClickListener(v -> {
            Recording lastRecording = dbHelper.getLastRecording();

            if (lastRecording == null) {
                Toast.makeText(this, "No transcription available yet!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, LastTranscriptionActivity.class);
            intent.putExtra("LAST_TITLE", lastRecording.getTitle());
            intent.putExtra("LAST_TEXT", lastRecording.getFilePath());
            intent.putExtra("LAST_TIME", lastRecording.getDate());
            startActivity(intent);
        });

        // History Tracking
        btnHistoryTracking.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

        // Flashcards
        if (btnFlashcards != null) {
            btnFlashcards.setOnClickListener(v ->
                    startActivity(new Intent(this, FlashcardActivity.class))
            );
        }

        // Search
        searchBar.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class))
        );

        // Timetable → Reminders
        btnTimetable.setOnClickListener(v ->
                startActivity(new Intent(this, Reminders.class))
        );


        // Export PDF
        exportPdfBtn.setOnClickListener(v -> {
                    Toast.makeText(this, "This is a dummy text for export PDF.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ExportActivity.class));
                }
        );

        // 🔔 Notification click
        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        // Bottom Nav - Home
        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
        );

        // Bottom Nav - Analytics
        navAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        // Bottom Nav - Settings
        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSettingsActivity.class))
        );

        // Bottom Nav - Profile
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        // Logout
        logoutBtn.setOnClickListener(v -> {
            session.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
