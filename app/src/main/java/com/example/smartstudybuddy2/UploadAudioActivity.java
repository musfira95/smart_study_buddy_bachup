package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.res.AssetFileDescriptor;

import java.io.IOException;

public class UploadAudioActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int RECORD_AUDIO_REQUEST = 200;

    // Upload Audio UI
    private CardView uploadButton, recordButton;
    private LinearLayout processButton;
    private TextView fileNameText;

    // Bottom Navigation
    private ImageView navHome, navAnalytics, navSettings, navProfile;

    private Uri audioUri;
    private String fileName;
    private long fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_audio);

        // ===== Upload Audio Views =====
        uploadButton = findViewById(R.id.btnSelectAudio);
        recordButton = findViewById(R.id.btnRecordAudio);
        processButton = findViewById(R.id.btnProcessAudio);
        fileNameText = findViewById(R.id.tvFileStatus);

        processButton.setEnabled(false);

        // ===== Bottom Nav Views =====
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        // ================= Upload MP3 =================
        uploadButton.setOnClickListener(v -> openAudioPicker());

        // ================= Process Audio =================
        processButton.setOnClickListener(v -> {
            if (audioUri != null) {
                Intent intent = new Intent(UploadAudioActivity.this, ProcessAudioActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("fileSize", fileSize);
                intent.putExtra("audioUri", audioUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select an MP3 first", Toast.LENGTH_SHORT).show();
            }
        });

        // ================= Record Audio =================
        recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(UploadAudioActivity.this, RecordMicActivity.class);
            startActivityForResult(intent, RECORD_AUDIO_REQUEST);
        });

        // ================= Bottom Navigation (SAME AS DASHBOARD) =================

        // Home → Dashboard
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(UploadAudioActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Analytics
        navAnalytics.setOnClickListener(v ->
                startActivity(new Intent(UploadAudioActivity.this, AnalyticsActivity.class))
        );

        // Settings
        navSettings.setOnClickListener(v ->
                startActivity(new Intent(UploadAudioActivity.this, ThemeSettingsActivity.class))
        );

        // Profile
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(UploadAudioActivity.this, ProfileActivity.class))
        );
    }

    // ================= Audio Picker =================
    private void openAudioPicker() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select MP3"), PICK_AUDIO_REQUEST);
    }

    // ================= Handle Results =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Recording Result
        if (requestCode == RECORD_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Recording received", Toast.LENGTH_SHORT).show();
        }

        // MP3 Picker Result
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            audioUri = data.getData();
            if (audioUri != null) {
                fileName = getFileName(audioUri);
                long sizeInBytes = getFileSizeBytes(audioUri); // Get exact bytes

                if (!fileName.toLowerCase().endsWith(".mp3")) {
                    Toast.makeText(this, "Please select an MP3 audio file only", Toast.LENGTH_SHORT).show();
                    resetSelection();
                    return;
                }

                // 5MB Limit = 5 * 1024 * 1024 = 5242880 Bytes
                if (sizeInBytes > 5 * 1024 * 1024) {
                    Toast.makeText(this, "File is too large! Limit is 5MB.", Toast.LENGTH_LONG).show();
                    resetSelection();
                    return;
                }

                fileSize = sizeInBytes / 1024; // Store in KB for display if needed
                fileNameText.setText("Selected: " + fileName + " (" + fileSize + " KB)");
                processButton.setEnabled(true);
            }
        }
    }

    private void resetSelection() {
        audioUri = null;
        fileName = null;
        fileSize = 0;
        fileNameText.setText("No file selected");
        processButton.setEnabled(false);
    }

    // ================= Helpers =================
    private String getFileName(Uri uri) {
        if (uri == null) return "";
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        return result != null ? result : uri.getLastPathSegment();
    }

    private long getFileSizeBytes(Uri uri) {
        if (uri == null) return 0;
        long size = 0;

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        if (size <= 0) {
            AssetFileDescriptor afd = null;
            try {
                afd = getContentResolver().openAssetFileDescriptor(uri, "r");
                if (afd != null && afd.getLength() > 0) size = afd.getLength();
            } catch (Exception ignored) {
            } finally {
                if (afd != null) {
                    try { afd.close(); } catch (IOException ignored) {}
                }
            }
        }

        return size;
    }
}
