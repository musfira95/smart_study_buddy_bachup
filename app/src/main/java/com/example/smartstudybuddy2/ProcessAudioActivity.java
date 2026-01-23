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
        processAudioBtn.setOnClickListener(v -> uploadToServer());

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

    // Send File to Flask Backend
    private void uploadToServer() {

        File file = convertUriToFile(audioUri);
        if (file == null) {
            Toast.makeText(this, "Audio file error!", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("audio/*"), file);

        MultipartBody.Part audioPart =
                MultipartBody.Part.createFormData(
                        "audiofile",
                        file.getName(),
                        requestBody
                );

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse> call = api.uploadAudio(audioPart);

        transcriptionText.setText("Processing... Please wait...");

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse data = response.body();

                    String finalResult =
                            "📌 TRANSCRIPT:\n" + data.transcript +
                                    "\n\n📌 SUMMARY:\n" + data.summary +
                                    "\n\n📌 QUIZ:\n" + data.quiz.toString();

                    transcriptionText.setText(finalResult);

                } else {
                    transcriptionText.setText("Server error!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                transcriptionText.setText("Failed: " + t.getMessage());
            }
        });
    }
}
