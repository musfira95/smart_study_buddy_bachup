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
        transcriptionText.setText("Initialization of Offline Model in progress... (Step 1/3)");
        Toast.makeText(this, "Starting Offline Processing...", Toast.LENGTH_SHORT).show();
        
        // --- SIMULATED SUCCESS FOR DEMO ---
        new android.os.Handler().postDelayed(() -> {
            String dummyTranscript = "This is a simulated transcription text. In a real scenario, this would be the output of your AI model processing the audio file.";
            
            transcriptionText.setText("Processing Complete!\n\nTranscription:\n" + dummyTranscript);
            Toast.makeText(this, "Processing Complete!", Toast.LENGTH_SHORT).show();

            // Reveal the View Summary button
            btnViewSummary.setVisibility(View.VISIBLE);
            
            // Set OnClickListener for the new button
            btnViewSummary.setOnClickListener(v -> {
                Intent intent = new Intent(ProcessAudioActivity.this, SummaryActivity.class);
                intent.putExtra("transcriptionText", dummyTranscript);
                startActivity(intent);
                finish();
            });

        }, 2000); // 2 seconds delay to simulate processing
    }


}
