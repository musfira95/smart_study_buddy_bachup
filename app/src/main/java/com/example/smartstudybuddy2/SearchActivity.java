package com.example.smartstudybuddy2;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    ProgressBar progressSearching;
    TextView tvResultsInfo;
    LinearLayout emptyStateContainer;
    
    List<SearchResult> allResults;
    SearchResultAdapter adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Setup Toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);
        progressSearching = findViewById(R.id.progressSearching);
        tvResultsInfo = findViewById(R.id.tvResultsInfo);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        dbHelper = new DatabaseHelper(this);
        allResults = new ArrayList<>();

        // ✅ Load all searchable content from database
        loadAllSearchResults();

        Log.d(TAG, "Loaded " + allResults.size() + " searchable items");

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

        // ---- CUSTOMIZE SEARCH VIEW COLORS ----
        customizeSearchViewColors();

        // Filter on typing with animations
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Show progress animation
                if (!newText.isEmpty()) {
                    showSearchProgress();
                } else {
                    hideSearchProgress();
                    showEmptyState();
                }
                
                adapter.getFilter().filter(newText);
                
                // Delay to show results with animation
                listView.postDelayed(() -> updateResultsDisplay(), 200);
                return false;
            }
        });
    }

    /**
     * Customize SearchView colors and styling
     */
    private void customizeSearchViewColors() {
        // ---- MAKE HINT COLOR + TEXT COLOR ----
        int textId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = searchView.findViewById(textId);
        if (searchText != null) {
            searchText.setHintTextColor(Color.parseColor("#CCCCCC"));
            searchText.setTextColor(Color.parseColor("#333333"));
            searchText.setTextSize(14);
        }

        // ---- SEARCH ICON COLOR ----
        int searchIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = searchView.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.parseColor("#9C27FF"), PorterDuff.Mode.SRC_IN);
        }

        // ---- CLOSE ICON COLOR ----
        int closeIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = searchView.findViewById(closeIconId);
        if (closeIcon != null) {
            closeIcon.setColorFilter(Color.parseColor("#9C27FF"), PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Show search progress animation
     */
    private void showSearchProgress() {
        progressSearching.setVisibility(View.VISIBLE);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(progressSearching, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.start();
    }

    /**
     * Hide search progress animation
     */
    private void hideSearchProgress() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(progressSearching, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setDuration(300);
        fadeOut.start();
    }

    /**
     * Show empty state with animation
     */
    private void showEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            AnimationSet animSet = new AnimationSet(true);
            
            AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
            alpha.setDuration(400);
            
            ScaleAnimation scale = new ScaleAnimation(0.9f, 1f, 0.9f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(400);
            
            animSet.addAnimation(alpha);
            animSet.addAnimation(scale);
            emptyStateContainer.startAnimation(animSet);
        }
    }

    /**
     * Hide empty state with animation
     */
    private void hideEmptyState() {
        if (emptyStateContainer != null) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(emptyStateContainer, "alpha", 1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.setDuration(300);
            fadeOut.start();
        }
    }

    /**
     * Update results display with animations
     */
    private void updateResultsDisplay() {
        if (adapter.getCount() > 0) {
            hideEmptyState();
            listView.setVisibility(View.VISIBLE);
            tvResultsInfo.setVisibility(View.VISIBLE);
            tvResultsInfo.setText("Found " + adapter.getCount() + " result" + 
                    (adapter.getCount() != 1 ? "s" : ""));
            
            // Animate list items appearance
            animateListItems();
        } else {
            showEmptyState();
            listView.setVisibility(View.GONE);
            tvResultsInfo.setVisibility(View.GONE);
        }
    }

    /**
     * Animate list items appearance
     */
    private void animateListItems() {
        for (int i = 0; i < listView.getChildCount(); i++) {
            View view = listView.getChildAt(i);
            if (view != null) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setStartOffset(i * 50);
                alphaAnimation.setDuration(300);
                view.startAnimation(alphaAnimation);
            }
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

