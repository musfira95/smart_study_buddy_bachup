package com.example.smartstudybuddy2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class RecordMicActivity extends BaseActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 100;

    // ✅ Buttons are TextView now (as per XML)
    private TextView startButton, pauseButton, stopButton;
    private TextView timerText;

    private MediaRecorder recorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private boolean isPaused = false;

    private Handler handler = new Handler();
    private long startTime = 0;
    private long pauseOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mic);

        startButton = findViewById(R.id.btnStartRecording);
        pauseButton = findViewById(R.id.btnPauseRecording);
        stopButton = findViewById(R.id.btnStopRecording);
        timerText = findViewById(R.id.tvTimer);

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        startButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                startRecording();
            } else {
                requestPermissions();
            }
        });

        pauseButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pauseOrResumeRecording();
            } else {
                Toast.makeText(this, "Pause/Resume not supported on this device", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(v -> stopRecording());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                RECORD_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        try {
            File audioDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "SSBRecordings");
            if (!audioDir.exists()) audioDir.mkdirs();

            audioFilePath = audioDir.getAbsolutePath() + "/recorded_" + System.currentTimeMillis() + ".3gp";

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioFilePath);
            recorder.prepare();
            recorder.start();

            isRecording = true;
            isPaused = false;
            startTime = System.currentTimeMillis();
            pauseOffset = 0;
            handler.post(updateTimer);

            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);

            Toast.makeText(this, "Recording Started...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseOrResumeRecording() {
        if (recorder != null && isRecording) {
            if (!isPaused) {
                recorder.pause();
                pauseOffset += System.currentTimeMillis() - startTime;
                isPaused = true;
                pauseButton.setText("▶ Resume Recording");
                handler.removeCallbacks(updateTimer);
                Toast.makeText(this, "Recording Paused", Toast.LENGTH_SHORT).show();
            } else {
                recorder.resume();
                startTime = System.currentTimeMillis();
                isPaused = false;
                pauseButton.setText("⏸ Pause Recording");
                handler.post(updateTimer);
                Toast.makeText(this, "Recording Resumed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }

            isRecording = false;
            isPaused = false;
            handler.removeCallbacks(updateTimer);

            Toast.makeText(this, "Recording Stopped!", Toast.LENGTH_SHORT).show();

            // Dummy transcription
            String transcribedText = "🗒 Transcription: This is a dummy transcription of recorded audio.";
            LastTranscriptionHolder.lastTranscription = transcribedText;

            // Insert into DB (ONLY ONCE)
            DatabaseHelper db = new DatabaseHelper(this);
            db.insertRecording("Recorded Audio", audioFilePath);

            Intent intent = new Intent(RecordMicActivity.this, NotesActivity.class);
            intent.putExtra("TRANSCRIPTION_TEXT", transcribedText);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (isRecording && !isPaused) {
                long elapsedMillis = System.currentTimeMillis() - startTime + pauseOffset;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
                handler.postDelayed(this, 500);
            }
        }
    };
}
