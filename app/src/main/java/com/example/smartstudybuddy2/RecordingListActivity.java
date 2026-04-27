package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {

    private static final String TAG = "RecordingListActivity";

    private ListView listView;
    private ArrayList<Recording> recordingList;
    private DatabaseHelper dbHelper;
    private boolean showOnlyBookmarked = false;
    private RecordingListAdapter adapter;
    private Chip chipAll, chipBookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        // Hide default action bar to prevent duplicate headers
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        listView = findViewById(R.id.recordingListView);
        dbHelper = new DatabaseHelper(this);

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Setup filter chips
        chipAll = findViewById(R.id.chipAll);
        chipBookmarks = findViewById(R.id.chipBookmarks);

        chipAll.setOnClickListener(v -> {
            showOnlyBookmarked = false;
            chipAll.setChecked(true);
            chipBookmarks.setChecked(false);
            loadRecordings();
            Log.d(TAG, "✅ Filter: All recordings");
        });

        chipBookmarks.setOnClickListener(v -> {
            showOnlyBookmarked = true;
            chipBookmarks.setChecked(true);
            chipAll.setChecked(false);
            loadRecordings();
            Log.d(TAG, "⭐ Filter: Bookmarks only");
        });

        // Set initial state
        chipAll.setChecked(true);
        chipBookmarks.setChecked(false);

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
