package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecordingDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDate, tvTranscription;
    private View btnPlay, btnPause, btnDelete, btnShare, btnSummarize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_detail);

        tvTitle = findViewById(R.id.tvRecordingTitle);
        tvDate = findViewById(R.id.tvRecordingDate);
        tvTranscription = findViewById(R.id.tvTranscription);

        // 🔹 Buttons are now Views / LinearLayouts
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnDelete = findViewById(R.id.btnDelete);
        btnShare = findViewById(R.id.btnShare);
        btnSummarize = findViewById(R.id.btnSummarize);

        int recordingId = getIntent().getIntExtra("recording_id", -1);

        tvTitle.setText("DummyRecording_" + recordingId + ".mp3");
        tvDate.setText("Date: 06 Dec 2025");
        tvTranscription.setText(
                "This is a dummy transcription for DummyRecording_" + recordingId
        );

        btnPlay.setOnClickListener(v ->
                Toast.makeText(this, "Play clicked", Toast.LENGTH_SHORT).show());

        btnPause.setOnClickListener(v ->
                Toast.makeText(this, "Pause clicked", Toast.LENGTH_SHORT).show());

        btnDelete.setOnClickListener(v ->
                Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show());

        btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show());

        btnSummarize.setOnClickListener(v -> {
            Intent intent = new Intent(this, SummaryActivity.class);
            intent.putExtra("recording_id", recordingId);
            startActivity(intent);
        });
    }
}
