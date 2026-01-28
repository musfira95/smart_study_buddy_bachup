package com.example.smartstudybuddy2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FlashcardActivity extends BaseActivity {

    private ListView listView;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        listView = findViewById(R.id.listViewFlashcards);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddFlashcard);
        db = new DatabaseHelper(this);

        // Load Flashcards from database
        loadFlashcards();

        // Add Flashcard Button
        fabAdd.setOnClickListener(v -> {
            // Open create flashcard activity (or create inline dialog)
            // For now, just reload to show newly added cards
            loadFlashcards();
        });
    }

    private void loadFlashcards() {
        Cursor cursor = db.getAllFlashcards();
        
        // If no flashcards exist, show message
        if (cursor == null || cursor.getCount() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No flashcards yet. Create one to get started!");
            listView.setEmptyView(emptyView);
        }

        // Map DB columns to Layout Views
        String[] from = new String[] { "question", "answer" };
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                from,
                to,
                0);

        listView.setAdapter(adapter);
    }
}
