package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartstudybuddy2.network.ApiClient;
import com.example.smartstudybuddy2.network.ApiService;
import com.example.smartstudybuddy2.network.TranscriptionResponse;
import com.example.smartstudybuddy2.network.SummaryResponse;
import com.example.smartstudybuddy2.network.SummaryRequest;
import com.example.smartstudybuddy2.network.QuizResponse;
import com.example.smartstudybuddy2.network.QuizRequest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProcessAudioActivity extends BaseActivity {

    TextView transcriptionText;
    CardView processAudioBtn, viewDetailsBtn, btnViewSummary;
    ProgressBar progressBar;

    String fileName;
    String audioUriString;
    Uri audioUri;
    long fileSize;
    long recordingId = -1;  // ✅ NEW: Store recording ID
    String lastTranscription = "";  // ✅ NEW: Store transcription globally

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_audio);

        transcriptionText = findViewById(R.id.resultTextView);
        processAudioBtn = findViewById(R.id.processButton);
        viewDetailsBtn = findViewById(R.id.viewDetailsBtn);
        btnViewSummary = findViewById(R.id.btnViewSummary);
        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            progressBar = new ProgressBar(this);
        }

        // Initially hide summary button - will show after processing
        btnViewSummary.setVisibility(android.view.View.GONE);
        progressBar.setVisibility(android.view.View.GONE);

        // ✅ Set up onClick listeners ONCE in onCreate (not in callback!)
        btnViewSummary.setOnClickListener(v -> {
            Log.d("ProcessAudioActivity", "📝 Summary button clicked!");
            
            if (lastTranscription == null || lastTranscription.isEmpty()) {
                Log.e("ProcessAudioActivity", "❌ lastTranscription is EMPTY!");
                Toast.makeText(ProcessAudioActivity.this, "❌ No transcription available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d("ProcessAudioActivity", "✅ lastTranscription exists: " + lastTranscription.substring(0, Math.min(50, lastTranscription.length())));
            Log.d("ProcessAudioActivity", "📝 Generating summary...");
            generateSummary(lastTranscription);
            Intent summaryIntent = new Intent(ProcessAudioActivity.this, SummaryActivity.class);
            summaryIntent.putExtra("transcription", lastTranscription);
            summaryIntent.putExtra("fileName", fileName);
            summaryIntent.putExtra("recordingId", recordingId);  // ✅ PASS RECORDING ID
            startActivity(summaryIntent);
        });

        // Manual Back Button logic
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // RECEIVE DATA
        fileName = getIntent().getStringExtra("fileName");
        recordingId = getIntent().getLongExtra("recordingId", -1);  // ✅ Get recording ID
        fileSize = getIntent().getLongExtra("fileSize", 0);
        audioUriString = getIntent().getStringExtra("audioUri");

        audioUri = Uri.parse(audioUriString);
        
        Log.d("ProcessAudioActivity", "📥 Received: fileName=" + fileName + ", recordingId=" + recordingId);

        // PROCESS AUDIO - Call FastAPI server instead of local processing
        processAudioBtn.setOnClickListener(v -> uploadAudioToServer());

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

    // Convert Uri → File (only if needed)
    private File convertUriToFile(Uri uri) {
        try {
            String uriString = uri.toString();
            Log.d("ProcessAudioActivity", "convertUriToFile called with: " + uriString);

            // 🔹 If it's already a real file path (from upload activity), just use it
            if (uriString.startsWith("file://")) {
                String filePath = uriString.substring(7);  // Remove "file://" prefix
                File file = new File(filePath);
                Log.d("ProcessAudioActivity", "   Detected file:// path");
                Log.d("ProcessAudioActivity", "   Extracted path: " + filePath);
                Log.d("ProcessAudioActivity", "   File exists: " + file.exists());
                Log.d("ProcessAudioActivity", "   File readable: " + file.canRead());
                Log.d("ProcessAudioActivity", "   File size: " + file.length());
                if (file.exists()) {
                    return file;
                }
            }

            // 🔹 If it's a direct file path (no scheme), use directly
            if (!uriString.contains("://")) {
                File file = new File(uriString);
                Log.d("ProcessAudioActivity", "   Detected direct file path");
                Log.d("ProcessAudioActivity", "   File exists: " + file.exists());
                Log.d("ProcessAudioActivity", "   File readable: " + file.canRead());
                if (file.exists()) {
                    return file;
                }
            }

            // Otherwise, convert content:// URI to file
            Log.d("ProcessAudioActivity", "   Converting content:// URI to file...");
            InputStream inputStream = getContentResolver().openInputStream(uri);
            
            File appStorageDir = getExternalFilesDir(null);
            if (appStorageDir == null) {
                appStorageDir = getFilesDir();
            }
            
            File uploadsDir = new File(appStorageDir, "Uploads");
            if (!uploadsDir.exists()) uploadsDir.mkdirs();
            
            String uniqueFileName = System.currentTimeMillis() + "_audio_upload.mp3";
            File tempFile = new File(uploadsDir, uniqueFileName);

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;

            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.close();
            inputStream.close();

            Log.d("ProcessAudioActivity", "✅ File saved to permanent storage");
            Log.d("ProcessAudioActivity", "   Path: " + tempFile.getAbsolutePath());
            Log.d("ProcessAudioActivity", "   Size: " + tempFile.length() + " bytes");

            return tempFile;

        } catch (Exception e) {
            Log.e("ProcessAudioActivity", "❌ Error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Upload audio to FastAPI server for transcription
    private void uploadAudioToServer() {
        if (audioUri == null) {
            Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        processAudioBtn.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);
        transcriptionText.setText("Uploading audio to server...");

        try {
            File audioFile = convertUriToFile(audioUri);
            if (audioFile == null) {
                throw new Exception("Could not read audio file");
            }

            // 🔹 If we have a recording ID, update the database with the actual file path
            if (recordingId > 0) {
                try {
                    DatabaseHelper db = new DatabaseHelper(ProcessAudioActivity.this);
                    boolean updated = db.updateRecordingFilePath((int) recordingId, audioFile.getAbsolutePath());
                    Log.d("ProcessAudioActivity", "📝 File path updated in DB: " + audioFile.getAbsolutePath() + " - Result: " + updated);
                } catch (Exception e) {
                    Log.w("ProcessAudioActivity", "⚠️ Warning: Could not update file path in DB: " + e.getMessage());
                }
            }

            Log.d("ProcessAudioActivity", "Uploading file: " + audioFile.getAbsolutePath());
            Log.d("ProcessAudioActivity", "File size: " + audioFile.length() + " bytes");
            Log.d("ProcessAudioActivity", "File name: " + audioFile.getName());
            Log.d("ProcessAudioActivity", "File exists: " + audioFile.exists());
            Log.d("ProcessAudioActivity", "File can read: " + audioFile.canRead());

            // Create RequestBody from the audio file
            // Using audio/mpeg for MP3 files
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("audio/mpeg"),
                    audioFile
            );

            // Create MultipartBody.Part with field name "audio_file" to match FastAPI expectation
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "audio_file",
                    audioFile.getName(),
                    requestBody
            );
            // Create API service and upload
            Log.d("ProcessAudioActivity", "🔵 Creating ApiClient...");
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<TranscriptionResponse> call = apiService.uploadAudio(filePart);

            Log.d("ProcessAudioActivity", "🔵 Request URL: http://10.46.38.53:8000/transcribe/");
            Log.d("ProcessAudioActivity", "🔵 Sending multipart request...");
            Log.d("ProcessAudioActivity", "🔵 File size being uploaded: " + audioFile.length() + " bytes");
            Log.d("ProcessAudioActivity", "🔵 Timeout: 120s connect, 600s read/write");

            call.enqueue(new Callback<TranscriptionResponse>() {
                @Override
                public void onResponse(Call<TranscriptionResponse> call, Response<TranscriptionResponse> response) {
                    progressBar.setVisibility(android.view.View.GONE);
                    processAudioBtn.setEnabled(true);

                    Log.d("ProcessAudioActivity", "Response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String transcription = response.body().getText();
                        Log.d("ProcessAudioActivity", "✅ Transcription received from API");
                        Log.d("ProcessAudioActivity", "   Length: " + transcription.length() + " chars");
                        Log.d("ProcessAudioActivity", "   Preview: " + transcription.substring(0, Math.min(100, transcription.length())));

                        // ✅ INSERT INTO SQLite IMMEDIATELY AFTER SUCCESSFUL API RESPONSE
                        try {
                            DatabaseHelper db = new DatabaseHelper(ProcessAudioActivity.this);
                            String saveFileName = fileName != null ? fileName : ("Recorded_" + System.currentTimeMillis());

                            Log.d("ProcessAudioActivity", "💾 INSERTING TRANSCRIPTION INTO SQLite DATABASE...");
                            Log.d("ProcessAudioActivity", "   fileName: " + saveFileName);
                            Log.d("ProcessAudioActivity", "   recordingId: " + recordingId);
                            Log.d("ProcessAudioActivity", "   transcription length: " + transcription.length() + " chars");

                            boolean saved;
                            
                            // ✅ If we have a recording ID, use that (PRIMARY METHOD - most reliable)
                            if (recordingId > 0) {
                                Log.d("ProcessAudioActivity", "🎯 PRIMARY METHOD: updateTranscriptionById(recordingId=" + recordingId + ")");
                                saved = db.updateTranscriptionById((int) recordingId, transcription);
                                Log.d("ProcessAudioActivity", "   UPDATE result: " + (saved ? "✅ SUCCESS" : "❌ FAILED"));
                            } else {
                                // Fallback to file_name matching (FALLBACK METHOD)
                                Log.w("ProcessAudioActivity", "⚠️ FALLBACK METHOD: insertTranscription(fileName=\"" + saveFileName + "\")");
                                Log.w("ProcessAudioActivity", "   (recordingId was not available)");
                                saved = db.insertTranscription(saveFileName, transcription);
                                Log.d("ProcessAudioActivity", "   INSERT result: " + (saved ? "✅ SUCCESS" : "❌ FAILED"));
                            }

                            if (saved) {
                                Log.i("ProcessAudioActivity", "✅✅✅ TRANSCRIPTION SAVED TO DATABASE SUCCESSFULLY ✅✅✅");
                                Toast.makeText(ProcessAudioActivity.this, "✅ Transcription saved to SQLite database!", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("ProcessAudioActivity", "❌ ERROR: Transcription save to database FAILED!");
                                Toast.makeText(ProcessAudioActivity.this, "⚠️ Error saving to database but transcription was received", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("ProcessAudioActivity", "❌ EXCEPTION while saving transcription: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(ProcessAudioActivity.this, "Error saving transcription: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        // Update UI
                        transcriptionText.setText("📝 Transcription:\n" + transcription);
                        lastTranscription = transcription;  // ✅ Store globally
                        Toast.makeText(ProcessAudioActivity.this, "✅ Transcription complete!", Toast.LENGTH_SHORT).show();

                        // ✅ DO NOT auto-generate summary and quiz
                        // User will click buttons to generate each feature separately
                        Log.d("ProcessAudioActivity", "✅ TRANSCRIPTION COMPLETE - Summary will be generated on-demand");

                        // Show summary button (listener already set in onCreate)
                        btnViewSummary.setVisibility(android.view.View.VISIBLE);
                    } else {
                        // Log detailed error response for debugging 422 errors
                        String errorMsg = "Server error: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("ProcessAudioActivity", "Server error response body: " + errorBody);
                                errorMsg = "Error " + response.code() + ": " + errorBody;
                            }
                        } catch (Exception e) {
                            Log.e("ProcessAudioActivity", "Error reading response body", e);
                        }

                        Log.e("ProcessAudioActivity", "Response code: " + response.code() + " - " + response.message());
                        transcriptionText.setText(errorMsg);
                        Toast.makeText(ProcessAudioActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                    progressBar.setVisibility(android.view.View.GONE);
                    processAudioBtn.setEnabled(true);

                    Log.e("ProcessAudioActivity", "❌ NETWORK ERROR OCCURRED");
                    Log.e("ProcessAudioActivity", "Error Type: " + t.getClass().getSimpleName());
                    Log.e("ProcessAudioActivity", "Error Message: " + t.getMessage());
                    Log.e("ProcessAudioActivity", "Target URL: http://192.168.100.9:8000/transcribe/", t);

                    // Print full stack trace for debugging
                    t.printStackTrace();

                    String errorMsg = t.getMessage();
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMsg = "❌ Cannot reach server at 192.168.100.9:8000\n\nCheck:\n1. Is FastAPI running?\n2. Are phone & laptop on same WiFi?\n3. Is Firewall blocking port 8000?";
                    } else if (t.getMessage().contains("timeout")) {
                        errorMsg = "⏱️ Connection timeout - Server is slow or unreachable";
                    }

                    transcriptionText.setText("Error: " + errorMsg);
                    Toast.makeText(ProcessAudioActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(android.view.View.GONE);
            processAudioBtn.setEnabled(true);
            Log.e("ProcessAudioActivity", "Exception: " + e.getMessage(), e);
            transcriptionText.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Generate summary from transcribed text
    private void generateSummary(String transcribedText) {
        if (transcribedText == null || transcribedText.isEmpty()) {
            Log.e("ProcessAudioActivity", "Cannot generate summary - empty text");
            return;
        }

        Log.d("ProcessAudioActivity", "📝 Generating summary...");

        try {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            SummaryRequest request = new SummaryRequest(transcribedText);
            Call<SummaryResponse> call = apiService.generateSummary(request);

            call.enqueue(new Callback<SummaryResponse>() {
                @Override
                public void onResponse(Call<SummaryResponse> call, Response<SummaryResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String summary = response.body().getSummary();
                        Log.d("ProcessAudioActivity", "✅ Summary generated: " + summary);
                        Toast.makeText(ProcessAudioActivity.this, "✅ Summary generated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ProcessAudioActivity", "❌ Summary generation failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<SummaryResponse> call, Throwable t) {
                    Log.e("ProcessAudioActivity", "❌ Summary API error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ProcessAudioActivity", "Summary generation error: " + e.getMessage());
        }
    }

    // Generate quiz from transcribed text
    private void generateQuiz(String transcribedText) {
        if (transcribedText == null || transcribedText.isEmpty()) {
            Log.e("ProcessAudioActivity", "Cannot generate quiz - empty text");
            return;
        }

        Log.d("ProcessAudioActivity", "🎯 GENERATING QUIZ START - recordingId=" + recordingId + ", text=" + transcribedText.length());

        try {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            QuizRequest request = new QuizRequest(transcribedText, 3);
            Log.d("ProcessAudioActivity", "📤 PASSING TO API - /quiz/ endpoint");
            Call<QuizResponse> call = apiService.generateQuiz(request);

            call.enqueue(new Callback<QuizResponse>() {
                @Override
                public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                    Log.d("ProcessAudioActivity", "📥 CALLBACK RECEIVED - Code: " + response.code());
                    
                    try {
                        if (response.body() == null) {
                            Log.e("ProcessAudioActivity", "❌ Quiz response.body() IS NULL!");
                            return;
                        }
                        
                        Log.d("ProcessAudioActivity", "✅ Response body exists, success=" + response.body().isSuccess());
                        
                        if (!response.isSuccessful()) {
                            Log.e("ProcessAudioActivity", "❌ HTTP not successful: " + response.code());
                            return;
                        }
                        
                        if (!response.body().isSuccess()) {
                            Log.e("ProcessAudioActivity", "❌ API success=false");
                            return;
                        }
                        
                        int totalQuestions = response.body().getTotalQuestions();
                        Log.d("ProcessAudioActivity", "✅ QUIZ SUCCESS! " + totalQuestions + " questions");
                        
                        // SAVE QUIZ TO DATABASE
                        Log.d("ProcessAudioActivity", "💾 SAVING QUIZ - recordingId=" + recordingId);
                        
                        if (recordingId <= 0) {
                            Log.e("ProcessAudioActivity", "❌ BAD RECORDING ID: " + recordingId);
                            return;
                        }
                        
                        java.util.List<QuizResponse.QuizQuestionItem> questions = response.body().getQuestions();
                        if (questions == null) {
                            Log.e("ProcessAudioActivity", "❌ Questions list is NULL!");
                            return;
                        }
                        
                        Log.d("ProcessAudioActivity", "✅ Questions list OK - " + questions.size() + " items");
                        
                        // Build JSON
                        StringBuilder quizJson = new StringBuilder("[");
                        for (int i = 0; i < questions.size(); i++) {
                            try {
                                QuizResponse.QuizQuestionItem q = questions.get(i);
                                Log.d("ProcessAudioActivity", "   [Q" + (i+1) + "] " + (q.question != null ? q.question.substring(0, Math.min(50, q.question.length())) : "NULL"));
                                
                                quizJson.append("{")
                                    .append("\"question\":\"").append(q.question != null ? q.question.replace("\"", "\\\"") : "").append("\",")
                                    .append("\"options\":[");
                                
                                if (q.options != null && !q.options.isEmpty()) {
                                    for (int j = 0; j < q.options.size(); j++) {
                                        QuizResponse.OptionItem opt = q.options.get(j);
                                        quizJson.append("{\"option\":\"")
                                            .append(opt.option != null ? opt.option.replace("\"", "\\\"") : "")
                                            .append("\"}");
                                        if (j < q.options.size() - 1) quizJson.append(",");
                                    }
                                }
                                
                                quizJson.append("]");
                                if (q.explanation != null) {
                                    quizJson.append(",\"explanation\":\"").append(q.explanation.replace("\"", "\\\"")).append("\"");
                                }
                                quizJson.append("}");
                                if (i < questions.size() - 1) quizJson.append(",");
                                
                            } catch (Exception ei) {
                                Log.e("ProcessAudioActivity", "❌ ERROR on Q" + (i+1) + ": " + ei.getMessage(), ei);
                            }
                        }
                        quizJson.append("]");
                        
                        Log.d("ProcessAudioActivity", "✅ JSON built - " + quizJson.length() + " chars");
                        
                        // SAVE TO DATABASE
                        DatabaseHelper db = new DatabaseHelper(ProcessAudioActivity.this);
                        boolean saved = db.updateRecordingQuiz((int) recordingId, quizJson.toString());
                        
                        Log.d("ProcessAudioActivity", "💾 DB UPDATE RESULT: " + saved);
                        if (saved) {
                            Log.d("ProcessAudioActivity", "✅✅✅ QUIZ SAVED SUCCESSFULLY TO DB!");
                            Toast.makeText(ProcessAudioActivity.this, "✅ Quiz saved to database!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ProcessAudioActivity", "❌❌❌ DB UPDATE RETURNED FALSE!");
                            Toast.makeText(ProcessAudioActivity.this, "❌ DB save returned false", Toast.LENGTH_SHORT).show();
                        }
                        
                    } catch (Exception eResponse) {
                        Log.e("ProcessAudioActivity", "❌ EXCEPTION IN CALLBACK: " + eResponse.getMessage(), eResponse);
                        Toast.makeText(ProcessAudioActivity.this, "❌ Error: " + eResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<QuizResponse> call, Throwable t) {
                    Log.e("ProcessAudioActivity", "❌ API CALL FAILED: " + t.getMessage(), t);
                    Toast.makeText(ProcessAudioActivity.this, "❌ Quiz API failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ProcessAudioActivity", "❌ EXCEPTION BEFORE CALLBACK: " + e.getMessage(), e);
            Toast.makeText(ProcessAudioActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
