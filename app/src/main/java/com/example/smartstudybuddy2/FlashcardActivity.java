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

        // Load Flashcards
        loadFlashcards();

        // Add Flashcard Button - For Valid Demo, we will just reload or show toast
        // ideally this opens a CreateFlashcardActivity (Phase 2)
        fabAdd.setOnClickListener(v -> {
            // For now, insert a dummy flashcard to demonstrate functionality
            db.insertFlashcard("What is Android?", "A mobile OS by Google", "General");
            loadFlashcards();
        });
    }

    private void loadFlashcards() {
        Cursor cursor = db.getAllFlashcards();
        
        // Map DB columns to Layout Views
        String[] from = new String[] { "question", "answer" };
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2, // Simple built-in layout
                cursor,
                from,
                to,
                0);

        listView.setAdapter(adapter);
    }
}
