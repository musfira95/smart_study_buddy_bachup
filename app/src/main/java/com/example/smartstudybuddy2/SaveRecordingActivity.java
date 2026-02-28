package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.File;

public class SaveRecordingActivity extends BaseActivity {

    private static final String TAG = "SaveRecordingActivity";
    
    private EditText recordingNameInput;
    private CardView saveButton, cancelButton;
    private String audioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_recording);

        // Get file path from intent
        audioFilePath = getIntent().getStringExtra("audioFilePath");
        
        Log.d(TAG, "📝 SaveRecordingActivity opened");
        Log.d(TAG, "   audioFilePath: " + audioFilePath);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "❌ User pressed back, discarding recording");
                
                // Delete the temporary file
                try {
                    File tempFile = new File(audioFilePath);
                    if (tempFile.exists()) {
                        tempFile.delete();
                        Log.d(TAG, "🗑️ Deleted temporary file: " + audioFilePath);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "⚠️ Could not delete temp file: " + e.getMessage());
                }
                
                Toast.makeText(this, "Recording discarded", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        // Input field with default name
        recordingNameInput = findViewById(R.id.recordingNameInput);
        recordingNameInput.setText("Recorded_" + System.currentTimeMillis());
        recordingNameInput.selectAll();  // Auto-select for easy editing

        // Save button
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String fileName = recordingNameInput.getText().toString().trim();

            if (fileName.isEmpty()) {
                Toast.makeText(SaveRecordingActivity.this, "Please enter a file name", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "⚠️ User entered empty name");
                return;
            }

            Log.d(TAG, "✅ User entered fileName: " + fileName);

            // Insert into DB and get recording ID
            try {
                DatabaseHelper db = new DatabaseHelper(SaveRecordingActivity.this);
                long recordingId = db.insertRecording(fileName, audioFilePath);

                Log.d(TAG, "✅ Recording saved to database with ID: " + recordingId);
                Log.d(TAG, "   fileName: " + fileName);
                Log.d(TAG, "   filePath: " + audioFilePath);

                // Send to ProcessAudioActivity to upload and transcribe
                Intent intent = new Intent(SaveRecordingActivity.this, ProcessAudioActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("recordingId", recordingId);
                intent.putExtra("fileSize", new File(audioFilePath).length());
                intent.putExtra("audioUri", android.net.Uri.fromFile(new File(audioFilePath)).toString());

                Log.d(TAG, "🚀 Starting ProcessAudioActivity with ID: " + recordingId);
                startActivity(intent);
                finish();

            } catch (Exception e) {
                Log.e(TAG, "❌ Error saving recording: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(SaveRecordingActivity.this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "❌ User cancelled saving, allowing re-record");
            Toast.makeText(SaveRecordingActivity.this, "Recording discarded", Toast.LENGTH_SHORT).show();
            
            // Delete the temporary file
            try {
                File tempFile = new File(audioFilePath);
                if (tempFile.exists()) {
                    tempFile.delete();
                    Log.d(TAG, "🗑️ Deleted temporary file: " + audioFilePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Could not delete temp file: " + e.getMessage());
            }
            
            finish();
        });
    }
}
