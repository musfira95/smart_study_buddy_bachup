package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView; // Added import
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProcessAudioActivity extends BaseActivity {

    TextView transcriptionText;
    CardView processAudioBtn, viewDetailsBtn, btnViewSummary;   // Added btnViewSummary

    String fileName;
    String audioUriString;
    Uri audioUri;
    long fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_audio);

        transcriptionText = findViewById(R.id.resultTextView);
        processAudioBtn = findViewById(R.id.processButton);
        viewDetailsBtn = findViewById(R.id.viewDetailsBtn);
        btnViewSummary = findViewById(R.id.btnViewSummary);

        // Manual Back Button logic
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // RECEIVE DATA
        fileName = getIntent().getStringExtra("fileName");
        fileSize = getIntent().getLongExtra("fileSize", 0);
        audioUriString = getIntent().getStringExtra("audioUri");

        audioUri = Uri.parse(audioUriString);


        // PROCESS AUDIO
        processAudioBtn.setOnClickListener(v -> processAudioLocally());

        // VIEW DETAILS
        viewDetailsBtn.setOnClickListener(v -> {
            Intent detailsIntent = new Intent(
                    ProcessAudioActivity.this,
                    ViewUploadedDetailsActivity.class
            );
            detailsIntent.putExtra("fileName", fileName);
            detailsIntent.putExtra("fileSize", fileSize);
            detailsIntent.putExtra("uploadTime", new java.util.Date().toString());
            startActivity(detailsIntent);
        });
    }

    // Convert Uri → File
    private File convertUriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "audio_upload.mp3");

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;

            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;

        } catch (Exception e) {
            Toast.makeText(this, "File conversion error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Placeholder for Offline Processing (Vosk)
    private void processAudioLocally() {
        transcriptionText.setText("Processing audio file... Please wait...");
        Toast.makeText(this, "Starting Audio Processing", Toast.LENGTH_SHORT).show();

        // Run processing in background thread
        new Thread(() -> {
            try {
                // Step 1: Convert Uri to File
                File audioFile = convertUriToFile(audioUri);
                if (audioFile == null) {
                    runOnUiThread(() -> {
                        transcriptionText.setText("Error: Could not read audio file");
                        Toast.makeText(ProcessAudioActivity.this, "File read error", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Step 2: Simulate Speech-to-Text Processing
                String transcription = performSpeechToText(audioFile);

                // Step 3: Save to database
                DatabaseHelper db = new DatabaseHelper(this);
                db.insertTranscription(fileName, transcription);

                // Update UI on main thread
                runOnUiThread(() -> {
                    transcriptionText.setText(transcription);
                    Toast.makeText(ProcessAudioActivity.this, "Processing Complete!", Toast.LENGTH_SHORT).show();

                    // Enable summary button
                    btnViewSummary.setOnClickListener(v -> {
                        Intent summaryIntent = new Intent(ProcessAudioActivity.this, SummaryActivity.class);
                        summaryIntent.putExtra("transcription", transcription);
                        summaryIntent.putExtra("fileName", fileName);
                        startActivity(summaryIntent);
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    transcriptionText.setText("Error during processing: " + e.getMessage());
                    Toast.makeText(ProcessAudioActivity.this, "Processing failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Real speech-to-text conversion (placeholder implementation)
    // In production, this would use Vosk or Google Cloud Speech-to-Text
    private String performSpeechToText(File audioFile) {
        try {
            // This is a placeholder - in real implementation, use:
            // - Vosk (offline)
            // - Google Cloud Speech-to-Text API
            // - CMU PocketSphinx
            // - Mozilla DeepSpeech

            // For now, we'll simulate processing
            String[] sampleTranscriptions = {
                "Good morning class, today we will discuss the fundamentals of artificial intelligence and machine learning. " +
                "AI is a branch of computer science that aims to create intelligent machines. " +
                "These machines can perform tasks that typically require human intelligence. " +
                "Some key applications include natural language processing, computer vision, and robotics.",

                "In this lecture, we will cover the basics of data structures. " +
                "Arrays, linked lists, stacks, queues, and trees are fundamental data structures. " +
                "Understanding these concepts is crucial for efficient programming. " +
                "We will implement examples in Java and Python.",

                "Today's topic is about web development. " +
                "HTML provides the structure, CSS provides the styling, and JavaScript provides interactivity. " +
                "We will also discuss modern frameworks like React, Vue, and Angular. " +
                "Building responsive websites is essential in today's mobile-first world.",

                "Welcome to the Physics lecture. Today we discuss Newton's laws of motion. " +
                "The first law states that an object remains at rest unless acted upon by a force. " +
                "The second law defines the relationship between force, mass, and acceleration. " +
                "The third law states that for every action, there is an equal and opposite reaction."
            };

            // Return a random transcription for demo purposes
            int randomIndex = (int) (Math.random() * sampleTranscriptions.length);
            return sampleTranscriptions[randomIndex];

        } catch (Exception e) {
            return "Error during transcription: " + e.getMessage();
        }
    }
}


