package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;

public class AllTranscriptionsActivity extends AppCompatActivity {

    private static final String TAG = "AllTranscriptionsActivity";

    private RecyclerView rvTranscriptions;
    private DatabaseHelper db;
    private ArrayList<Recording> allTranscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transcriptions);

        rvTranscriptions = findViewById(R.id.rvTranscriptions);
        db = new DatabaseHelper(this);

        // ✅ REMOVE ALL DUMMY DATA
        Log.d(TAG, "🗑️ Cleaning up ALL dummy/test recordings...");
        db.cleanupAllDummyData();
        db.deleteDummyRecordings();

        // ✅ Fetch all transcriptions using: SELECT * FROM audio_files ORDER BY timestamp DESC
        Log.d(TAG, "📥 Fetching all transcriptions from database...");
        Log.d(TAG, "   Query: SELECT * FROM audio_files ORDER BY timestamp DESC");
        allTranscriptions = db.getAllRecordings();  // Already orders by timestamp DESC

        Log.d(TAG, "✅ Total transcriptions fetched: " + (allTranscriptions != null ? allTranscriptions.size() : "null"));

        if (allTranscriptions != null) {
            for (int i = 0; i < allTranscriptions.size(); i++) {
                Recording r = allTranscriptions.get(i);
                boolean hasTranscription = r.getTranscription() != null && !r.getTranscription().isEmpty();
                Log.d(TAG, "[" + i + "] ID=" + r.getId() + ", Title=" + r.getTitle() + 
                    ", Date=" + r.getDate() + ", Transcription=" + (hasTranscription ? "✅ Yes" : "⏳ Pending"));
            }
        }

        TranscriptionAdapter adapter = new TranscriptionAdapter(this, allTranscriptions, recording -> {
            // Item click: Detail screen
            Log.d(TAG, "✅ Transcription clicked: ID=" + recording.getId() + ", Title=" + recording.getTitle());
        });

        rvTranscriptions.setLayoutManager(new LinearLayoutManager(this));
        rvTranscriptions.setAdapter(adapter);
    }
}




