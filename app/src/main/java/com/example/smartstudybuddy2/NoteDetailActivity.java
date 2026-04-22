package com.example.smartstudybuddy2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private LinearLayout btnUpdate, btnDelete, btnSummarize;
    private DatabaseHelper dbHelper;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // ------------------- Find Views -------------------
        etTitle = findViewById(R.id.etDetailTitle);
        etContent = findViewById(R.id.etDetailContent);
        btnUpdate = findViewById(R.id.btnUpdateNote);
        btnDelete = findViewById(R.id.btnDeleteNote);
        btnSummarize = findViewById(R.id.btnSummarizeNote);

        // ------------------- Make LinearLayouts clickable -------------------
        btnUpdate.setClickable(true);
        btnUpdate.setFocusable(true);
        btnDelete.setClickable(true);
        btnDelete.setFocusable(true);
        btnSummarize.setClickable(true);
        btnSummarize.setFocusable(true);

        dbHelper = new DatabaseHelper(this);

        // ------------------- Get data from intent -------------------
        noteId = getIntent().getIntExtra("noteId", -1);
        String title = getIntent().getStringExtra("noteTitle");
        String content = getIntent().getStringExtra("noteContent");

        etTitle.setText(title);
        etContent.setText(content);

        // ------------------- Update note -------------------
        btnUpdate.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString().trim();
            String updatedContent = etContent.getText().toString().trim();

            if (updatedTitle.isEmpty()) {
                updatedTitle = "Untitled"; // agar title blank ho to default name
            }

            if (updatedContent.isEmpty()) {
                Toast.makeText(NoteDetailActivity.this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateNote(noteId, updatedTitle, updatedContent);
            if (updated) {
                Toast.makeText(NoteDetailActivity.this, "Note updated successfully!", Toast.LENGTH_SHORT).show();
                finish(); // back to NotesActivity
            } else {
                Toast.makeText(NoteDetailActivity.this, "Update failed!", Toast.LENGTH_SHORT).show();
            }
        });

        // ------------------- Delete note -------------------
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.AppDialog)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteNote(noteId);
                        Toast.makeText(NoteDetailActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        finish(); // back to NotesActivity
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // ------------------- Summarize button -------------------
        btnSummarize.setOnClickListener(v -> {
            String contentText = etContent.getText().toString().trim();
            if (!contentText.isEmpty()) {
                Intent intent = new Intent(NoteDetailActivity.this, SummaryActivity.class);
                intent.putExtra("transcriptionText", contentText);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No content to summarize", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
