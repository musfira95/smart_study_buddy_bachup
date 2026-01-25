package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessAudioActivity extends AppCompatActivity {

    TextView transcriptionText;
    CardView processAudioBtn, viewDetailsBtn;   // ✅ FIXED (Button → CardView)

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

        // RECEIVE DATA
        fileName = getIntent().getStringExtra("fileName");
        fileSize = getIntent().getLongExtra("fileSize", 0);
        audioUriString = getIntent().getStringExtra("audioUri");

        audioUri = Uri.parse(audioUriString);


        // PROCESS AUDIO
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

    // -------------------- SERVER UPLOAD --------------------
    private void uploadAudioToServer() {
        if (audioUri == null) {
            Toast.makeText(this, "No audio selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        transcriptionText.setText("Preparing file for upload...");
        File file = convertUriToFile(audioUri);
        if (file == null) return;

        // Create RequestBody (audio/mpeg for mp3)
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mpeg"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("audiofile", file.getName(), requestFile);

        // Make API Call
        transcriptionText.setText("Uploading & Processing... Please wait.");
        Toast.makeText(this, "Connecting to Server...", Toast.LENGTH_SHORT).show();
        processAudioBtn.setEnabled(false); // Disable button to prevent double clicks

        ApiService service = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse> call = service.uploadAudio(body);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                processAudioBtn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    String trans = apiResponse.transcript;
                    String sum = apiResponse.summary;

                    transcriptionText.setText("Processing Complete!\n\nTranscription:\n" + trans);
                    
                    // Show Summary Button or Navigate
                    Toast.makeText(ProcessAudioActivity.this, "Success!", Toast.LENGTH_SHORT).show();

                    // Navigate to Summary Activity
                    Intent intent = new Intent(ProcessAudioActivity.this, SummaryActivity.class);
                    intent.putExtra("transcriptionText", trans); // Pass transcript
                    // You might want to pass summary too if the server returns it
                    // intent.putExtra("summaryText", sum); 
                    startActivity(intent);

                } else {
                    transcriptionText.setText("Server Error: " + response.message() + " (" + response.code() + ")");
                    Toast.makeText(ProcessAudioActivity.this, "Failed to process audio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                processAudioBtn.setEnabled(true);
                transcriptionText.setText("Connection Failed:\n" + t.getMessage() + "\n\nCheck your IP Address in ApiClient.java or ensure server is running.");
                Toast.makeText(ProcessAudioActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Placeholder for Offline Processing (Vosk) - KEPT FOR REFERENCE BUT UNUSED
    private void processAudioLocally() {
        transcriptionText.setText("Initialization of Offline Model in progress... (Step 1/3)");
        Toast.makeText(this, "Starting Offline Processing...", Toast.LENGTH_SHORT).show();
        // TODO: Initialize Vosk Model and Start Transcription
    }
}
