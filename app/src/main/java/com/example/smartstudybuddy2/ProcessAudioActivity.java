package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartstudybuddy2.network.ApiClient;
import com.example.smartstudybuddy2.network.ApiService;
import com.example.smartstudybuddy2.network.TranscriptionResponse;

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

            // Create multipart request
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("audio/mpeg"),
                    audioFile
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    audioFile.getName(),
                    requestFile
            );

            // Send to FastAPI server
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<TranscriptionResponse> call = apiService.uploadAudio(body);

            call.enqueue(new Callback<TranscriptionResponse>() {
                @Override
                public void onResponse(Call<TranscriptionResponse> call, Response<TranscriptionResponse> response) {
                    progressBar.setVisibility(android.view.View.GONE);
                    processAudioBtn.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        String transcription = response.body().getText();

                        // Save to database
                        try {
                            DatabaseHelper db = new DatabaseHelper(ProcessAudioActivity.this);
                            db.insertTranscription(fileName, transcription);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Update UI
                        transcriptionText.setText(transcription);
                        Toast.makeText(ProcessAudioActivity.this, "Transcription complete!", Toast.LENGTH_SHORT).show();

                        // Show summary button
                        btnViewSummary.setVisibility(android.view.View.VISIBLE);
                        btnViewSummary.setOnClickListener(v -> {
                            Intent summaryIntent = new Intent(ProcessAudioActivity.this, SummaryActivity.class);
                            summaryIntent.putExtra("transcription", transcription);
                            summaryIntent.putExtra("fileName", fileName);
                            startActivity(summaryIntent);
                        });
                    } else {
                        transcriptionText.setText("Error: Server returned " + response.code());
                        Toast.makeText(ProcessAudioActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                    progressBar.setVisibility(android.view.View.GONE);
                    processAudioBtn.setEnabled(true);
                    transcriptionText.setText("Error: " + t.getMessage());
                    Toast.makeText(ProcessAudioActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(android.view.View.GONE);
            processAudioBtn.setEnabled(true);
            transcriptionText.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
}
