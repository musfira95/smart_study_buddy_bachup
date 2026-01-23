package com.example.smartstudybuddy2;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private ArrayList<NoteItem> noteList;
    private DatabaseHelper dbHelper;

    private EditText etTitle, etContent;
    private CardView btnSave;   // ✅ FIXED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        recyclerView = findViewById(R.id.notesRecyclerView);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSaveNote);

        dbHelper = new DatabaseHelper(this);
        noteList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, noteList);
        recyclerView.setAdapter(adapter);

        // Receive transcription
        String transcription = getIntent().getStringExtra("TRANSCRIPTION_TEXT");
        if (transcription != null && !transcription.isEmpty()) {
            etContent.setText(transcription);
        }

        loadNotes();

        // Save note
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(this, "Enter content", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                title = "Untitled";
            }

            boolean inserted = dbHelper.insertNote(title, content);
            if (inserted) {
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                loadNotes();
                etTitle.setText("");
                etContent.setText("");
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load notes
    private void loadNotes() {
        noteList.clear();
        Cursor cursor = dbHelper.getAllNotes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                noteList.add(new NoteItem(id, title, content));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
