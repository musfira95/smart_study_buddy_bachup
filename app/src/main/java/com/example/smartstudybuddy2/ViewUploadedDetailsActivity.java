package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewUploadedDetailsActivity extends AppCompatActivity {

    TextView fileNameTv, fileSizeTv, uploadTimeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_uploaded_details);

        fileNameTv = findViewById(R.id.fileNameTv);
        fileSizeTv = findViewById(R.id.fileSizeTv);
        uploadTimeTv = findViewById(R.id.uploadTimeTv);

        // Receive data from previous activity
        String fileName = getIntent().getStringExtra("fileName");

        // fileSize ko long se read karo
        long fileSize = getIntent().getLongExtra("fileSize", 0);

        String uploadTime = getIntent().getStringExtra("uploadTime");

        // Show on screen
        fileNameTv.setText(fileName != null ? fileName : "N/A");
        fileSizeTv.setText(fileSize + " KB");
        uploadTimeTv.setText(uploadTime != null ? uploadTime : "N/A");
    }
}