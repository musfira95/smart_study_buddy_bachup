package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {

    private static final String TAG = "RecordingListActivity";

    private ListView listView;
    private ArrayList<Recording> recordingList;
    private DatabaseHelper dbHelper;
    private boolean showOnlyBookmarked = false;
    private RecordingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        // Setup Toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView = findViewById(R.id.recordingListView);
        dbHelper = new DatabaseHelper(this);

        // Setup filter buttons
        Button btnAllRecordings = findViewById(R.id.btnAllRecordings);
        Button btnBookmarkedOnly = findViewById(R.id.btnBookmarkedOnly);

        btnAllRecordings.setOnClickListener(v -> {
            showOnlyBookmarked = false;
            btnAllRecordings.setBackgroundResource(R.drawable.button_rounded);
            btnBookmarkedOnly.setBackgroundResource(android.R.drawable.btn_default);
            loadRecordings();
            Log.d(TAG, "✅ Filter: All recordings");
        });

        btnBookmarkedOnly.setOnClickListener(v -> {
            showOnlyBookmarked = true;
            btnBookmarkedOnly.setBackgroundResource(R.drawable.button_rounded);
            btnAllRecordings.setBackgroundResource(android.R.drawable.btn_default);
            loadRecordings();
            Log.d(TAG, "⭐ Filter: Bookmarks only");
        });

        // ✅ COMPREHENSIVE cleanup of ALL dummy data
        Log.d(TAG, "🗑️ Cleaning up ALL dummy/test recordings...");
        dbHelper.cleanupAllDummyData();
        dbHelper.deleteDummyRecordings();

        loadRecordings(); // Load real recordings from database
    }



    private void loadRecordings() {
        if (showOnlyBookmarked) {
            Log.d(TAG, "⭐ Loading BOOKMARKED recordings only");
            recordingList = dbHelper.getBookmarkedRecordings();
        } else {
            Log.d(TAG, "📥 Loading ALL recordings using: SELECT * FROM transcriptions ORDER BY timestamp DESC");
            recordingList = dbHelper.getAllRecordings();
        }

        if (recordingList == null) {
            recordingList = new ArrayList<>();
            Log.w(TAG, "⚠️ getAllRecordings returned null");
        }

        Log.d(TAG, "✅ Total recordings fetched from database: " + recordingList.size());

        // Log detailed info about each recording
        for (int i = 0; i < recordingList.size(); i++) {
            Recording r = recordingList.get(i);
            String transcriptionStatus = (r.getTranscription() != null && !r.getTranscription().isEmpty()) 
                ? "✅ Yes (" + r.getTranscription().length() + " chars)"
                : "⏳ Pending";
            Log.d(TAG, "[" + i + "] ID=" + r.getId() +
                    " | Title=" + r.getTitle() +
                    " | Date=" + r.getDate() +
                    " | Transcription=" + transcriptionStatus);
        }

        if (recordingList.isEmpty()) {
            if (showOnlyBookmarked) {
                Log.i(TAG, "ℹ️ No bookmarked recordings found");
                Toast.makeText(this, "No bookmarked recordings yet!", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "ℹ️ No recordings found in database - showing empty state");
                Toast.makeText(this, "No recordings found in database yet!", Toast.LENGTH_SHORT).show();
            }
        } else {
            String filterInfo = showOnlyBookmarked ? " (BOOKMARKED)" : "";
            Log.i(TAG, "✅ Displaying " + recordingList.size() + " recordings" + filterInfo);
        }

        // ✔ FIXED – Load adapter with filtered data
        adapter = new RecordingListAdapter(this, recordingList);
        listView.setAdapter(adapter);

        // ✔ Clicking an item opens detail screen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Recording selectedRecording = recordingList.get(position);

            Log.d(TAG, "Recording clicked: ID=" + selectedRecording.getId() + ", Title=" + selectedRecording.getTitle());

            Intent intent = new Intent(RecordingListActivity.this, RecordingDetailActivity.class);
            intent.putExtra("recording_id", selectedRecording.getId());
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from other activities (bookmark state may have changed)
        loadRecordings();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        Log.d(TAG, "📄 onResume: Refreshed recording list");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
