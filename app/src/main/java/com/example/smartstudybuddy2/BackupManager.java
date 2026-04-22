package com.example.smartstudybuddy2;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * BackupManager - Simple backup functionality for app data
 * Exports summaries, quizzes, and flashcards to JSON format
 */
public class BackupManager {

    private static final String TAG = "BackupManager";

    /**
     * Create and save backup to Downloads folder
     */
    public static boolean createBackup(Context context) {
        try {
            Log.d(TAG, "🔄 Starting backup...");
            
            // Get data from database
            DatabaseHelper db = new DatabaseHelper(context);
            Log.d(TAG, "✅ DatabaseHelper initialized");
            
            // Create JSON object
            JSONObject backup = new JSONObject();
            
            // Add summaries (skip if table doesn't exist)
            ArrayList<String> summaries = new ArrayList<>();
            try {
                summaries = db.getAllSummaries();
                if (summaries == null) summaries = new ArrayList<>();
                Log.d(TAG, "✅ Added " + summaries.size() + " summaries");
            } catch (Exception se) {
                Log.w(TAG, "⚠️ Summaries not available: " + se.getMessage());
            }
            JSONArray summariesArray = new JSONArray(summaries);
            backup.put("summaries", summariesArray);
            
            // Add quizzes (skip if table doesn't exist)
            ArrayList<QuizQuestion> quizzes = new ArrayList<>();
            try {
                quizzes = db.getAllQuizQuestions();
                if (quizzes == null) quizzes = new ArrayList<>();
                Log.d(TAG, "✅ Added " + quizzes.size() + " quizzes");
            } catch (Exception qe) {
                Log.w(TAG, "⚠️ Quiz table not available: " + qe.getMessage());
            }
            JSONArray quizzesArray = new JSONArray();
            for (QuizQuestion q : quizzes) {
                JSONObject qObj = new JSONObject();
                qObj.put("question", q.getQuestion());
                qObj.put("answer", q.getAnswer());
                quizzesArray.put(qObj);
            }
            backup.put("quizzes", quizzesArray);
            
            // Add flashcards (skip if table doesn't exist)
            ArrayList<Flashcard> flashcards = new ArrayList<>();
            try {
                flashcards = db.getAllFlashcardsAsArray();
                if (flashcards == null) flashcards = new ArrayList<>();
                Log.d(TAG, "✅ Added " + flashcards.size() + " flashcards");
            } catch (Exception fe) {
                Log.w(TAG, "⚠️ Flashcards table not available: " + fe.getMessage());
            }
            JSONArray flashcardsArray = new JSONArray();
            for (Flashcard f : flashcards) {
                JSONObject fObj = new JSONObject();
                fObj.put("question", f.getQuestion());
                fObj.put("answer", f.getAnswer());
                flashcardsArray.put(fObj);
            }
            backup.put("flashcards", flashcardsArray);
            
            // Add metadata
            JSONObject metadata = new JSONObject();
            metadata.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            metadata.put("total_summaries", summaries.size());
            metadata.put("total_quizzes", quizzes.size());
            metadata.put("total_flashcards", flashcards.size());
            backup.put("metadata", metadata);
            Log.d(TAG, "✅ Metadata added");
            
            // Save to file (use app's external files directory - no special permissions needed)
            File backupDir = context.getExternalFilesDir(null);
            if (backupDir == null) {
                Log.e(TAG, "❌ External files directory not available");
                return false;
            }
            
            // Create directory if it doesn't exist
            if (!backupDir.exists()) {
                backupDir.mkdirs();
                Log.d(TAG, "✅ Created backup directory: " + backupDir.getAbsolutePath());
            }
            
            String filename = "SmartStudyBuddy_backup.json";
            File backupFile = new File(backupDir, filename);
            
            FileWriter writer = new FileWriter(backupFile);
            writer.write(backup.toString(2)); // Pretty print with 2-space indent
            writer.close();
            
            Log.d(TAG, "✅ Backup saved successfully to: " + backupFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ BACKUP FAILED: " + e.getMessage());
            Log.e(TAG, "Error trace:", e);
            e.printStackTrace();
            return false;
        }
    }
}
