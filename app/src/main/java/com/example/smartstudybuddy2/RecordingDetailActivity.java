package com.example.smartstudybuddy2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class RecordingDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecordingDetailActivity";

    private TextView tvTitle, tvDate, tvTranscription;
    private View btnPlay, btnPause, btnDelete, btnShare, btnSummarize, btnExportPdf;
    private DatabaseHelper dbHelper;
    private MediaPlayer mediaPlayer;
    private int recordingId = -1;
    private String filePath = "";
    private String title = "";
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_detail);

        // BACK BUTTON
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvTitle = findViewById(R.id.tvRecordingTitle);
        tvDate = findViewById(R.id.tvRecordingDate);
        tvTranscription = findViewById(R.id.tvTranscription);

        // 🔹 Buttons are now Views / LinearLayouts
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnDelete = findViewById(R.id.btnDelete);
        btnShare = findViewById(R.id.btnShare);
        btnSummarize = findViewById(R.id.btnSummarize);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        dbHelper = new DatabaseHelper(this);

        recordingId = getIntent().getIntExtra("recording_id", -1);

        Log.d(TAG, "Loading recording with ID: " + recordingId);

        // ✅ Fetch real data from database
        if (recordingId != -1) {
            Recording recording = dbHelper.getRecordingById(recordingId);
            if (recording != null) {
                title = recording.getTitle();
                String date = recording.getDate();
                String transcription = recording.getTranscription();
                filePath = recording.getFilePath();

                Log.d(TAG, "Recording found - Title: " + title + ", Date: " + date);
                Log.d(TAG, "File path: " + filePath);
                Log.d(TAG, "Transcription length: " + (transcription != null ? transcription.length() : 0));

                tvTitle.setText(title != null ? title : "No title");
                tvDate.setText("Date: " + (date != null ? date : "Unknown"));

                if (transcription != null && !transcription.isEmpty() && !transcription.equals("Processing...")) {
                    tvTranscription.setText(transcription);
                    Log.d(TAG, "✅ Transcription displayed");
                } else {
                    tvTranscription.setText("⏳ Transcription not yet available. Processing...");
                    Log.d(TAG, "⏳ Transcription not ready");
                }
            } else {
                Log.e(TAG, "❌ Recording not found in database");
                Toast.makeText(this, "Recording not found", Toast.LENGTH_SHORT).show();
                tvTitle.setText("Not Found");
                tvDate.setText("N/A");
                tvTranscription.setText("Recording data could not be loaded");
            }
        } else {
            Log.e(TAG, "❌ Invalid recording ID");
            tvTitle.setText("Invalid ID");
            tvTranscription.setText("No recording selected");
        }

        // ✅ PLAY BUTTON - Real implementation
        btnPlay.setOnClickListener(v -> playAudio());

        // ✅ PAUSE BUTTON - Real implementation
        btnPause.setOnClickListener(v -> pauseAudio());

        // ✅ DELETE BUTTON - Real implementation
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());

        // ✅ SHARE BUTTON - Real implementation
        btnShare.setOnClickListener(v -> shareRecording());


        // SUMMARIZE BUTTON
        btnSummarize.setOnClickListener(v -> {
            if (tvTranscription == null || tvTranscription.getText().toString().isEmpty() || 
                tvTranscription.getText().toString().contains("Processing")) {
                Toast.makeText(RecordingDetailActivity.this, "⏳ Transcription still processing...", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(RecordingDetailActivity.this, SummaryActivity.class);
            intent.putExtra("recording_id", recordingId);
            intent.putExtra("transcription", tvTranscription.getText().toString());  // 🔹 Pass real transcription
            intent.putExtra("fileName", title);
            startActivity(intent);
        });

        // ✅ EXPORT PDF BUTTON - Call FastAPI endpoint
        btnExportPdf.setOnClickListener(v -> exportPdfViaAPI());
    }

    // ✅ PLAY AUDIO - Simple and direct
    private void playAudio() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "No file path", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (isPlaying && mediaPlayer != null) {
                Toast.makeText(this, "Already playing", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            boolean success = false;

            // Extract actual file path if it's a file:// URI
            String actualPath = filePath;
            if (filePath.startsWith("file://")) {
                actualPath = filePath.substring(7);
            }

            // Try direct file path first
            File audioFile = new File(actualPath);
            if (audioFile.exists() && audioFile.canRead()) {
                try {
                    mediaPlayer.setDataSource(actualPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    success = true;
                    Log.d(TAG, "✅ Playing from: " + actualPath);
                } catch (Exception e) {
                    Log.w(TAG, "⚠️ File method failed: " + e.getMessage());
                }
            }

            // Try as content URI if file method failed
            if (!success) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(this, Uri.parse(filePath));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    success = true;
                    Log.d(TAG, "✅ Playing from URI");
                } catch (Exception e) {
                    Log.w(TAG, "⚠️ URI method failed: " + e.getMessage());
                }
            }

            if (!success) {
                Toast.makeText(this, "Cannot play - file not found", Toast.LENGTH_LONG).show();
                mediaPlayer.release();
                mediaPlayer = null;
                return;
            }

            isPlaying = true;
            Toast.makeText(this, "▶️ Playing", Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                mp.release();
                mediaPlayer = null;
                Toast.makeText(RecordingDetailActivity.this, "✅ Done", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPlaying = false;
                Toast.makeText(RecordingDetailActivity.this, "Play error", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isPlaying = false;
        }
    }

    // ✅ PAUSE AUDIO
    private void pauseAudio() {
        if (mediaPlayer == null) {
            Log.w(TAG, "⚠️ No audio currently playing");
            Toast.makeText(this, "No audio playing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                Log.d(TAG, "⏸️ Playback paused");
                Toast.makeText(this, "⏸️ Paused: " + title, Toast.LENGTH_SHORT).show();
            } else {
                mediaPlayer.start();
                isPlaying = true;
                Log.d(TAG, "▶️ Playback resumed");
                Toast.makeText(this, "▶️ Resumed: " + title, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during pause/resume: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ DELETE RECORDING
    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppDialog);
        builder.setTitle("🗑️ Delete Recording");
        builder.setMessage("Are you sure you want to delete '" + title + "'?\n\nThis cannot be undone.");

        builder.setPositiveButton("❌ Delete", (dialog, which) -> {
            Log.d(TAG, "🗑️ User confirmed deletion of: " + title);
            deleteRecording();
        });

        builder.setNegativeButton("✋ Cancel", (dialog, which) -> {
            Log.d(TAG, "❌ User cancelled deletion");
            dialog.dismiss();
        });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteRecording() {
        try {
            // Stop playback if any
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            DatabaseHelper db = new DatabaseHelper(this);
            boolean deleted = db.deleteRecordingById(recordingId);

            if (deleted) {
                Log.d(TAG, "✅ Recording deleted successfully: " + title);
                Toast.makeText(this, "✅ Recording deleted", Toast.LENGTH_SHORT).show();
                finish();  // Close activity and return to list
            } else {
                Log.e(TAG, "❌ Failed to delete recording from database");
                Toast.makeText(this, "❌ Failed to delete recording", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during deletion: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error deleting recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ SHARE RECORDING
    private void shareRecording() {
        try {
            Log.d(TAG, "📤 Sharing recording: " + title);
            Log.d(TAG, "   Original path: " + filePath);

            // Extract actual file path (remove file:// prefix if present)
            String actualPath = filePath;
            if (actualPath.startsWith("file://")) {
                actualPath = actualPath.substring(7);
            }
            Log.d(TAG, "   Actual path: " + actualPath);

            File audioFile = new File(actualPath);
            
            if (!audioFile.exists()) {
                Toast.makeText(this, "❌ Audio file not found", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ File doesn't exist at: " + actualPath);
                return;
            }

            // ✅ Copy to cache for reliable sharing (some apps like WhatsApp prefer cache files)
            File cacheDir = new File(getCacheDir(), "ShareCache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File shareFile = new File(cacheDir, title + ".mp3");
            copyFile(audioFile, shareFile);
            Log.d(TAG, "✅ File copied to cache: " + shareFile.getAbsolutePath());

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/mpeg");  // ✅ Changed to audio/mpeg for MP3
            
            // Use FileProvider to get proper URI
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    shareFile
            );
            
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recording: " + title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I'm sharing a recording with you: " + title);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.d(TAG, "✅ Launching share dialog");
            startActivity(Intent.createChooser(shareIntent, "Share Recording Via"));

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during sharing: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error sharing recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Copy file from source to destination
     */
    private void copyFile(File source, File destination) throws Exception {
        java.io.FileInputStream fis = new java.io.FileInputStream(source);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(destination);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fis.close();
        fos.close();
        Log.d(TAG, "   ✅ File copied successfully");
    }

    // ✅ EXPORT PDF VIA FASTAPI
    private void exportPdfViaAPI() {
        if (recordingId == -1) {
            Toast.makeText(this, "❌ Invalid recording ID", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Invalid recording ID for PDF export");
            return;
        }

        Log.d(TAG, "📄 Export PDF clicked for recording ID: " + recordingId);
        Toast.makeText(this, "⏳ Generating PDF...", Toast.LENGTH_SHORT).show();

        // Background thread to call FastAPI endpoint
        new Thread(() -> {
            try {
                Log.d(TAG, "=== PDF Export: Fetching data for Recording ID: " + recordingId + " ===");
                
                // ✅ STEP 1: Get Recording data from audio_files table for THIS specific recording
                Recording recordingData = dbHelper.getRecordingById(recordingId);
                if (recordingData == null) {
                    Log.e(TAG, "❌ CRITICAL: Recording ID " + recordingId + " not found in database!");
                    runOnUiThread(() -> 
                        Toast.makeText(RecordingDetailActivity.this, 
                            "❌ Error: Recording not found", Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                
                // ✅ STEP 2: Extract summary from audio_files table (NOT from transcription)
                String summary = recordingData.getSummary();
                Log.d(TAG, "   📊 Summary from audio_files(ID=" + recordingId + "): " + 
                    (summary != null && !summary.isEmpty() ? "✅ Found (" + summary.length() + " chars)" : "⚠️ Empty or null"));
                if (summary == null || summary.isEmpty()) {
                    summary = "";
                }
                
                // ✅ STEP 3: Extract quiz_json from audio_files table (NOT generated fresh)
                String quizJson = recordingData.getQuizJson();
                Log.d(TAG, "   🎯 Quiz JSON from audio_files(ID=" + recordingId + "): " + 
                    (quizJson != null && !quizJson.isEmpty() && !quizJson.equals("[]") ? "✅ Found (" + quizJson.length() + " chars)" : "⚠️ Empty or no quiz"));
                if (quizJson == null || quizJson.isEmpty()) {
                    quizJson = "[]";
                }
                
                // ✅ STEP 4: Fetch flashcards for THIS specific recording
                java.util.ArrayList<Flashcard> flashcards = dbHelper.getFlashcardsForRecording(recordingId);
                java.util.List<java.util.Map<String, String>> flashcardsList = new java.util.ArrayList<>();
                
                if (flashcards != null && !flashcards.isEmpty()) {
                    Log.d(TAG, "   🎴 Flashcards for ID=" + recordingId + ": ✅ Found " + flashcards.size() + " items");
                    for (Flashcard card : flashcards) {
                        java.util.Map<String, String> cardMap = new java.util.HashMap<>();
                        cardMap.put("question", card.getQuestion());
                        cardMap.put("answer", card.getAnswer());
                        flashcardsList.add(cardMap);
                    }
                } else {
                    Log.w(TAG, "   🎴 Flashcards for ID=" + recordingId + ": ⚠️ None found");
                }
                
                Log.d(TAG, "=== PDF Data Collection Complete ===");
                
                // ✅ Safe null checks and escaping
                String safeTitle = title != null ? title.replace("\"", "\\\"").replace("\n", "\\n") : "";
                String safeDate = tvDate.getText().toString().replace("Date: ", "").replace("\"", "\\\"").replace("\n", "\\n");
                String safeTranscription = tvTranscription.getText().toString().isEmpty() ? "" : tvTranscription.getText().toString().replace("\"", "\\\"").replace("\n", "\\n");
                String safeSummary = (summary != null && !summary.isEmpty()) ? summary.replace("\"", "\\\"").replace("\n", "\\n") : "";
                
                // ✅ Build JSON carefully - use Gson to handle escaping properly
                // Convert to proper JSON using manual construction with proper escaping
                java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
                requestMap.put("recording_id", recordingId);
                requestMap.put("title", safeTitle != null ? safeTitle : "");
                requestMap.put("date", safeDate != null ? safeDate : "");
                requestMap.put("transcription", safeTranscription != null ? safeTranscription : "");
                requestMap.put("summary", safeSummary != null ? safeSummary : "");
                
                // ✅ Add flashcards as a proper list that Gson can serialize
                requestMap.put("flashcards", flashcardsList);
                
                // ✅ Keep quiz_json as STRING (not JSONArray) - backend expects string format
                // The backend will parse it when needed
                requestMap.put("quiz_json", quizJson != null ? quizJson : "[]");
                
                // Use Gson to convert to JSON
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String json = gson.toJson(requestMap);
                
                Log.d(TAG, "📤 Sending PDF Export Request");
                Log.d(TAG, "   Recording ID (source): " + recordingId);
                Log.d(TAG, "   Title: " + safeTitle);
                Log.d(TAG, "   Date: " + safeDate);
                Log.d(TAG, "   Transcription length: " + safeTranscription.length() + " chars");
                Log.d(TAG, "   Summary length: " + safeSummary.length() + " chars (from audio_files table)");
                Log.d(TAG, "   Quiz JSON length: " + (quizJson != null ? quizJson.length() : 0) + " chars (from audio_files table)");
                Log.d(TAG, "   Flashcards count: " + flashcardsList.size());
                Log.d(TAG, "   URL: http://10.237.2.53:8000/export-pdf/");
                
                okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
                okhttp3.RequestBody body = okhttp3.RequestBody.create(json, JSON);

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://10.237.2.53:8000/export-pdf/")
                        .post(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Get the PDF bytes and save or open
                    byte[] pdfBytes = response.body().bytes();
                    
                    if (pdfBytes.length > 0) {
                        Log.d(TAG, "✅ PDF received from FastAPI (" + pdfBytes.length + " bytes)");
                        savePdfAndOpen(pdfBytes);
                    } else {
                        Log.e(TAG, "❌ Empty PDF response from FastAPI");
                        runOnUiThread(() -> 
                            Toast.makeText(RecordingDetailActivity.this, 
                                "❌ Empty PDF response", 
                                Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    // ✅ Better error logging
                    String errorMsg = "Unknown error";
                    try {
                        if (response.body() != null) {
                            errorMsg = response.body().string();
                        }
                    } catch (Exception e) {
                        errorMsg = e.getMessage();
                    }
                    
                    Log.e(TAG, "❌ FastAPI error (status: " + response.code() + ")");
                    Log.e(TAG, "   Error message: " + errorMsg);
                    
                    String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> 
                        Toast.makeText(RecordingDetailActivity.this, 
                            "❌ PDF Error " + response.code() + ": " + finalErrorMsg.substring(0, Math.min(100, finalErrorMsg.length())), 
                            Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Exception during PDF export: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> 
                    Toast.makeText(RecordingDetailActivity.this, 
                        "❌ Error: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Save PDF to file and open in browser/viewer
    private void savePdfAndOpen(byte[] pdfBytes) {
        try {
            // Save PDF to Downloads folder
            File downloadsDir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS), "SmartStudyBuddy");
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            String filename = "Recording_" + recordingId + "_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(downloadsDir, filename);
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(pdfFile);
            fos.write(pdfBytes);
            fos.close();
            
            Log.d(TAG, "✅ PDF saved to: " + pdfFile.getAbsolutePath());
            
            // Open PDF in viewer
            Intent intent = new Intent(Intent.ACTION_VIEW);
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile
            );
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(intent, "Open PDF with"));
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving/opening PDF: " + e.getMessage());
            e.printStackTrace();
            runOnUiThread(() -> 
                Toast.makeText(RecordingDetailActivity.this, 
                    "❌ Could not open PDF: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show()
            );
        }
    }

    // Open PDF in browser (fallback)
    private void openPdfInBrowser(String pdfUrl) {
        try {
            Log.d(TAG, "🌐 Opening PDF in browser: " + pdfUrl);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(pdfUrl));
            startActivity(browserIntent);
            
            runOnUiThread(() -> 
                Toast.makeText(RecordingDetailActivity.this, 
                    "✅ Opening PDF...", 
                    Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening PDF in browser: " + e.getMessage());
            e.printStackTrace();
            runOnUiThread(() -> 
                Toast.makeText(RecordingDetailActivity.this, 
                    "❌ Could not open PDF: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show()
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused");
        // Optionally pause playback when activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed, releasing media player");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
