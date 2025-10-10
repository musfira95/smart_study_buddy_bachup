package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UploadAudioActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    Button uploadButton, processButton;
    TextView fileNameText;
    Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_audio);

        uploadButton = findViewById(R.id.uploadButton);
        processButton = findViewById(R.id.processButton);
        fileNameText = findViewById(R.id.fileNameText);

        // Initially disable the process button
        processButton.setEnabled(false);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioPicker();
            }
        });



        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioUri != null){
                    Toast.makeText(UploadAudioActivity.this, "Audio ready for processing!", Toast.LENGTH_SHORT).show();
                    // TODO: Add audio processing logic here
                } else {
                    Toast.makeText(UploadAudioActivity.this, "Please select an audio file first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAudioPicker(){
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select MP3"), PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null){
            audioUri = data.getData();
            if(audioUri != null){
                String name = getFileName(audioUri);

                // Only allow .mp3 files
                if(name.endsWith(".mp3")){
                    fileNameText.setText(name);
                    processButton.setEnabled(true); // enable process button
                } else {
                    Toast.makeText(this, "Please select an MP3 file", Toast.LENGTH_SHORT).show();
                    audioUri = null;
                    processButton.setEnabled(false);
                    fileNameText.setText("No file selected");
                }
            }
        }
    }

    private String getFileName(Uri uri){
        String result = "";
        if(uri.getScheme().equals("content")){
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)){
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if(result.isEmpty()){
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
