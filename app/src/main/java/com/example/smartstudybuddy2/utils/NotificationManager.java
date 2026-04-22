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

    private static void save(Context context, String title, String message, String type) {
        try {
            DatabaseHelper db = new DatabaseHelper(context);
            db.insertNotification(title, message, type);
            Log.d(TAG, "✅ Notification saved: " + title);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to save notification: " + e.getMessage());
        }
    }

    public static void notifyTranscriptionReady(Context context, String recordingTitle) {
        save(context,
            "📝 Transcription Ready!",
            "Recording '" + recordingTitle + "' has been transcribed. Tap to summarize.",
            "transcription");
    }

    public static void notifySummaryReady(Context context, String recordingTitle) {
        save(context,
            "✨ Summary Ready!",
            "Summary for '" + recordingTitle + "' has been generated. Tap to view.",
            "summary");
    }

    public static void notifyQuizGenerated(Context context, String summaryTitle) {
        save(context,
            "🎯 Quiz Ready!",
            "Your quiz for '" + summaryTitle + "' is ready. Test your knowledge now!",
            "quiz");
    }

    public static void notifyWeakTopic(Context context, String category, double score) {
        if (score >= 60) return;
        save(context,
            "⚠️ Low Score Alert",
            "Your score in '" + category + "' is " + String.format("%.1f", score) + "%. Practice more!",
            "warning");
    }

    public static void notifyStudyReminder(Context context) {
        save(context,
            "📚 Time to Study!",
            "You haven't studied for a while. Create a new recording or practice a quiz!",
            "reminder");
    }

    public static void notifyStudyConsistency(Context context, int days) {
        save(context,
            "🔥 Great Streak!",
            "You've been studying for " + days + " consecutive days! Keep going!",
            "achievement");
    }

    public static void notifyFlashcardCreated(Context context, String topic) {
        save(context,
            "🎴 Flashcards Ready!",
            "Flashcards for '" + topic + "' have been created. Start reviewing!",
            "flashcard");
    }
}
