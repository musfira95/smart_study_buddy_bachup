package com.example.smartstudybuddy2;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * StudySessionHelper - Utility class for saving study sessions
 * Handles calculation of metadata and database persistence
 */
public class StudySessionHelper {

    private static final String TAG = "StudySessionHelper";
    private final Context context;
    private final DatabaseHelper dbHelper;

    public StudySessionHelper(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Save a completed study session with all audio processing results
     * @return Session ID if successful, -1 if failed
     */
    public long saveStudySession(
            String fileName,
            String audioPath,
            String transcription,
            String summary,
            String quizJson,
            Uri audioUri) {
        try {
            // Validate inputs
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "Session_" + System.currentTimeMillis();
            }
            if (transcription == null) transcription = "";
            if (summary == null) summary = "";
            if (quizJson == null) quizJson = "[]";

            // Calculate metrics
            int wordCount = calculateWordCount(transcription);
            int duration = getAudioDuration(audioUri);
            String createdDate = getCurrentFormattedDate();

            Log.d(TAG, "💾 Saving Study Session:");
            Log.d(TAG, "  - Title: " + fileName);
            Log.d(TAG, "  - Duration: " + duration + " sec");
            Log.d(TAG, "  - Word Count: " + wordCount);
            Log.d(TAG, "  - Quiz: " + (quizJson.equals("[]") ? "No" : "Yes"));

            // Save to database
            long sessionId = dbHelper.insertStudySession(
                    fileName,
                    audioPath != null ? audioPath : "",
                    transcription,
                    summary,
                    quizJson,
                    createdDate,
                    duration,
                    wordCount,
                    0.0,  // Initial quiz score (user will complete later)
                    false // PDF not generated yet
            );

            if (sessionId > 0) {
                Log.d(TAG, "✅ Study Session saved with ID: " + sessionId);
                return sessionId;
            } else {
                Log.e(TAG, "❌ Failed to insert study session");
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving study session: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Calculate word count from transcription text
     */
    public static int calculateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Get audio file duration in seconds
     * @return Duration in seconds, or 0 if unable to determine
     */
    public int getAudioDuration(Uri audioUri) {
        if (audioUri == null) {
            Log.w(TAG, "Audio URI is null, duration set to 0");
            return 0;
        }

        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, audioUri);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration() / 1000; // Convert milliseconds to seconds
            Log.d(TAG, "✅ Audio duration calculated: " + duration + " seconds");
            return duration;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "❌ Invalid audio URI: " + e.getMessage());
            return 0;
        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ MediaPlayer in invalid state: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting audio duration: " + e.getMessage());
            return 0;
        } finally {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing MediaPlayer: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Get current date as formatted string
     */
    public static String getCurrentFormattedDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return new Date().toString();
        }
    }

    /**
     * Update quiz score after user completes quiz
     */
    public boolean updateQuizScore(int sessionId, double quizScore) {
        try {
            if (quizScore < 0 || quizScore > 100) {
                Log.e(TAG, "Invalid quiz score: " + quizScore);
                return false;
            }
            return dbHelper.updateStudySession(sessionId, quizScore, false);
        } catch (Exception e) {
            Log.e(TAG, "Error updating quiz score: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark PDF as generated for session
     */
    public boolean markPdfGenerated(int sessionId) {
        try {
            StudySession session = dbHelper.getStudySessionById(sessionId);
            if (session != null) {
                return dbHelper.updateStudySession(sessionId, session.getQuizScore(), true);
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error marking PDF generated: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all study sessions
     */
    public java.util.ArrayList<StudySession> getAllSessions() {
        return dbHelper.getAllStudySessions();
    }

    /**
     * Get single session by ID
     */
    public StudySession getSession(int sessionId) {
        return dbHelper.getStudySessionById(sessionId);
    }

    /**
     * Delete session by ID
     */
    public boolean deleteSession(int sessionId) {
        return dbHelper.deleteStudySession(sessionId);
    }
}
