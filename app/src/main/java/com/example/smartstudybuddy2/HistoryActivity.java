package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * HistoryActivity - Display all Study Sessions
 * Shows transcriptions, summaries, quizzes completed
 * Allows viewing details and deleting sessions
 */
public class HistoryActivity extends AppCompatActivity implements
        HistoryAdapter.OnItemClickListener,
        HistoryAdapter.OnDeleteClickListener {

    private static final String TAG = "HistoryActivity";
    
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private ArrayList<Object> historyList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_history);

            // Setup Toolbar with back button
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            rvHistory = findViewById(R.id.rvHistory);
            if (rvHistory != null) {
                rvHistory.setLayoutManager(new LinearLayoutManager(this));
            }

            historyList = new ArrayList<>();
            dbHelper = new DatabaseHelper(this);

            // Load all data types from database
            loadHistoryFromDatabase();

            // Sort: latest date first
            sortHistoryByDate();

            Log.d(TAG, "✅ Total history items loaded: " + historyList.size());

            // Setup adapter with listeners
            adapter = new HistoryAdapter(this, historyList);
            adapter.setClickListener(this);
            adapter.setDeleteListener(this);
            
            if (rvHistory != null) {
                rvHistory.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load all history items from database
     * Includes: Recordings, Study Sessions, and Quiz Results
     */
    private void loadHistoryFromDatabase() {
        try {
            // 1. Load RECORDINGS
            Log.d(TAG, "Loading recordings...");
            try {
                ArrayList<Recording> recordings = dbHelper.getAllRecordings();
                if (recordings != null && !recordings.isEmpty()) {
                    historyList.addAll(recordings);
                    Log.d(TAG, "✅ Loaded " + recordings.size() + " recordings");
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Error loading recordings: " + e.getMessage());
            }

            // 2. Load STUDY SESSIONS (from audio processing)
            Log.d(TAG, "Loading study sessions...");
            try {
                ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();
                if (sessions != null && !sessions.isEmpty()) {
                    historyList.addAll(sessions);
                    Log.d(TAG, "✅ Loaded " + sessions.size() + " study sessions");
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Error loading study sessions: " + e.getMessage());
            }

            // 3. Load QUIZ RESULTS
            Log.d(TAG, "Loading quiz results...");
            try {
                ArrayList<QuizResult> quizResults = dbHelper.getAllQuizResults();
                if (quizResults != null && !quizResults.isEmpty()) {
                    historyList.addAll(quizResults);
                    Log.d(TAG, "✅ Loaded " + quizResults.size() + " quiz results");
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Error loading quiz results: " + e.getMessage());
            }

            // Only show error toast if NO data was loaded at all
            if (historyList.isEmpty()) {
                Log.w(TAG, "⚠️ No history data found in database");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Critical error in loadHistoryFromDatabase: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sort history by date (newest first)
     */
    private void sortHistoryByDate() {
        Collections.sort(historyList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String date1 = getDateFromItem(o1);
                String date2 = getDateFromItem(o2);
                return date2.compareTo(date1); // descending (newest first)
            }
        });
    }

    /**
     * Extract date from any history item type
     */
    private String getDateFromItem(Object item) {
        if (item instanceof Recording) {
            return ((Recording) item).getDate();
        } else if (item instanceof StudySession) {
            return ((StudySession) item).getCreatedDate();
        } else if (item instanceof QuizResult) {
            return ((QuizResult) item).getCreatedDate();
        }
        return "";
    }

    /**
     * Handle Study Session item click - Open detail view
     */
    @Override
    public void onItemClick(StudySession session) {
        Log.d(TAG, "📂 Clicked Study Session: " + session.getTitle());
        Intent intent = new Intent(this, StudySessionDetailActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    /**
     * Handle Study Session delete - Shows confirmation dialog before deleting
     */
    @Override
    public void onDeleteClick(StudySession session, int position) {
        Log.d(TAG, "🗑️ Delete requested for: " + session.getTitle());
        
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.AppDialog);
        builder.setTitle("Delete Session")
                .setMessage("Are you sure you want to delete \"" + session.getTitle() + "\"?\n\nThis action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "Delete cancelled by user");
                })
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteSessionFromDatabase(session, position);
                });
        
        builder.show();
    }

    /**
     * Delete session from database and update RecyclerView
     */
    private void deleteSessionFromDatabase(StudySession session, int position) {
        try {
            // Delete from database
            if (dbHelper.deleteStudySession(session.getId())) {
                // Remove from list immediately
                historyList.remove(position);
                
                // Notify adapter of item removal
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, historyList.size());
                
                Log.d(TAG, "✅ Session deleted: " + session.getTitle());
                Toast.makeText(this, "Session deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "❌ Failed to delete session from database");
                Toast.makeText(this, "Failed to delete session", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during deletion: " + e.getMessage());
            Toast.makeText(this, "Error deleting session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from detail view
        historyList.clear();
        loadHistoryFromDatabase();
        sortHistoryByDate();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

