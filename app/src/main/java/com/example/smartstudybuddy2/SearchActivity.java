package com.example.smartstudybuddy2;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    SearchView searchView;
    ListView listView;
    List<SearchResult> allResults;
    SearchResultAdapter adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);

        dbHelper = new DatabaseHelper(this);
        allResults = new ArrayList<>();

        // ✅ Load all searchable content from database
        loadAllSearchResults();

        Log.d(TAG, "Loaded " + allResults.size() + " searchable items");

        // If no results found, add placeholder
        if (allResults.isEmpty()) {
            SearchResult noResult = new SearchResult(0, SearchResult.TYPE_NOTE, 
                    "No content found", "Create recordings, notes, or flashcards to search.", "");
            allResults.add(noResult);
        }

        // Use SearchResultAdapter
        adapter = new SearchResultAdapter(this, R.layout.item_search_result, allResults);
        listView.setAdapter(adapter);

        // Set up list item click listener with error handling
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (allResults != null && position >= 0 && position < allResults.size()) {
                    SearchResult result = allResults.get(position);
                    if (result != null && result.getId() > 0) {
                        handleResultClick(result);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling result click: " + e.getMessage());
                Toast.makeText(SearchActivity.this, "Error opening result", Toast.LENGTH_SHORT).show();
            }
        });

        // ---- MAKE HINT COLOR + TEXT COLOR GREY ----
        int textId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = searchView.findViewById(textId);
        if (searchText != null) {
            searchText.setHintTextColor(Color.GRAY);
            searchText.setTextColor(Color.GRAY);
        }

        // ---- SEARCH ICON GREY ----
        int searchIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = searchView.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        // ---- CLOSE ICON GREY ----
        int closeIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = searchView.findViewById(closeIconId);
        if (closeIcon != null) {
            closeIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        // Filter on typing
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    /**
     * Load all searchable content from database
     * ✅ Searches: Recordings (with transcriptions), Notes, and Flashcards
     */
    private void loadAllSearchResults() {
        try {
            // 1. Load RECORDINGS with transcriptions
            Log.d(TAG, "Loading recordings...");
            ArrayList<Recording> recordings = dbHelper.getAllRecordings();
            if (recordings != null) {
                for (Recording recording : recordings) {
                    try {
                        String preview = recording.getTranscription() != null && !recording.getTranscription().isEmpty()
                                ? recording.getTranscription().substring(0, Math.min(100, recording.getTranscription().length()))
                                : "No transcription yet";
                        
                        SearchResult result = new SearchResult(
                            recording.getId(),
                            SearchResult.TYPE_RECORDING,
                            recording.getTitle() != null ? recording.getTitle() : "Untitled",
                            preview,
                            recording.getDate() != null ? recording.getDate() : ""
                        );
                        allResults.add(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing recording: " + e.getMessage());
                    }
                }
                Log.d(TAG, "✅ Loaded " + recordings.size() + " recordings");
            }

            // 2. Load NOTES
            Log.d(TAG, "Loading notes...");
            Cursor noteCursor = dbHelper.getAllNotes();
            if (noteCursor != null && noteCursor.moveToFirst()) {
                do {
                    try {
                        int id = noteCursor.getInt(noteCursor.getColumnIndexOrThrow("id"));
                        String title = noteCursor.getString(noteCursor.getColumnIndexOrThrow("title"));
                        String content = noteCursor.getString(noteCursor.getColumnIndexOrThrow("content"));
                        
                        String preview = content != null && !content.isEmpty()
                                ? content.substring(0, Math.min(100, content.length()))
                                : "No content";
                        
                        SearchResult result = new SearchResult(
                            id,
                            SearchResult.TYPE_NOTE,
                            title != null ? title : "Untitled",
                            preview,
                            ""  // Notes don't have timestamps in current schema
                        );
                        allResults.add(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing note: " + e.getMessage());
                    }
                } while (noteCursor.moveToNext());
                noteCursor.close();
                Log.d(TAG, "✅ Loaded notes");
            }

            // 3. Load FLASHCARDS
            Log.d(TAG, "Loading flashcards...");
            ArrayList<Flashcard> flashcards = dbHelper.getAllFlashcardsAsArray();
            if (flashcards != null) {
                for (Flashcard flashcard : flashcards) {
                    try {
                        SearchResult result = new SearchResult(
                            flashcard.getId(),
                            SearchResult.TYPE_FLASHCARD,
                            flashcard.getQuestion() != null ? flashcard.getQuestion() : "No question",
                            "Answer: " + (flashcard.getAnswer() != null ? flashcard.getAnswer() : "No answer"),
                            flashcard.getTopic() != null ? flashcard.getTopic() : "General"
                        );
                        allResults.add(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing flashcard: " + e.getMessage());
                    }
                }
                Log.d(TAG, "✅ Loaded " + flashcards.size() + " flashcards");
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error loading search results: " + e.getMessage());
        }
    }

    /**
     * Handle click on search result - navigate to appropriate screen
     */
    private void handleResultClick(SearchResult result) {
        try {
            Log.d(TAG, "Result clicked: ID=" + result.getId() + ", Type=" + result.getType());

            switch (result.getType()) {
                case SearchResult.TYPE_RECORDING:
                    // Open recording detail
                    Intent recordingIntent = new Intent(this, RecordingDetailActivity.class);
                    recordingIntent.putExtra("recording_id", result.getId());
                    startActivity(recordingIntent);
                    break;

                case SearchResult.TYPE_NOTE:
                    // Could open a note detail screen or editor here
                    // For now, just show toast
                    Log.d(TAG, "Note clicked: " + result.getTitle());
                    Toast.makeText(this, "Note: " + result.getTitle(), Toast.LENGTH_SHORT).show();
                    break;

                case SearchResult.TYPE_FLASHCARD:
                    // Could open flashcard reviewer here
                    Log.d(TAG, "Flashcard clicked: " + result.getTitle());
                    Toast.makeText(this, "Opening flashcard...", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling result click: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

