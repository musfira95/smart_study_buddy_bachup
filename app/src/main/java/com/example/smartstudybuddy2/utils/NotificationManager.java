package com.example.smartstudybuddy2.utils;

import android.content.Context;
import android.util.Log;

import com.example.smartstudybuddy2.DatabaseHelper;

/**
 * ✅ Smart Notification Manager
 * Triggers notifications based on real user events and activity
 */
public class NotificationManager {

    private static final String TAG = "NotificationManager";
    private static DatabaseHelper dbHelper;

    public NotificationManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * ✅ Notify when transcription is ready
     */
    public static void notifyTranscriptionReady(String recordingTitle) {
        Log.d(TAG, "🔔 Notification: Transcription ready for '" + recordingTitle + "'");
        
        String title = "📝 Transcription Ready!";
        String message = "Your recording '" + recordingTitle + "' has been transcribed. Ready to summarize? Click to view.";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "transcription");
            if (inserted) {
                Log.d(TAG, "✅ Notification saved to database");
            }
        }
    }

    /**
     * ✅ Notify when quiz is generated/available
     */
    public static void notifyQuizGenerated(String summaryTitle) {
        Log.d(TAG, "🔔 Notification: Quiz generated for '" + summaryTitle + "'");
        
        String title = "🎯 Quiz Ready!";
        String message = "Your quiz for '" + summaryTitle + "' is ready. Test your knowledge now!";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "quiz");
            if (inserted) {
                Log.d(TAG, "✅ Notification saved to database");
            }
        }
    }

    /**
     * ✅ Alert user if quiz score is low (<60%)
     */
    public static void notifyWeakTopic(String category, double score) {
        if (score >= 60) {
            return; // Only notify for low scores
        }
        
        Log.d(TAG, "🔔 Notification: Low score alert - " + category + " (" + String.format("%.1f", score) + "%)");
        
        String title = "⚠️ Low Score Alert";
        String message = "Your score in " + category + " is " + String.format("%.1f", score) + "%. " +
                        "Practice more to improve!";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "warning");
            if (inserted) {
                Log.d(TAG, "✅ Low score notification saved to database");
            }
        }
    }

    /**
     * ✅ Reminder to study if no activity in 24 hours
     */
    public static void notifyStudyReminder() {
        Log.d(TAG, "🔔 Notification: Study reminder - no recent activity");
        
        String title = "📚 Time to Study!";
        String message = "You haven't studied for a while. Create a new recording or practice a quiz!";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "reminder");
            if (inserted) {
                Log.d(TAG, "✅ Study reminder notification saved to database");
            }
        }
    }

    /**
     * ✅ Congratulate on-consistency/streak
     */
    public static void notifyStudyConsistency(int days) {
        Log.d(TAG, "🔔 Notification: Study streak - " + days + " days");
        
        String title = "🔥 Great Streak!";
        String message = "You've been studying for " + days + " consecutive days! Keep going!";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "achievement");
            if (inserted) {
                Log.d(TAG, "✅ Streak notification saved to database");
            }
        }
    }

    /**
     * ✅ New flashcard creation reminder
     */
    public static void notifyFlashcardCreated(String topic) {
        Log.d(TAG, "🔔 Notification: Flashcard created - " + topic);
        
        String title = "🎴 New Flashcard Added";
        String message = "You've added a new flashcard in '" + topic + "'. Start reviewing!";
        
        if (dbHelper != null) {
            boolean inserted = dbHelper.insertNotification(title, message, "flashcard");
            if (inserted) {
                Log.d(TAG, "✅ Flashcard notification saved to database");
            }
        }
    }
}
