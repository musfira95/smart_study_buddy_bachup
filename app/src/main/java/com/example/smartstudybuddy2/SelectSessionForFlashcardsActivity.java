package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * ✅ Select Session For Flashcards Activity
 * 
 * Displays all study sessions from SQLite database.
 * User selects a session to create or review flashcards for that session.
 * 
 * Features:
 * - Fetch all study sessions from database
 * - Display in RecyclerView with Title and Created Date
 * - Click handler to pass session ID to FlashcardActivity
 * - Clean architecture - no modification to History feature
 */
public class SelectSessionForFlashcardsActivity extends AppCompatActivity {
    private static final String TAG = "SelectSessionFlash";
    
    private RecyclerView rvSessions;
    private SessionForFlashcardAdapter adapter;
    private DatabaseHelper dbHelper;
    private ArrayList<StudySession> sessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_session_for_flashcards);

        rvSessions = findViewById(R.id.rvSessions);
        dbHelper = new DatabaseHelper(this);
        sessionList = new ArrayList<>();

        // Setup RecyclerView
        if (rvSessions != null) {
            rvSessions.setLayoutManager(new LinearLayoutManager(this));
        }

        // Load all study sessions
        loadStudySessions();
        
        // ✅ Back button handler
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Log.d(TAG, "✅ SelectSessionForFlashcardsActivity initialized");
    }

    /**
     * Fetch all flashcard-able content from database:
     * 1. All study sessions (complete processing)
     * 2. All recordings with transcriptions (can create flashcards from these)
     */
    private void loadStudySessions() {
        try {
            sessionList = new ArrayList<>();
            
            // Load from study_sessions table (primary source)
            ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();
            if (sessions != null && !sessions.isEmpty()) {
                sessionList.addAll(sessions);
                Log.d(TAG, "✅ Loaded " + sessions.size() + " study sessions");
            }
            
            // ALSO load recordings with transcriptions (secondary source)
            // These are audio uploads that have been transcribed by FastAPI
            ArrayList<Recording> recordings = dbHelper.getAllRecordings();
            if (recordings != null && !recordings.isEmpty()) {
                int recordingsWithTranscription = 0;
                
                for (Recording rec : recordings) {
                    // Check if recording has transcription
                    if (rec.getTranscription() != null && !rec.getTranscription().trim().isEmpty()) {
                        // Create a StudySession from this recording for flashcard purposes
                        StudySession sessionFromRecording = new StudySession(
                                rec.getId(),
                                rec.getTitle() != null ? rec.getTitle() : "Recording",
                                rec.getFilePath() != null ? rec.getFilePath() : "",
                                rec.getTranscription(),  // Use transcription
                                "",  // No summary yet
                                "[]",  // No quiz yet
                                rec.getDate(),
                                0,  // Duration unknown
                                0,  // Word count will be calculated
                                0.0,  // No quiz score
                                false  // PDF not generated
                        );
                        sessionList.add(sessionFromRecording);
                        recordingsWithTranscription++;
                    }
                }
                
                Log.d(TAG, "✅ Loaded " + recordingsWithTranscription + " recordings with transcriptions");
            }
            
            Log.d(TAG, "📊 Total available for flashcards: " + sessionList.size());
            
            if (sessionList.isEmpty()) {
                Log.w(TAG, "⚠️ No study sessions or transcribed recordings found in database");
                Toast.makeText(this, "📚 No transcribed sessions available.\n\nTo create flashcards:\n1. Go to Dashboard\n2. Click 'Record Audio' or 'Upload'\n3. Wait for transcription & processing\n4. Come back here", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Log.d(TAG, "✅ Loaded " + sessionList.size() + " total sessions for flashcards");

            // Create adapter with click listener
            adapter = new SessionForFlashcardAdapter(sessionList, session -> {
                onSessionSelected(session);
            });

            rvSessions.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading study sessions: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle session selection - Pass session ID to FlashcardActivity
     */
    private void onSessionSelected(StudySession session) {
        try {
            Log.d(TAG, "🎯 Selected session: " + session.getTitle() + " (ID: " + session.getId() + ")");

            // Launch FlashcardActivity with this session's ID
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("session_id", session.getId());
            intent.putExtra("session_title", session.getTitle());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error launching FlashcardActivity: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning
        loadStudySessions();
    }
}
