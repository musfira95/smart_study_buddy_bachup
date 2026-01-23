package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TranscriptionActivity extends AppCompatActivity {

    private TextView tvTranscriptionContent;
    private Button  btnViewAll;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcription);

        tvTranscriptionContent = findViewById(R.id.tvTranscriptionContent);
        btnViewAll = findViewById(R.id.btnViewAll);
        db = new DatabaseHelper(this);

// Last recording fetch karo
        Recording lastRecording = db.getLastRecording();
        if (lastRecording != null) {
            String displayText = "Title: " + lastRecording.getTitle() +
                    "\nPath: " + lastRecording.getFilePath() +
                    "\nDate: " + lastRecording.getDate();


            // Agar note me save nahi hai to save karo
            db.insertNote(lastRecording.getTitle(), lastRecording.getTitle() + "\n" + lastRecording.getDate());

        } else {
            tvTranscriptionContent.setText("No transcription available yet!");
        }

// View All button click
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(TranscriptionActivity.this, AllTranscriptionsActivity.class);
            startActivity(intent);
        });



        // View All Transcriptions button click
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(TranscriptionActivity.this, AllTranscriptionsActivity.class);
            startActivity(intent);
        });
    }
}
