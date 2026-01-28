package com.example.smartstudybuddy2;

import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecordingProcessor - Handles real-time audio recording and processing
 */
public class RecordingProcessor {

    private MediaRecorder mediaRecorder;
    private File audioFile;
    private boolean isRecording = false;
    private long startTime;
    private RecordingListener listener;

    public interface RecordingListener {
        void onRecordingStart();
        void onRecordingStop();
        void onRecordingPause();
        void onRecordingResume();
        void onError(String message);
    }

    public RecordingProcessor(RecordingListener listener) {
        this.listener = listener;
    }

    /**
     * Start recording audio
     */
    public boolean startRecording() {
        try {
            // Create audio file
            audioFile = createAudioFile();

            if (audioFile == null) {
                if (listener != null) listener.onError("Cannot create audio file");
                return false;
            }

            // Initialize MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            startTime = System.currentTimeMillis();

            if (listener != null) listener.onRecordingStart();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError("Recording failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stop recording
     */
    public boolean stopRecording() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                if (listener != null) listener.onRecordingStop();
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError("Stop recording failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Pause recording
     */
    public boolean pauseRecording() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                if (mediaRecorder != null && isRecording) {
                    mediaRecorder.pause();
                    if (listener != null) listener.onRecordingPause();
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError("Pause failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resume recording
     */
    public boolean resumeRecording() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                if (mediaRecorder != null && !isRecording) {
                    mediaRecorder.resume();
                    isRecording = true;
                    if (listener != null) listener.onRecordingResume();
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onError("Resume failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get current recording duration
     */
    public long getRecordingDuration() {
        if (isRecording) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    /**
     * Get audio file
     */
    public File getAudioFile() {
        return audioFile;
    }

    /**
     * Create audio file in app cache
     */
    private File createAudioFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "AUDIO_" + timeStamp + ".mp3";

            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "SmartStudyBuddy");

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            return new File(storageDir, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if currently recording
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Release resources
     */
    public void release() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
