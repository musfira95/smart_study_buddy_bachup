package com.example.smartstudybuddy2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SmartStudyBuddy.db";
    private static final int DB_VERSION = 21; // ✅ v21: Added bookmarked column for bookmarks

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Format timestamp (milliseconds) to readable date string
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        try {
            return new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        } catch (Exception e) {
            android.util.Log.w("DatabaseHelper", "⚠️ Error formatting timestamp: " + e.getMessage());
            return "Unknown";
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // USERS (merged full version)
            db.execSQL("CREATE TABLE Users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE, " +
                    "username TEXT, " +
                    "password TEXT, " +
                    "role TEXT DEFAULT 'user', " +      // from File 1
                    "isBlocked INTEGER DEFAULT 0)");     // from File 2

            // DEFAULT ADMIN
            // DEFAULT ADMIN - Use INSERT OR IGNORE to prevent crash if exists
            db.execSQL("INSERT OR IGNORE INTO Users (email, username, password, role) VALUES (" +
                    "'musfira@gmail.com', 'Musfira', 'Musfira123.', 'admin')");


            // NOTES TABLE (same in both files)
            db.execSQL("CREATE TABLE IF NOT EXISTS notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "content TEXT)");

            // AUDIO FILES (combined version – File1 + File2) - WITH TRANSCRIPTION
            db.execSQL("CREATE TABLE IF NOT EXISTS audio_files (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "file_name TEXT, " +
                    "file_path TEXT, " +
                    "timestamp INTEGER, " +
                    "transcription TEXT DEFAULT NULL, " +
                    "summary TEXT DEFAULT NULL, " +
                    "quiz_json TEXT DEFAULT '[]', " +
                    "topic TEXT DEFAULT 'General', " +
                    "bookmarked INTEGER DEFAULT 0)");

            // STUDY SESSION TABLE (only File 1 had it → added)
            // ✅ UPDATED: Using new enhanced schema with audio processing fields
            db.execSQL(CREATE_STUDY_SESSIONS_TABLE);

            // Additional tables
            db.execSQL(CREATE_PROFILE_TABLE);
            db.execSQL(CREATE_FLASHCARD_TABLE);
            db.execSQL(CREATE_FLASHCARD_STATS_TABLE);  // ✅ Add flashcard stats table
            db.execSQL(CREATE_SCHEDULE_TABLE);
            db.execSQL(CREATE_QUIZ_TABLE);
            db.execSQL(CREATE_QUIZ_RESULTS_TABLE);
            db.execSQL(CREATE_NOTIFICATIONS_TABLE);
            db.execSQL(CREATE_HELP_TABLE);
            db.execSQL(CREATE_ABOUT_TABLE);

            // Insert default help and about content
            insertDefaultHelpContent(db);
            insertDefaultAboutContent(db);
            insertDefaultQuizQuestions(db);
            
            android.util.Log.d("DatabaseHelper", "✅ Database created successfully!");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ CRITICAL ERROR in onCreate: " + e.getMessage());
            e.printStackTrace();
        }

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            android.util.Log.d("DatabaseHelper", "🔄 Upgrading database from version " + oldVersion + " to " + newVersion);
            
            // ✅ GOLDEN RULE: NEVER DROP USER DATA TABLES!
            // These tables contain user's recordings, notes, and study sessions - NEVER DELETE!
            // - audio_files (recordings)
            // - study_sessions (study data)
            // - notes (user notes)
            // - Users (user accounts)
            // - profile (user profile)
            
            // Only ADD new tables that don't exist - use CREATE IF NOT EXISTS
            // This way, old data stays, new features are added
            
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FLASHCARDS + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "question TEXT," +
                        "answer TEXT," +
                        "topic TEXT," +
                        "created_date TEXT)");
                android.util.Log.d("DatabaseHelper", "✅ Flashcards table created or already exists");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ Flashcards: " + e.getMessage());
            }
            
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FLASHCARD_STATS + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "flashcard_id INTEGER," +
                        "review_count INTEGER DEFAULT 0," +
                        "mastery_level INTEGER DEFAULT 1," +
                        "last_reviewed_date TEXT," +
                        "FOREIGN KEY(flashcard_id) REFERENCES flashcards(id))");
                android.util.Log.d("DatabaseHelper", "✅ Flashcard stats table created or already exists");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ Flashcard stats: " + e.getMessage());
            }
            
            // ✅ Add missing columns to schedules table for reminders feature
            try {
                db.execSQL("ALTER TABLE " + TABLE_SCHEDULES + " ADD COLUMN feature_type TEXT");
                android.util.Log.d("DatabaseHelper", "✅ Added feature_type column to schedules");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ feature_type already exists: " + e.getMessage());
            }
            
            try {
                db.execSQL("ALTER TABLE " + TABLE_SCHEDULES + " ADD COLUMN feature_id INTEGER");
                android.util.Log.d("DatabaseHelper", "✅ Added feature_id column to schedules");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ feature_id already exists: " + e.getMessage());
            }
            
            try {
                db.execSQL("ALTER TABLE " + TABLE_SCHEDULES + " ADD COLUMN is_completed INTEGER DEFAULT 0");
                android.util.Log.d("DatabaseHelper", "✅ Added is_completed column to schedules");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ is_completed already exists: " + e.getMessage());
            }

            // ✅ Create notifications table if missing
            try {
                db.execSQL(CREATE_NOTIFICATIONS_TABLE);
                android.util.Log.d("DatabaseHelper", "✅ Notifications table created or already exists");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ Notifications table: " + e.getMessage());
            }
            
            // ✅ v19: Remove old quiz results that were saved with generic 'General' category
            try {
                int deleted = db.delete("quiz_results", "category = ?", new String[]{"General"});
                android.util.Log.d("DatabaseHelper", "✅ Deleted " + deleted + " old General quiz results");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ quiz_results cleanup: " + e.getMessage());
            }

            // ✅ Ensure help_content and about_content tables exist for all versions
            try {
                db.execSQL(CREATE_HELP_TABLE);
                android.util.Log.d("DatabaseHelper", "✅ help_content table created or already exists");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ help_content: " + e.getMessage());
            }
            try {
                db.execSQL(CREATE_ABOUT_TABLE);
                // Insert default row if table is empty
                Cursor ac = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ABOUT_CONTENT, null);
                if (ac.moveToFirst() && ac.getInt(0) == 0) {
                    insertDefaultAboutContent(db);
                }
                ac.close();
                android.util.Log.d("DatabaseHelper", "✅ about_content table created or already exists");
            } catch (Exception e) {
                android.util.Log.d("DatabaseHelper", "ℹ️ about_content: " + e.getMessage());
            }

            android.util.Log.d("DatabaseHelper", "✅✅✅ DATABASE UPGRADE COMPLETE - ALL USER DATA PRESERVED! ✅✅✅");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ ERROR in onUpgrade: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // New method to restore admin if deleted
    public void ensureAdminExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email='musfira@gmail.com'", null);
        if (!cursor.moveToFirst()) {
            db.execSQL("INSERT INTO Users (email, username, password, role) VALUES (" +
                    "'musfira@gmail.com', 'Musfira', 'Musfira123.', 'admin')");
        }
        cursor.close();
        
        // Ensure new tables exist too
        ensureTablesExist();
    }

    public void ensureTablesExist() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_FLASHCARD_TABLE);
        db.execSQL(CREATE_SCHEDULE_TABLE);
        
        // Ensure audio_files table has all required columns
        ensureAudioFilesColumns();
    }

    /**
     * Add missing columns to audio_files table (for existing databases)
     */
    public void ensureAudioFilesColumns() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            // Check if summary column exists
            Cursor cursor = db.rawQuery("PRAGMA table_info(audio_files)", null);
            java.util.Set<String> existingColumns = new java.util.HashSet<>();
            
            while (cursor.moveToNext()) {
                existingColumns.add(cursor.getString(1)); // Column name is at index 1
            }
            cursor.close();
            
            // Add missing columns
            if (!existingColumns.contains("summary")) {
                android.util.Log.d("DatabaseHelper", "📍 Adding missing 'summary' column to audio_files");
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN summary TEXT DEFAULT NULL");
                    android.util.Log.d("DatabaseHelper", "✅ 'summary' column added successfully");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "⚠️ Column 'summary' might already exist: " + e.getMessage());
                }
            }
            
            if (!existingColumns.contains("quiz_json")) {
                android.util.Log.d("DatabaseHelper", "📍 Adding missing 'quiz_json' column to audio_files");
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN quiz_json TEXT DEFAULT '[]'");
                    android.util.Log.d("DatabaseHelper", "✅ 'quiz_json' column added successfully");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "⚠️ Column 'quiz_json' might already exist: " + e.getMessage());
                }
            }

            if (!existingColumns.contains("user_email")) {
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN user_email TEXT DEFAULT NULL");
                    android.util.Log.d("DatabaseHelper", "✅ 'user_email' column added");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "user_email: " + e.getMessage());
                }
            }

            if (!existingColumns.contains("source")) {
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN source TEXT DEFAULT 'recorded'");
                    android.util.Log.d("DatabaseHelper", "✅ 'source' column added");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "source: " + e.getMessage());
                }
            }
            
            if (!existingColumns.contains("topic")) {
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN topic TEXT DEFAULT 'General'");
                    android.util.Log.d("DatabaseHelper", "✅ 'topic' column added for topic detection");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "topic: " + e.getMessage());
                }
            }

            if (!existingColumns.contains("bookmarked")) {
                try {
                    db.execSQL("ALTER TABLE audio_files ADD COLUMN bookmarked INTEGER DEFAULT 0");
                    android.util.Log.d("DatabaseHelper", "✅ 'bookmarked' column added for bookmarks");
                } catch (Exception e) {
                    android.util.Log.w("DatabaseHelper", "bookmarked: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error ensuring audio_files columns: " + e.getMessage());
        }
    }


    // -----------------------------------------------------------------
    // USERS (Merge of BOTH files)
    // -----------------------------------------------------------------

    public boolean insertUser(String email, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email=?", new String[]{email});
        if (cursor.moveToFirst()) {
            cursor.close();
            return false;  // email exists
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);

        return db.insert("Users", null, cv) != -1;
    }
    // ✅ Update user role
    public boolean updateUserRole(String email, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("role", newRole);

        int rows = db.update("users", cv, "email=?", new String[]{email});
        return rows > 0; // true if updated
    }


    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Users WHERE email=? AND password=?",
                new String[]{email, password}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM Users WHERE email=?", new String[]{email});
        String role = "user";
        if (cursor.moveToFirst()) role = cursor.getString(0);
        cursor.close();
        return role;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT email FROM Users WHERE email=?", new String[]{email});
        boolean ex = c.moveToFirst();
        c.close();
        return ex;
    }

    public boolean isEmailExists(String email) {
        return checkEmailExists(email);
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        return db.update("Users", cv, "email=?", new String[]{email}) > 0;
    }

    // from File 1 (update full user)
    public boolean updateUser(String oldEmail, String newEmail, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", newEmail);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        return db.update("Users", cv, "email=?", new String[]{oldEmail}) > 0;
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Users", "email=?", new String[]{email}) > 0;
    }

    // Overloaded method for updating user by ID (used in EditUserActivity)
    public boolean updateUser(int userId, String email, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        return db.update("Users", cv, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    // Delete user by ID (used in EditUserActivity)
    public boolean deleteUserById(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Users", "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT username, email, role, password, isBlocked FROM Users",
                null
        );
    }

    // -----------------------------------------------------------------
    // EXTRA USER FEATURES (File 2 ONLY)
    // -----------------------------------------------------------------
    public void saveOrUpdateProfile(
            String name, String email, String phone,
            String university, String course, String semester,
            String studyPref, String dailyGoal, String language
    ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("phone", phone);
        cv.put("university", university);
        cv.put("course", course);
        cv.put("semester", semester);
        cv.put("study_preference", studyPref);
        cv.put("daily_goal", dailyGoal);
        cv.put("language", language);

        Cursor cursor = db.rawQuery("SELECT * FROM profile", null);

        if (cursor.getCount() > 0) {
            db.update("profile", cv, null, null);
        } else {
            db.insert("profile", null, cv);
        }

        cursor.close();
    }

    public boolean updateProfileName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        int rows = db.update("profile", cv, "email = ?", new String[]{email});
        if (rows == 0) {
            // No row for this email yet — insert a minimal row
            cv.put("email", email);
            db.insert("profile", null, cv);
        }
        return true;
    }


    public static final String TABLE_PROFILE = "profile";

    public static final String CREATE_PROFILE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILE + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "email TEXT," +
                    "phone TEXT," +
                    "university TEXT," +
                    "course TEXT," +
                    "semester TEXT," +
                    "study_preference TEXT," +
                    "daily_goal TEXT," +
                    "language TEXT)";

    // FLASHCARDS
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String CREATE_FLASHCARD_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FLASHCARDS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT," +
                    "answer TEXT," +
                    "topic TEXT," +
                    "created_date TEXT)";

    // ✅ FLASHCARD STATS (Track mastery and reviews)
    public static final String TABLE_FLASHCARD_STATS = "flashcard_stats";
    public static final String CREATE_FLASHCARD_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FLASHCARD_STATS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "flashcard_id INTEGER," +
                    "review_count INTEGER DEFAULT 0," +
                    "mastery_level INTEGER DEFAULT 1," +
                    "last_reviewed_date TEXT," +
                    "FOREIGN KEY(flashcard_id) REFERENCES flashcards(id))";

    // SCHEDULES / TIMETABLE
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String CREATE_SCHEDULE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SCHEDULES + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "description TEXT," +
                    "time TEXT," +
                    "date TEXT," +
                    "feature_type TEXT," +
                    "feature_id INTEGER," +
                    "is_completed INTEGER DEFAULT 0)";

    // QUIZ QUESTIONS
    public static final String TABLE_QUIZ_QUESTIONS = "quiz_questions";
    public static final String CREATE_QUIZ_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_QUESTIONS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT," +
                    "optionA TEXT," +
                    "optionB TEXT," +
                    "optionC TEXT," +
                    "optionD TEXT," +
                    "answer TEXT," +
                    "category TEXT," +
                    "difficulty TEXT," +
                    "created_date TEXT)";

    // QUIZ RESULTS (Track user scores and performance)
    public static final String TABLE_QUIZ_RESULTS = "quiz_results";
    public static final String CREATE_QUIZ_RESULTS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_RESULTS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category TEXT," +
                    "correct_count INTEGER," +
                    "wrong_count INTEGER," +
                    "total_questions INTEGER," +
                    "score_percentage REAL," +
                    "duration_seconds INTEGER," +
                    "created_date TEXT)";

    // NOTIFICATIONS
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String CREATE_NOTIFICATIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "message TEXT," +
                    "timestamp TEXT," +
                    "type TEXT," +
                    "is_read INTEGER DEFAULT 0)";

    // ✅ STUDY SESSIONS (Audio processing results)
    public static final String TABLE_STUDY_SESSIONS = "study_sessions";
    public static final String CREATE_STUDY_SESSIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STUDY_SESSIONS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "audio_path TEXT," +
                    "transcription TEXT," +
                    "summary TEXT," +
                    "quiz_json TEXT DEFAULT '[]'," +
                    "created_date TEXT," +
                    "duration INTEGER DEFAULT 0," +
                    "word_count INTEGER DEFAULT 0," +
                    "quiz_score REAL DEFAULT 0.0," +
                    "pdf_generated INTEGER DEFAULT 0)";

    // HELP & SUPPORT CONTENT
    public static final String TABLE_HELP_CONTENT = "help_content";
    public static final String CREATE_HELP_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_HELP_CONTENT + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "content TEXT," +
                    "category TEXT," +
                    "last_updated TEXT)";

    // ABOUT CONTENT
    public static final String TABLE_ABOUT_CONTENT = "about_content";
    public static final String CREATE_ABOUT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ABOUT_CONTENT + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "app_name TEXT," +
                    "app_version TEXT," +
                    "description TEXT," +
                    "support_email TEXT," +
                    "support_phone TEXT," +
                    "last_updated TEXT)";

    public boolean insertFlashcard(String question, String answer, String topic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", question);
        cv.put("answer", answer);
        cv.put("topic", topic);
        cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        return db.insert(TABLE_FLASHCARDS, null, cv) != -1;
    }

    public Cursor getAllFlashcards() {
        SQLiteDatabase db = this.getReadableDatabase();
        // CursorAdapter requires a column named "_id"
        return db.rawQuery("SELECT id AS _id, * FROM " + TABLE_FLASHCARDS, null);
    }
    
    /**
     * ✅ Get all flashcards as ArrayList (for SearchActivity and other features)
     */
    public ArrayList<Flashcard> getAllFlashcardsAsArray() {
        ArrayList<Flashcard> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FLASHCARDS + " ORDER BY created_date DESC", null);

        if (c.moveToFirst()) {
            do {
                Flashcard flashcard = new Flashcard(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("question")),
                    c.getString(c.getColumnIndexOrThrow("answer")),
                    c.getString(c.getColumnIndexOrThrow("topic")),
                    c.getString(c.getColumnIndexOrThrow("created_date"))
                );
                list.add(flashcard);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    /**
     * Get flashcards created after a specific recording timestamp
     * This helps ensure we only get flashcards related to the current recording
     */
    public ArrayList<Flashcard> getFlashcardsForRecording(int recordingId) {
        ArrayList<Flashcard> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            
            // Get the recording's timestamp first
            Cursor recordingCursor = db.rawQuery("SELECT timestamp FROM audio_files WHERE id=?", 
                                                 new String[]{String.valueOf(recordingId)});
            long recordingTimestamp = 0;
            if (recordingCursor.moveToFirst()) {
                recordingTimestamp = recordingCursor.getLong(recordingCursor.getColumnIndexOrThrow("timestamp"));
            }
            recordingCursor.close();
            
            android.util.Log.d("DatabaseHelper", "📚 Fetching flashcards for recording ID " + recordingId + " (timestamp: " + recordingTimestamp + ")");
            
            // Get flashcards created after this recording
            // This filters out old dummy data and only shows recent flashcards
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FLASHCARDS + 
                                  " WHERE created_date > datetime(" + recordingTimestamp + "/1000, 'unixepoch')" +
                                  " ORDER BY created_date DESC", null);
            
            if (c.moveToFirst()) {
                do {
                    Flashcard flashcard = new Flashcard(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("question")),
                        c.getString(c.getColumnIndexOrThrow("answer")),
                        c.getString(c.getColumnIndexOrThrow("topic")),
                        c.getString(c.getColumnIndexOrThrow("created_date"))
                    );
                    list.add(flashcard);
                } while (c.moveToNext());
            }
            c.close();
            
            android.util.Log.d("DatabaseHelper", "✅ Found " + list.size() + " flashcards for recording ID " + recordingId);
        } catch (Exception e) {
            android.util.Log.w("DatabaseHelper", "⚠️ Error getting flashcards for recording: " + e.getMessage());
            // Fallback to all flashcards if filtering fails
            return getAllFlashcardsAsArray();
        }
        return list;
    }

    // ====================================================================
    // ✅ FLASHCARD STATS MANAGEMENT (Track mastery and reviews)
    // ====================================================================

    /**
     * Record a flashcard review and update mastery level
     */
    public boolean recordFlashcardReview(int flashcardId, int masteryLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if stat already exists
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FLASHCARD_STATS + " WHERE flashcard_id=?", 
                              new String[]{String.valueOf(flashcardId)});
        boolean exists = c.moveToFirst();
        c.close();

        ContentValues cv = new ContentValues();
        cv.put("mastery_level", masteryLevel);
        cv.put("last_reviewed_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        if (exists) {
            // Update existing stat
            Cursor reviewCount = db.rawQuery("SELECT review_count FROM " + TABLE_FLASHCARD_STATS + 
                                            " WHERE flashcard_id=?", 
                                            new String[]{String.valueOf(flashcardId)});
            int currentCount = 0;
            if (reviewCount.moveToFirst()) {
                currentCount = reviewCount.getInt(0);
            }
            reviewCount.close();
            
            cv.put("review_count", currentCount + 1);
            boolean updated = db.update(TABLE_FLASHCARD_STATS, cv, "flashcard_id=?", 
                                       new String[]{String.valueOf(flashcardId)}) > 0;
            
            if (updated) {
                android.util.Log.d("DatabaseHelper", "✅ Flashcard stat updated: ID=" + flashcardId + 
                                  ", Mastery=" + masteryLevel);
            }
            return updated;
        } else {
            // Create new stat
            cv.put("flashcard_id", flashcardId);
            cv.put("review_count", 1);
            
            boolean inserted = db.insert(TABLE_FLASHCARD_STATS, null, cv) != -1;
            if (inserted) {
                android.util.Log.d("DatabaseHelper", "✅ Flashcard stat created: ID=" + flashcardId + 
                                  ", Mastery=" + masteryLevel);
            }
            return inserted;
        }
    }

    /**
     * Get flashcard stats
     */
    public FlashcardStat getFlashcardStat(int flashcardId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FLASHCARD_STATS + " WHERE flashcard_id=?", 
                              new String[]{String.valueOf(flashcardId)});
        
        FlashcardStat stat = null;
        if (c.moveToFirst()) {
            stat = new FlashcardStat(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getInt(c.getColumnIndexOrThrow("flashcard_id")),
                c.getInt(c.getColumnIndexOrThrow("review_count")),
                c.getInt(c.getColumnIndexOrThrow("mastery_level")),
                c.getString(c.getColumnIndexOrThrow("last_reviewed_date"))
            );
        }
        c.close();
        return stat;
    }

    /**
     * Get all flashcards stats with mastery summary
     */
    public ArrayList<FlashcardStat> getAllFlashcardStats() {
        ArrayList<FlashcardStat> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FLASHCARD_STATS + " ORDER BY mastery_level DESC", null);

        if (c.moveToFirst()) {
            do {
                FlashcardStat stat = new FlashcardStat(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getInt(c.getColumnIndexOrThrow("flashcard_id")),
                    c.getInt(c.getColumnIndexOrThrow("review_count")),
                    c.getInt(c.getColumnIndexOrThrow("mastery_level")),
                    c.getString(c.getColumnIndexOrThrow("last_reviewed_date"))
                );
                list.add(stat);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    /**
     * Get count of mastered flashcards
     */
    public int getMasteredFlashcardCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FLASHCARD_STATS + " WHERE mastery_level=5", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
    
    public Cursor getFlashcardsByTopic(String topic) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FLASHCARDS + " WHERE topic=?", new String[]{topic});
    }

    public boolean insertSchedule(String title, String description, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("time", time);
        cv.put("date", date);
        return db.insert(TABLE_SCHEDULES, null, cv) != -1;
    }

    public Cursor getAllSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SCHEDULES, null);
    }
    
    public Cursor getSchedulesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SCHEDULES + " WHERE date=?", new String[]{date});
    }

    // ====================================================================
    // ✅ TIMETABLE/REMINDERS - New Methods for Enhanced Scheduling
    // ====================================================================

    /**
     * Insert reminder with feature linking (Recording/Flashcard/Quiz/PDF)
     */
    public long insertScheduleWithFeature(String title, String description, String date, 
                                          String time, String featureType, int featureId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("title", title);
            cv.put("description", description);
            cv.put("date", date);
            cv.put("time", time);
            cv.put("feature_type", featureType != null ? featureType : "reminder");
            cv.put("feature_id", featureId > 0 ? featureId : null);
            cv.put("is_completed", 0);
            
            long result = db.insert(TABLE_SCHEDULES, null, cv);
            if (result != -1) {
                android.util.Log.d("DatabaseHelper", "✅ Reminder created: " + title + " (Type: " + featureType + ")");
            }
            return result;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error inserting schedule with feature: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get all reminders as ArrayList
     */
    public ArrayList<ScheduleReminder> getAllScheduleReminders() {
        ArrayList<ScheduleReminder> list = new ArrayList<>();
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db == null) {
                android.util.Log.e("DatabaseHelper", "❌ Database is null");
                return list;
            }
            
            c = db.rawQuery("SELECT id, title, description, date, time, feature_type, feature_id, is_completed " +
                                  "FROM " + TABLE_SCHEDULES + " ORDER BY date DESC, time ASC", null);
            
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        ScheduleReminder reminder = new ScheduleReminder(
                            c.getInt(c.getColumnIndexOrThrow("id")),
                            c.getString(c.getColumnIndexOrThrow("title")),
                            c.getString(c.getColumnIndexOrThrow("description")),
                            c.getString(c.getColumnIndexOrThrow("date")),
                            c.getString(c.getColumnIndexOrThrow("time")),
                            c.getString(c.getColumnIndexOrThrow("feature_type")),
                            c.getInt(c.getColumnIndexOrThrow("feature_id")),
                            c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1
                        );
                        list.add(reminder);
                    } catch (Exception e) {
                        android.util.Log.e("DatabaseHelper", "Error parsing reminder row: " + e.getMessage());
                    }
                } while (c.moveToNext());
            }
            android.util.Log.d("DatabaseHelper", "✅ Loaded " + list.size() + " reminders");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error loading reminders: " + e.getMessage(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     * Get today's reminders
     */
    public ArrayList<ScheduleReminder> getTodaySchedules() {
        ArrayList<ScheduleReminder> list = new ArrayList<>();
        Cursor c = null;
        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            SQLiteDatabase db = this.getReadableDatabase();
            if (db == null) {
                android.util.Log.e("DatabaseHelper", "❌ Database is null");
                return list;
            }
            
            c = db.rawQuery("SELECT id, title, description, date, time, feature_type, feature_id, is_completed " +
                                  "FROM " + TABLE_SCHEDULES + " WHERE date=? ORDER BY time ASC", 
                                  new String[]{today});
            
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        ScheduleReminder reminder = new ScheduleReminder(
                            c.getInt(c.getColumnIndexOrThrow("id")),
                            c.getString(c.getColumnIndexOrThrow("title")),
                            c.getString(c.getColumnIndexOrThrow("description")),
                            c.getString(c.getColumnIndexOrThrow("date")),
                            c.getString(c.getColumnIndexOrThrow("time")),
                            c.getString(c.getColumnIndexOrThrow("feature_type")),
                            c.getInt(c.getColumnIndexOrThrow("feature_id")),
                            c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1
                        );
                        list.add(reminder);
                    } catch (Exception e) {
                        android.util.Log.e("DatabaseHelper", "Error parsing today's reminder: " + e.getMessage());
                    }
                } while (c.moveToNext());
            }
            android.util.Log.d("DatabaseHelper", "✅ Loaded " + list.size() + " today's reminders");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error loading today's reminders: " + e.getMessage(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     * Get upcoming reminders (future dates only)
     */
    public ArrayList<ScheduleReminder> getUpcomingSchedules() {
        ArrayList<ScheduleReminder> list = new ArrayList<>();
        Cursor c = null;
        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            SQLiteDatabase db = this.getReadableDatabase();
            if (db == null) {
                android.util.Log.e("DatabaseHelper", "❌ Database is null");
                return list;
            }
            
            c = db.rawQuery("SELECT id, title, description, date, time, feature_type, feature_id, is_completed " +
                                  "FROM " + TABLE_SCHEDULES + " WHERE date > ? ORDER BY date ASC, time ASC", 
                                  new String[]{today});
            
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        ScheduleReminder reminder = new ScheduleReminder(
                            c.getInt(c.getColumnIndexOrThrow("id")),
                            c.getString(c.getColumnIndexOrThrow("title")),
                            c.getString(c.getColumnIndexOrThrow("description")),
                            c.getString(c.getColumnIndexOrThrow("date")),
                            c.getString(c.getColumnIndexOrThrow("time")),
                            c.getString(c.getColumnIndexOrThrow("feature_type")),
                            c.getInt(c.getColumnIndexOrThrow("feature_id")),
                            c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1
                        );
                        list.add(reminder);
                    } catch (Exception e) {
                        android.util.Log.e("DatabaseHelper", "Error parsing upcoming reminder: " + e.getMessage());
                    }
                } while (c.moveToNext());
            }
            android.util.Log.d("DatabaseHelper", "✅ Loaded " + list.size() + " upcoming reminders");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error loading upcoming reminders: " + e.getMessage(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     * Mark reminder as completed
     */
    public boolean markReminderCompleted(int reminderId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("is_completed", 1);
            
            int updated = db.update(TABLE_SCHEDULES, cv, "id=?", new String[]{String.valueOf(reminderId)});
            
            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Reminder marked completed: ID " + reminderId);
                return true;
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error marking reminder completed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete reminder by ID
     */
    public boolean deleteScheduleReminder(int reminderId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int deleted = db.delete(TABLE_SCHEDULES, "id=?", new String[]{String.valueOf(reminderId)});
            
            if (deleted > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Reminder deleted: ID " + reminderId);
                return true;
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error deleting reminder: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get reminder by ID
     */
    public ScheduleReminder getScheduleReminderById(int id) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT id, title, description, date, time, feature_type, feature_id, is_completed " +
                                  "FROM " + TABLE_SCHEDULES + " WHERE id=?", 
                                  new String[]{String.valueOf(id)});
            
            if (c.moveToFirst()) {
                ScheduleReminder reminder = new ScheduleReminder(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("title")),
                    c.getString(c.getColumnIndexOrThrow("description")),
                    c.getString(c.getColumnIndexOrThrow("date")),
                    c.getString(c.getColumnIndexOrThrow("time")),
                    c.getString(c.getColumnIndexOrThrow("feature_type")),
                    c.getInt(c.getColumnIndexOrThrow("feature_id")),
                    c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1
                );
                c.close();
                return reminder;
            }
            c.close();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error getting reminder: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update reminder
     */
    public boolean updateScheduleReminder(int reminderId, String title, String description, 
                                          String date, String time, String featureType, int featureId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("title", title);
            cv.put("description", description);
            cv.put("date", date);
            cv.put("time", time);
            cv.put("feature_type", featureType != null ? featureType : "reminder");
            cv.put("feature_id", featureId > 0 ? featureId : null);
            
            int updated = db.update(TABLE_SCHEDULES, cv, "id=?", new String[]{String.valueOf(reminderId)});
            
            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Reminder updated: ID " + reminderId);
                return true;
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error updating reminder: " + e.getMessage());
            return false;
        }
    }

    public boolean isLastAdmin(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM Users WHERE role='admin'", null);
        c1.moveToFirst();
        int count = c1.getInt(0);
        c1.close();

        if (count == 1) {
            Cursor c2 = db.rawQuery("SELECT email FROM Users WHERE role='admin' LIMIT 1", null);
            boolean last = false;
            if (c2.moveToFirst())
                last = c2.getString(0).equalsIgnoreCase(email);

            c2.close();
            return last;
        }
        return false;
    }

    // BLOCK FEATURE (File 2)
    public boolean isUserBlocked(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT isBlocked FROM Users WHERE email=?", new String[]{email});
        if (c.moveToFirst()) {
            boolean b = c.getInt(0) == 1;
            c.close();
            return b;
        }
        c.close();
        return false;
    }

    public boolean updateBlockStatus(String email, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isBlocked", status);
        return db.update("Users", cv, "email=?", new String[]{email}) > 0;
    }

    // -----------------------------------------------------------------
    // NOTES (Same features from both files)
    // -----------------------------------------------------------------

    public boolean insertNote(String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        return db.insert("notes", null, cv) != -1;
    }

    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM notes", null);
    }

    public boolean updateNote(int id, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        return db.update("notes", cv, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("notes", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // -----------------------------------------------------------------
    // TRANSCRIPTION & SUMMARY METHODS (NEW - Backend Processing)
    // -----------------------------------------------------------------

    /**
     * Insert transcription from audio processing
     */
    public boolean insertTranscription(String fileName, String transcriptionText) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            // ✅ Update audio_files table with transcription
            ContentValues cv = new ContentValues();
            cv.put("transcription", transcriptionText);

            android.util.Log.d("DatabaseHelper", "🔍 Searching for file_name: " + fileName);
            android.util.Log.d("DatabaseHelper", "   Transcription length: " + (transcriptionText != null ? transcriptionText.length() : 0) + " chars");

            int updated = db.update("audio_files", cv, "file_name=?", new String[]{fileName});

            android.util.Log.d("DatabaseHelper", "✅ Updated rows: " + updated);

            // If no update (new transcription without recording), insert to notes table as backup
            if (updated == 0) {
                android.util.Log.w("DatabaseHelper", "⚠️ No matching file_name found, checking database...");

                // Log all files in database for debugging
                Cursor allFiles = db.rawQuery("SELECT id, file_name FROM audio_files ORDER BY timestamp DESC LIMIT 5", null);
                if (allFiles.moveToFirst()) {
                    android.util.Log.d("DatabaseHelper", "Recent files in database:");
                    do {
                        String storedName = allFiles.getString(allFiles.getColumnIndexOrThrow("file_name"));
                        android.util.Log.d("DatabaseHelper", "  - ID=" + allFiles.getInt(0) + ", file_name=" + storedName);
                    } while (allFiles.moveToNext());
                }
                allFiles.close();

                // Insert to notes table as backup
                ContentValues cv2 = new ContentValues();
                cv2.put("title", fileName != null ? fileName : "Transcription_" + System.currentTimeMillis());
                cv2.put("content", transcriptionText);
                long inserted = db.insert("notes", null, cv2);
                android.util.Log.d("DatabaseHelper", "📝 Fallback insert to notes table: " + (inserted != -1 ? "SUCCESS" : "FAILED"));
                return inserted != -1;
            }

            android.util.Log.d("DatabaseHelper", "✅ Transcription saved successfully to audio_files");
            return true;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in insertTranscription: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ NEW METHOD - Update transcription by recording ID (More reliable)
     */
    public boolean updateTranscriptionById(int recordingId, String transcriptionText) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "🔍 Updating transcription for ID: " + recordingId);
            android.util.Log.d("DatabaseHelper", "   Transcription length: " + (transcriptionText != null ? transcriptionText.length() : 0) + " chars");

            ContentValues cv = new ContentValues();
            cv.put("transcription", transcriptionText);

            int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});

            android.util.Log.d("DatabaseHelper", "✅ Updated rows: " + updated);

            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Transcription updated successfully by ID: " + recordingId);
                
                // Verify the update
                Cursor c = db.rawQuery("SELECT transcription FROM audio_files WHERE id=?", new String[]{String.valueOf(recordingId)});
                if (c.moveToFirst()) {
                    String saved = c.getString(c.getColumnIndexOrThrow("transcription"));
                    android.util.Log.d("DatabaseHelper", "✅ VERIFIED: Saved transcription length: " + (saved != null ? saved.length() : 0));
                }
                c.close();
                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No record found with ID: " + recordingId);
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in updateTranscriptionById: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 🔹 NEW METHOD - Update file path by recording ID (fixes uploaded file playback issues)
     */
    public boolean updateRecordingFilePath(int recordingId, String filePath) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "🔧 Updating file path for ID: " + recordingId);
            android.util.Log.d("DatabaseHelper", "   New file path: " + filePath);

            ContentValues cv = new ContentValues();
            cv.put("file_path", filePath);

            int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});

            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ File path updated successfully: " + filePath);
                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No record found with ID: " + recordingId);
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in updateRecordingFilePath: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update quiz JSON for a recording
     */
    public boolean updateRecordingQuiz(int recordingId, String quizJson) {
        try {
            // Ensure columns exist first
            ensureAudioFilesColumns();
            
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "🎯 Updating quiz for ID: " + recordingId);
            android.util.Log.d("DatabaseHelper", "   Quiz JSON length: " + (quizJson != null ? quizJson.length() : 0));

            ContentValues cv = new ContentValues();
            cv.put("quiz_json", quizJson != null ? quizJson : "[]");

            int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});

            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Quiz updated successfully for recording ID: " + recordingId);
                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No record found with ID: " + recordingId);
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in updateRecordingQuiz: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ Update summary directly in audio_files table (for PDF export)
     */
    public boolean updateRecordingSummary(int recordingId, String summaryText) {
        try {
            ensureAudioFilesColumns();
            
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "📝 Updating summary for recording ID: " + recordingId);
            android.util.Log.d("DatabaseHelper", "   Summary length: " + (summaryText != null ? summaryText.length() : 0));

            ContentValues cv = new ContentValues();
            cv.put("summary", summaryText != null ? summaryText : "");

            int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});

            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Summary updated successfully for recording ID: " + recordingId);
                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No record found with ID: " + recordingId);
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in updateRecordingSummary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update topic for a recording (from topic detection)
     */
    public boolean updateRecordingTopic(int recordingId, String topic) {
        try {
            ensureAudioFilesColumns();
            
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "🎯 Updating topic for recording ID: " + recordingId);
            android.util.Log.d("DatabaseHelper", "   Topic: " + (topic != null ? topic : "General"));

            ContentValues cv = new ContentValues();
            cv.put("topic", topic != null ? topic : "General");

            int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});

            if (updated > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Topic updated successfully for recording ID: " + recordingId);
                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No record found with ID: " + recordingId);
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in updateRecordingTopic: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Toggle bookmark status for a recording
     */
    public boolean toggleBookmark(int recordingId) {
        try {
            ensureAudioFilesColumns();
            
            SQLiteDatabase db = this.getWritableDatabase();
            
            // Get current bookmark status
            Cursor cursor = db.rawQuery("SELECT bookmarked FROM audio_files WHERE id=?", new String[]{String.valueOf(recordingId)});
            
            if (cursor.moveToFirst()) {
                int currentStatus = cursor.getInt(0);
                int newStatus = currentStatus == 0 ? 1 : 0;
                
                ContentValues cv = new ContentValues();
                cv.put("bookmarked", newStatus);
                
                int updated = db.update("audio_files", cv, "id=?", new String[]{String.valueOf(recordingId)});
                cursor.close();
                
                if (updated > 0) {
                    android.util.Log.d("DatabaseHelper", "⭐ Bookmark toggled for recording ID: " + recordingId + " (now: " + newStatus + ")");
                    return true;
                }
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in toggleBookmark: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a recording is bookmarked
     */
    public boolean isBookmarked(int recordingId) {
        try {
            ensureAudioFilesColumns();
            
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT bookmarked FROM audio_files WHERE id=?", new String[]{String.valueOf(recordingId)});
            
            if (cursor.moveToFirst()) {
                int bookmarked = cursor.getInt(0);
                cursor.close();
                return bookmarked == 1;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in isBookmarked: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all bookmarked recordings
     */
    public ArrayList<Recording> getBookmarkedRecordings() {
        ArrayList<Recording> bookmarkedRecordings = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = this.getReadableDatabase();
            
            android.util.Log.d("DatabaseHelper", "⭐ Fetching bookmarked recordings from database...");
            c = db.rawQuery("SELECT * FROM audio_files WHERE bookmarked=1 ORDER BY timestamp DESC", null);
            
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        String transcription = null;
                        try {
                            transcription = c.getString(c.getColumnIndexOrThrow("transcription"));
                        } catch (Exception e) {
                            transcription = null;
                        }
                        
                        long timestamp = 0;
                        try {
                            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                        } catch (Exception e) {
                            timestamp = 0;
                        }
                        
                        String dateFormatted = formatTimestamp(timestamp);
                        int recordingId = c.getInt(c.getColumnIndexOrThrow("id"));
                        String title = c.getString(c.getColumnIndexOrThrow("file_name"));
                        String filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
                        
                        Recording recording = new Recording(
                                recordingId,
                                title != null ? title : "Untitled",
                                filePath != null ? filePath : "",
                                dateFormatted,
                                0,
                                transcription != null ? transcription : ""
                        );
                        bookmarkedRecordings.add(recording);
                        android.util.Log.d("DatabaseHelper", "  ⭐ Bookmarked Recording ID=" + recordingId + ", Title=" + title);
                    } catch (Exception itemError) {
                        android.util.Log.w("DatabaseHelper", "⚠️ Error processing bookmarked item: " + itemError.getMessage());
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in getBookmarkedRecordings: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
        return bookmarkedRecordings;
    }

    /**
     * Insert summary generated from transcription
     */
    public boolean insertSummary(String fileName, String summaryText, String originalTranscription) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("title", (fileName != null ? fileName : "Summary") + " - Summary");
            cv.put("content", "ORIGINAL:\n" + originalTranscription + "\n\nSUMMARY:\n" + summaryText);
            long result = db.insert("notes", null, cv);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ NEW: Get all summaries for backup
     */
    public ArrayList<String> getAllSummaries() {
        ArrayList<String> summaries = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT summary FROM audio_files WHERE summary IS NOT NULL", null);
            
            if (cursor.moveToFirst()) {
                do {
                    String summary = cursor.getString(0);
                    if (summary != null && !summary.isEmpty()) {
                        summaries.add(summary);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching summaries: " + e.getMessage());
        }
        return summaries;
    }

    /**
     * Get transcription by title
     */
    public String getTranscription(String fileName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query("notes", new String[]{"content"},
                "title=?", new String[]{fileName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String content = cursor.getString(cursor.getColumnIndex("content"));
                cursor.close();
                return content;
            }
            if (cursor != null) cursor.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all transcriptions
     */
    public ArrayList<String> getAllTranscriptions() {
        ArrayList<String> transcriptions = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT content FROM notes ORDER BY id DESC", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    transcriptions.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transcriptions;
    }

    // -----------------------------------------------------------------
    // AUDIO RECORDINGS (File 1 + File 2 merge)
    // -----------------------------------------------------------------

    public void insertAudioFile(String fileName, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long currentTimeMillis = System.currentTimeMillis();
        cv.put("file_name", fileName);
        cv.put("file_path", filePath);
        cv.put("timestamp", currentTimeMillis);
        cv.put("transcription", (String) null);
        long rowId = db.insert("audio_files", null, cv);
        android.util.Log.d("DatabaseHelper", "✅ Audio file inserted: ID=" + rowId + ", fileName=" + fileName);
    }

    // File 1 special insert - NOW RETURNS RECORDING ID
    public long insertRecording(String fileName, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long currentTimeMillis = System.currentTimeMillis();
        cv.put("file_name", fileName);
        cv.put("file_path", filePath);
        cv.put("timestamp", currentTimeMillis);
        cv.put("transcription", (String) null);
        long recordingId = db.insert("audio_files", null, cv);
        android.util.Log.d("DatabaseHelper", "✅ Recording inserted with ID: " + recordingId + ", fileName: " + fileName);
        return recordingId;
    }

    // Overload: save recording WITH user email and source (recorded/uploaded)
    public long insertRecording(String fileName, String filePath, String userEmail, String source) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("file_name", fileName);
        cv.put("file_path", filePath);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("transcription", (String) null);
        if (userEmail != null) cv.put("user_email", userEmail);
        if (source != null) cv.put("source", source);
        long recordingId = db.insert("audio_files", null, cv);
        android.util.Log.d("DatabaseHelper", "✅ Recording inserted ID=" + recordingId + " user=" + userEmail + " source=" + source);
        return recordingId;
    }

    // Returns int[]{total, recorded, uploaded} for a user
    public int[] getAudioStatsForUser(String email) {
        int[] stats = new int[]{0, 0, 0};
        if (email == null) return stats;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(
                "SELECT COUNT(*), " +
                "SUM(CASE WHEN source='recorded' OR source IS NULL THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN source='uploaded' THEN 1 ELSE 0 END) " +
                "FROM audio_files WHERE user_email=?",
                new String[]{email});
            if (c.moveToFirst()) {
                stats[0] = c.getInt(0);
                stats[1] = c.getInt(1);
                stats[2] = c.getInt(2);
            }
            c.close();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "getAudioStatsForUser: " + e.getMessage());
        }
        return stats;
    }

    public Recording getLastRecording() {
        SQLiteDatabase db = null;
        Cursor c = null;
        Recording r = null;
        
        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM audio_files ORDER BY timestamp DESC LIMIT 1", null);

            if (c != null && c.moveToFirst()) {
                try {
                    String transcription = null;
                    try {
                        transcription = c.getString(c.getColumnIndexOrThrow("transcription"));
                    } catch (Exception e) {
                        android.util.Log.w("DatabaseHelper", "⚠️ Error getting transcription: " + e.getMessage());
                        transcription = null;
                    }
                    
                    long timestamp = 0;
                    try {
                        timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                    } catch (Exception e) {
                        android.util.Log.w("DatabaseHelper", "⚠️ Error getting timestamp: " + e.getMessage());
                        timestamp = 0;
                    }
                    
                    String dateFormatted = formatTimestamp(timestamp);
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String title = c.getString(c.getColumnIndexOrThrow("file_name"));
                    String filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
                    
                    r = new Recording(
                            id,
                            title != null ? title : "Untitled",
                            filePath != null ? filePath : "",
                            dateFormatted,
                            0,
                            transcription != null ? transcription : ""
                    );
                    android.util.Log.d("DatabaseHelper", "✅ Last recording fetched: ID=" + r.getId() + ", Title=" + r.getTitle() + ", HasTranscription=" + (transcription != null && !transcription.isEmpty()));
                } catch (Exception itemError) {
                    android.util.Log.e("DatabaseHelper", "❌ Error processing last recording: " + itemError.getMessage());
                    itemError.printStackTrace();
                }
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No recording found");
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Critical error in getLastRecording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    android.util.Log.e("DatabaseHelper", "❌ Error closing cursor: " + e.getMessage());
                }
            }
        }
        
        return r;
    }

    public Recording getRecordingById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM audio_files WHERE id=?", new String[]{String.valueOf(id)});

        Recording r = null;
        if (c.moveToFirst()) {
            String transcription = null;
            try {
                transcription = c.getString(c.getColumnIndexOrThrow("transcription"));
            } catch (Exception e) {
                android.util.Log.w("DatabaseHelper", "⚠️ Error getting transcription for ID " + id + ": " + e.getMessage());
                transcription = null;
            }
            
            String summary = null;
            try {
                summary = c.getString(c.getColumnIndexOrThrow("summary"));
            } catch (Exception e) {
                android.util.Log.w("DatabaseHelper", "⚠️ Error getting summary for ID " + id + ": " + e.getMessage());
                summary = null;
            }
            
            String quizJson = "[]";
            try {
                quizJson = c.getString(c.getColumnIndexOrThrow("quiz_json"));
            } catch (Exception e) {
                android.util.Log.w("DatabaseHelper", "⚠️ Error getting quiz_json for ID " + id + ": " + e.getMessage());
                quizJson = "[]";
            }
            
            long timestamp = 0;
            try {
                timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
            } catch (Exception e) {
                android.util.Log.w("DatabaseHelper", "⚠️ Error getting timestamp for ID " + id + ": " + e.getMessage());
            }
            String dateFormatted = formatTimestamp(timestamp);
            r = new Recording(
                    id,
                    c.getString(c.getColumnIndexOrThrow("file_name")),
                    c.getString(c.getColumnIndexOrThrow("file_path")),
                    dateFormatted,
                    0,
                    transcription != null ? transcription : ""
            );
            
            // Set summary and quiz_json on the Recording object
            r.setSummary(summary != null ? summary : "");
            r.setQuizJson(quizJson != null && !quizJson.isEmpty() ? quizJson : "[]");
            
            android.util.Log.d("DatabaseHelper", "✅ Recording fetched by ID " + id + ": Title=" + r.getTitle() + 
                    ", HasTranscription=" + (transcription != null && !transcription.isEmpty()) +
                    ", HasQuiz=" + (quizJson != null && !quizJson.equals("[]")));
        } else {
            android.util.Log.w("DatabaseHelper", "⚠️ No recording found with ID: " + id);
        }
        c.close();
        return r;
    }

    public ArrayList<Recording> getAllRecordings() {
        ArrayList<Recording> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = this.getReadableDatabase();

            android.util.Log.d("DatabaseHelper", "📊 Fetching all recordings from database...");
            c = db.rawQuery("SELECT * FROM audio_files ORDER BY timestamp DESC", null);
            
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        String transcription = null;
                        try {
                            transcription = c.getString(c.getColumnIndexOrThrow("transcription"));
                        } catch (Exception e) {
                            android.util.Log.w("DatabaseHelper", "⚠️ Error reading transcription: " + e.getMessage());
                            transcription = null;
                        }
                        
                        long timestamp = 0;
                        try {
                            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                        } catch (Exception e) {
                            android.util.Log.w("DatabaseHelper", "⚠️ Error reading timestamp: " + e.getMessage());
                            timestamp = 0;
                        }
                        
                        String dateFormatted = formatTimestamp(timestamp);
                        int recordingId = c.getInt(c.getColumnIndexOrThrow("id"));
                        String title = c.getString(c.getColumnIndexOrThrow("file_name"));
                        String filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
                        
                        Recording recording = new Recording(
                                recordingId,
                                title != null ? title : "Untitled",
                                filePath != null ? filePath : "",
                                dateFormatted,
                                0,
                                transcription != null ? transcription : ""
                        );
                        list.add(recording);
                        android.util.Log.d("DatabaseHelper", "  ✓ Recording ID=" + recordingId + ", Title=" + title + ", HasTranscription=" + (transcription != null && !transcription.isEmpty()));
                    } catch (Exception itemError) {
                        android.util.Log.e("DatabaseHelper", "❌ Error processing recording row: " + itemError.getMessage());
                        itemError.printStackTrace();
                        continue; // Skip this row and continue
                    }
                } while (c.moveToNext());
            }
            
            android.util.Log.d("DatabaseHelper", "📊 Total recordings fetched: " + list.size());
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Critical error in getAllRecordings: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    android.util.Log.e("DatabaseHelper", "❌ Error closing cursor: " + e.getMessage());
                }
            }
        }
        
        return list;
    }

    public boolean deleteRecording(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("audio_files", "id=?", new String[]{String.valueOf(id)});
        if (result > 0) {
            android.util.Log.d("DatabaseHelper", "✅ Recording deleted: ID " + id);
            return true;
        }
        return false;
    }

    // dummy recordings (File 1 only)
    public void insertDummyRecordings() {
        // ❌ DISABLED - No more dummy data
        // Dummy recordings were causing issues in the app
        // Use real recordings only!
        android.util.Log.d("DatabaseHelper", "⚠️ insertDummyRecordings() called but DISABLED");
    }

    // ✅ COMPREHENSIVE CLEANUP - Delete ALL dummy/test recordings
    public void cleanupAllDummyData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            
            // Delete by patterns
            int deleted1 = db.delete("audio_files", "file_name LIKE ?", new String[]{"Dummy%"});
            int deleted2 = db.delete("audio_files", "file_name LIKE ?", new String[]{"Test%"});
            int deleted3 = db.delete("audio_files", "file_name LIKE ?", new String[]{"Sample%"});
            int deleted4 = db.delete("audio_files", "file_name LIKE ?", new String[]{"%dummy%"});
            
            int totalDeleted = deleted1 + deleted2 + deleted3 + deleted4;
            
            android.util.Log.d("DatabaseHelper", "🗑️ CLEANUP: Deleted " + totalDeleted + " dummy/test entries");
            android.util.Log.d("DatabaseHelper", "   - Dummy*: " + deleted1);
            android.util.Log.d("DatabaseHelper", "   - Test*: " + deleted2);
            android.util.Log.d("DatabaseHelper", "   - Sample*: " + deleted3);
            android.util.Log.d("DatabaseHelper", "   - *dummy*: " + deleted4);
            
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error in cleanupAllDummyData: " + e.getMessage());
        }
    }
    
    // ✅ Original method - kept for backward compatibility
    public void deleteDummyRecordings() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int deleted = db.delete("audio_files", "file_name LIKE ?", new String[]{"DummyRecording_%"});
            android.util.Log.d("DatabaseHelper", "🗑️ Deleted dummy recordings: " + deleted + " rows");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error deleting dummy recordings: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // STUDY SESSIONS (Enhanced with audio processing fields)
    // -----------------------------------------------------------------

    /**
     * Insert a new Study Session with full audio processing results
     * Called after transcription + summary + quiz generation completes
     */
    public long insertStudySession(String title, String audioPath, String transcription,
                                   String summary, String quizJson, String createdDate,
                                   int duration, int wordCount, double quizScore, boolean pdfGenerated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("audio_path", audioPath);
        cv.put("transcription", transcription);
        cv.put("summary", summary);
        cv.put("quiz_json", quizJson != null ? quizJson : "[]");
        cv.put("created_date", createdDate);
        cv.put("duration", duration);
        cv.put("word_count", wordCount);
        cv.put("quiz_score", quizScore);
        cv.put("pdf_generated", pdfGenerated ? 1 : 0);
        
        long result = db.insert(TABLE_STUDY_SESSIONS, null, cv);
        if (result != -1) {
            android.util.Log.d("DatabaseHelper", "✅ Study Session inserted: " + title);
        } else {
            android.util.Log.e("DatabaseHelper", "❌ Failed to insert Study Session: " + title);
        }
        return result;
    }

    /**
     * Insert Study Session (Legacy method - backward compatible)
     */
    public void insertStudySession(String subject, int duration) {
        long timestamp = System.currentTimeMillis();
        String createdDate = formatTimestamp(timestamp);
        insertStudySession(subject, "", "", "", "[]", createdDate, duration, 0, 0.0, false);
    }

    /**
     * Get all Study Sessions, ordered by latest first
     */
    public ArrayList<StudySession> getAllStudySessions() {
        ArrayList<StudySession> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor c = db.query(
                    TABLE_STUDY_SESSIONS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "created_date DESC"
            );

            if (c.moveToFirst()) {
                do {
                    StudySession session = new StudySession(
                            c.getInt(c.getColumnIndexOrThrow("id")),
                            c.getString(c.getColumnIndexOrThrow("title")),
                            c.getString(c.getColumnIndexOrThrow("audio_path")),
                            c.getString(c.getColumnIndexOrThrow("transcription")),
                            c.getString(c.getColumnIndexOrThrow("summary")),
                            c.getString(c.getColumnIndexOrThrow("quiz_json")),
                            c.getString(c.getColumnIndexOrThrow("created_date")),
                            c.getInt(c.getColumnIndexOrThrow("duration")),
                            c.getInt(c.getColumnIndexOrThrow("word_count")),
                            c.getDouble(c.getColumnIndexOrThrow("quiz_score")),
                            c.getInt(c.getColumnIndexOrThrow("pdf_generated")) == 1
                    );
                    list.add(session);
                } while (c.moveToNext());
            }
            c.close();
            android.util.Log.d("DatabaseHelper", "✅ Loaded " + list.size() + " study sessions");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error loading study sessions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get a single Study Session by ID
     */
    public StudySession getStudySessionById(int sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Cursor c = db.query(
                    TABLE_STUDY_SESSIONS,
                    null,
                    "id = ?",
                    new String[]{String.valueOf(sessionId)},
                    null,
                    null,
                    null
            );

            if (c.moveToFirst()) {
                StudySession session = new StudySession(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("audio_path")),
                        c.getString(c.getColumnIndexOrThrow("transcription")),
                        c.getString(c.getColumnIndexOrThrow("summary")),
                        c.getString(c.getColumnIndexOrThrow("quiz_json")),
                        c.getString(c.getColumnIndexOrThrow("created_date")),
                        c.getInt(c.getColumnIndexOrThrow("duration")),
                        c.getInt(c.getColumnIndexOrThrow("word_count")),
                        c.getDouble(c.getColumnIndexOrThrow("quiz_score")),
                        c.getInt(c.getColumnIndexOrThrow("pdf_generated")) == 1
                );
                c.close();
                return session;
            }
            c.close();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error loading study session: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update Study Session (e.g., update quiz score after completion)
     */
    public boolean updateStudySession(int sessionId, double newQuizScore, boolean pdfGenerated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("quiz_score", newQuizScore);
        cv.put("pdf_generated", pdfGenerated ? 1 : 0);
        
        int result = db.update(TABLE_STUDY_SESSIONS, cv, "id = ?", new String[]{String.valueOf(sessionId)});
        if (result > 0) {
            android.util.Log.d("DatabaseHelper", "✅ Study Session updated: ID " + sessionId);
            return true;
        }
        return false;
    }

    /**
     * Delete a Study Session by ID
     */
    public boolean deleteStudySession(int sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_STUDY_SESSIONS, "id = ?", new String[]{String.valueOf(sessionId)});
        if (result > 0) {
            android.util.Log.d("DatabaseHelper", "✅ Study Session deleted: ID " + sessionId);
            return true;
        }
        return false;
    }
    public Cursor getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM profile LIMIT 1", null);
    }
    public Cursor getProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 🔴 GUARANTEE profile table exists (FINAL FIX)
        db.execSQL(CREATE_PROFILE_TABLE);

        return db.rawQuery(
                "SELECT * FROM profile WHERE email = ?",
                new String[]{email}
        );
    }
    // TOTAL USERS
    public int getTotalUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Users", null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    // TOTAL AUDIOS
    public int getTotalAudios() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM audio_files", null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    // TOTAL NOTES / TRANSCRIPTIONS
    public int getTotalNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM notes", null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }

    // BLOCKED USERS
    public int getBlockedUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Users WHERE isBlocked = 1", null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;
    }
    // READ
    public String getUsernameByEmail(String email) {
        String username = "";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT username FROM users WHERE email = ?",
                new String[]{email}
        );

        if (c != null && c.moveToFirst()) {
            username = c.getString(0);
            c.close();
        }
        return username;
    }

    // UPDATE
    public boolean updateUsername(String email, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", newUsername);

        int rows = db.update(
                "users",
                values,
                "email = ?",
                new String[]{email}
        );
        return rows > 0;
    }
    public String getPasswordByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT password FROM users WHERE email = ?",
                new String[]{email}
        );

        if (c != null && c.moveToFirst()) {
            String pass = c.getString(0);
            c.close();
            return pass;
        }

        if (c != null) c.close();
        return null;
    }

    // ====================================================================
    // QUIZ QUESTIONS MANAGEMENT
    // ====================================================================

    public boolean insertQuizQuestion(String question, String optionA, String optionB,
                                      String optionC, String optionD, String answer,
                                      String category, String difficulty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", question);
        cv.put("optionA", optionA);
        cv.put("optionB", optionB);
        cv.put("optionC", optionC);
        cv.put("optionD", optionD);
        cv.put("answer", answer);
        cv.put("category", category);
        cv.put("difficulty", difficulty);
        cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        return db.insert(TABLE_QUIZ_QUESTIONS, null, cv) != -1;
    }

    public ArrayList<QuizQuestion> getAllQuizQuestions() {
        ArrayList<QuizQuestion> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_QUIZ_QUESTIONS + " ORDER BY RANDOM() LIMIT 10", null);

        if (c.moveToFirst()) {
            do {
                list.add(new QuizQuestion(
                        c.getString(c.getColumnIndexOrThrow("question")),
                        c.getString(c.getColumnIndexOrThrow("optionA")),
                        c.getString(c.getColumnIndexOrThrow("optionB")),
                        c.getString(c.getColumnIndexOrThrow("optionC")),
                        c.getString(c.getColumnIndexOrThrow("optionD")),
                        c.getString(c.getColumnIndexOrThrow("answer"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public ArrayList<QuizQuestion> getQuizQuestionsByCategory(String category) {
        ArrayList<QuizQuestion> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE category=? ORDER BY RANDOM()",
                              new String[]{category});

        if (c.moveToFirst()) {
            do {
                list.add(new QuizQuestion(
                        c.getString(c.getColumnIndexOrThrow("question")),
                        c.getString(c.getColumnIndexOrThrow("optionA")),
                        c.getString(c.getColumnIndexOrThrow("optionB")),
                        c.getString(c.getColumnIndexOrThrow("optionC")),
                        c.getString(c.getColumnIndexOrThrow("optionD")),
                        c.getString(c.getColumnIndexOrThrow("answer"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // ====================================================================
    // ✅ QUIZ RESULTS MANAGEMENT (Performance Tracking)
    // ====================================================================

    /**
     * Save quiz result after quiz completion
     */
    public boolean insertQuizResult(String category, int correctCount, int wrongCount, int durationSeconds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        
        int totalQuestions = correctCount + wrongCount;
        double scorePercentage = totalQuestions > 0 ? (correctCount * 100.0) / totalQuestions : 0;
        
        cv.put("category", category != null ? category : "General");
        cv.put("correct_count", correctCount);
        cv.put("wrong_count", wrongCount);
        cv.put("total_questions", totalQuestions);
        cv.put("score_percentage", scorePercentage);
        cv.put("duration_seconds", durationSeconds);
        cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        boolean inserted = db.insert(TABLE_QUIZ_RESULTS, null, cv) != -1;
        if (inserted) {
            android.util.Log.d("DatabaseHelper", "✅ Quiz result saved: " + category + " - Score: " + String.format("%.1f", scorePercentage) + "%");
        } else {
            android.util.Log.e("DatabaseHelper", "❌ Failed to save quiz result");
        }
        return inserted;
    }

    /**
     * Get all quiz results sorted by date (newest first)
     */
    public ArrayList<QuizResult> getAllQuizResults() {
        ArrayList<QuizResult> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Ensure table exists first
            ensureQuizTablesExist();
            
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_QUIZ_RESULTS + " ORDER BY created_date DESC", null);

            if (c.moveToFirst()) {
                do {
                    QuizResult result = new QuizResult(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("category")),
                        c.getInt(c.getColumnIndexOrThrow("correct_count")),
                        c.getInt(c.getColumnIndexOrThrow("wrong_count")),
                        c.getInt(c.getColumnIndexOrThrow("total_questions")),
                        c.getDouble(c.getColumnIndexOrThrow("score_percentage")),
                        c.getInt(c.getColumnIndexOrThrow("duration_seconds")),
                        c.getString(c.getColumnIndexOrThrow("created_date"))
                    );
                    list.add(result);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error in getAllQuizResults: " + e.getMessage());
            // Return empty list instead of crashing
        }
        return list;
    }

    /**
     * Get average quiz score (overall performance)
     */
    public double getAverageQuizScore() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Ensure table exists first
            ensureQuizTablesExist();
            
            Cursor c = db.rawQuery("SELECT AVG(score_percentage) as avg_score FROM " + TABLE_QUIZ_RESULTS, null);
            double avgScore = 0;
            if (c.moveToFirst()) {
                avgScore = c.getDouble(c.getColumnIndexOrThrow("avg_score"));
            }
            c.close();
            return avgScore;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error in getAverageQuizScore: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Ensure quiz-related tables exist
     */
    private void ensureQuizTablesExist() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Check if table exists by trying to query it
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_QUIZ_RESULTS + "'", null);
            boolean tableExists = cursor.getCount() > 0;
            cursor.close();
            
            // If table doesn't exist, create it
            if (!tableExists) {
                android.util.Log.d("DatabaseHelper", "📍 Creating missing quiz_results table...");
                db.execSQL(CREATE_QUIZ_RESULTS_TABLE);
                android.util.Log.d("DatabaseHelper", "✅ quiz_results table created successfully");
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error ensuring quiz tables exist: " + e.getMessage());
        }
    }

    /**
     * Get quiz results by category
     */
    public ArrayList<QuizResult> getQuizResultsByCategory(String category) {
        ArrayList<QuizResult> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Ensure table exists first
            ensureQuizTablesExist();
            
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_QUIZ_RESULTS + " WHERE category=? ORDER BY created_date DESC",
                                  new String[]{category});

            if (c.moveToFirst()) {
                do {
                    QuizResult result = new QuizResult(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("category")),
                        c.getInt(c.getColumnIndexOrThrow("correct_count")),
                        c.getInt(c.getColumnIndexOrThrow("wrong_count")),
                        c.getInt(c.getColumnIndexOrThrow("total_questions")),
                        c.getDouble(c.getColumnIndexOrThrow("score_percentage")),
                        c.getInt(c.getColumnIndexOrThrow("duration_seconds")),
                        c.getString(c.getColumnIndexOrThrow("created_date"))
                    );
                    list.add(result);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error in getQuizResultsByCategory: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get quiz results count
     */
    public int getQuizResultsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Ensure table exists first
            ensureQuizTablesExist();
            
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_QUIZ_RESULTS, null);
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
            return count;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error in getQuizResultsCount: " + e.getMessage());
            return 0;
        }
    }

    // ====================================================================
    // NOTIFICATIONS MANAGEMENT
    // ====================================================================

    public boolean insertNotification(String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Ensure table exists (safety for existing installs)
        try { db.execSQL(CREATE_NOTIFICATIONS_TABLE); } catch (Exception ignored) {}

        // ✅ Prevent duplicate: skip if same type inserted within last 10 seconds
        try {
            String tenSecondsAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() - 10000));
            Cursor check = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS + " WHERE type=? AND timestamp>?",
                new String[]{type, tenSecondsAgo});
            if (check.moveToFirst() && check.getInt(0) > 0) {
                check.close();
                android.util.Log.d("DatabaseHelper", "⏭️ Duplicate notification skipped: " + type);
                return false;
            }
            check.close();
        } catch (Exception ignored) {}

        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("message", message);
        cv.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        cv.put("type", type);
        cv.put("is_read", 0);
        return db.insert(TABLE_NOTIFICATIONS, null, cv) != -1;
    }

    public ArrayList<String> getAllNotifications() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        // Ensure table exists (safety for existing installs)
        try { db.execSQL(CREATE_NOTIFICATIONS_TABLE); } catch (Exception ignored) {}
        Cursor c = db.rawQuery("SELECT title, message FROM " + TABLE_NOTIFICATIONS + " ORDER BY id DESC LIMIT 20", null);

        if (c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String message = c.getString(c.getColumnIndexOrThrow("message"));
                list.add(title + "\n" + message);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean markNotificationAsRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        return db.update(TABLE_NOTIFICATIONS, cv, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public void clearAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Ensure table exists (safety for existing installs)
        try { db.execSQL(CREATE_NOTIFICATIONS_TABLE); } catch (Exception ignored) {}
        db.delete(TABLE_NOTIFICATIONS, null, null);
    }

    // ====================================================================
    // HELP CONTENT MANAGEMENT
    // ====================================================================

    public String getHelpContent(String category) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(CREATE_HELP_TABLE);
            Cursor c = db.rawQuery("SELECT content FROM " + TABLE_HELP_CONTENT + " WHERE category=? LIMIT 1",
                                  new String[]{category});
            String content = "";
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("content");
                if (idx >= 0) content = c.getString(idx);
            }
            c.close();
            return content;
        } catch (Exception e) {
            return "";
        }
    }

    public String getHelpSupportEmail() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT support_email FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);
            String email = "support@smartstudybuddy.com";
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("support_email");
                if (idx >= 0) email = c.getString(idx);
            }
            c.close();
            return email;
        } catch (Exception e) {
            return "support@smartstudybuddy.com";
        }
    }

    public String getHelpSupportPhone() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT support_phone FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);
            String phone = "+1 234 567 890";
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("support_phone");
                if (idx >= 0) phone = c.getString(idx);
            }
            c.close();
            return phone;
        } catch (Exception e) {
            return "+1 234 567 890";
        }
    }

    // ====================================================================
    // ABOUT CONTENT MANAGEMENT
    // ====================================================================

    public String getAboutAppDescription() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT description FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);
            String desc = "Smart Study Buddy helps you record audio, generate transcriptions, save notes, and stay organized.";
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("description");
                if (idx >= 0) desc = c.getString(idx);
            }
            c.close();
            return desc;
        } catch (Exception e) {
            return "Smart Study Buddy helps you record audio, generate transcriptions, save notes, and stay organized.";
        }
    }

    public String getAboutAppVersion() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT app_version FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);
            String version = "1.0.0";
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex("app_version");
                if (idx >= 0) version = c.getString(idx);
            }
            c.close();
            return version;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    public boolean updateAboutContent(String appVersion, String description, String email, String phone) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Ensure table exists before updating
            db.execSQL(CREATE_ABOUT_TABLE);
            // Delete old row and insert fresh (upsert)
            db.delete(TABLE_ABOUT_CONTENT, null, null);
            ContentValues cv = new ContentValues();
            cv.put("app_name", "Smart Study Buddy");
            cv.put("app_version", appVersion);
            cv.put("description", description);
            cv.put("support_email", email);
            cv.put("support_phone", phone);
            cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            long result = db.insert(TABLE_ABOUT_CONTENT, null, cv);
            android.util.Log.d("DatabaseHelper", "updateAboutContent insert result: " + result);
            return result != -1;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "updateAboutContent error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateHelpContent(String category, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        return db.update(TABLE_HELP_CONTENT, cv, "category=?", new String[]{category}) > 0;
    }

    // ====================================================================
    // DEFAULT DATA INITIALIZATION
    // ====================================================================

    private void insertDefaultHelpContent(SQLiteDatabase db) {
        try {
            ContentValues cv = new ContentValues();

            // General help
            cv.put("title", "Getting Started");
            cv.put("content", "Welcome to Smart Study Buddy! Start by recording lectures or uploading audio files to generate transcriptions.");
            cv.put("category", "getting_started");
            cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_HELP_CONTENT, null, cv);

            // FAQ
            cv = new ContentValues();
            cv.put("title", "How do I record audio?");
            cv.put("content", "Navigate to the Record Mic section, tap the record button, and speak clearly. Your audio will be saved automatically.");
            cv.put("category", "faq_recording");
            cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_HELP_CONTENT, null, cv);
            
            android.util.Log.d("DatabaseHelper", "✅ Default help content inserted");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error inserting default help content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDefaultAboutContent(SQLiteDatabase db) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("app_name", "Smart Study Buddy");
            cv.put("app_version", "1.0.0");
            cv.put("description", "Smart Study Buddy helps you record audio, generate transcription, save notes, view analytics, and stay organized with your studies. Designed with simplicity and power — all in one app!");
            cv.put("support_email", "support@smartstudybuddy.com");
            cv.put("support_phone", "+1 234 567 890");
            cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_ABOUT_CONTENT, null, cv);
            
            android.util.Log.d("DatabaseHelper", "✅ Default about content inserted");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error inserting default about content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDefaultQuizQuestions(SQLiteDatabase db) {
        try {
            ContentValues cv = new ContentValues();

            // Question 1
            cv.put("question", "What is Artificial Intelligence?");
            cv.put("optionA", "Artificial Input");
            cv.put("optionB", "Automatic Intelligence");
            cv.put("optionC", "Artificial Intelligence");
            cv.put("optionD", "Auto Internet");
            cv.put("answer", "Artificial Intelligence");
            cv.put("category", "Technology");
            cv.put("difficulty", "Easy");
            cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_QUIZ_QUESTIONS, null, cv);

            // Question 2
            cv = new ContentValues();
            cv.put("question", "Which one is a programming language?");
            cv.put("optionA", "HTML");
            cv.put("optionB", "CSS");
            cv.put("optionC", "Java");
            cv.put("optionD", "Photoshop");
            cv.put("answer", "Java");
            cv.put("category", "Programming");
            cv.put("difficulty", "Easy");
            cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            db.insert(TABLE_QUIZ_QUESTIONS, null, cv);
            
            android.util.Log.d("DatabaseHelper", "✅ Default quiz questions inserted");
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "⚠️ Error inserting default quiz questions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete a recording by ID - also deletes associated file and transcription
     */
    public boolean deleteRecordingById(int recordingId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            android.util.Log.d("DatabaseHelper", "🗑️ Deleting recording with ID: " + recordingId);

            // First, get the file path so we can delete the actual file
            Cursor cursor = db.rawQuery("SELECT file_path FROM audio_files WHERE id=?", new String[]{String.valueOf(recordingId)});
            String filePath = null;
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
                android.util.Log.d("DatabaseHelper", "   File path: " + filePath);
            }
            cursor.close();

            // Delete from database
            int deletedRows = db.delete("audio_files", "id=?", new String[]{String.valueOf(recordingId)});

            if (deletedRows > 0) {
                android.util.Log.d("DatabaseHelper", "✅ Recording deleted from database: " + recordingId);

                // Try to delete the actual audio file if it exists
                if (filePath != null && !filePath.isEmpty()) {
                    try {
                        java.io.File audioFile = new java.io.File(filePath);
                        if (audioFile.exists() && audioFile.delete()) {
                            android.util.Log.d("DatabaseHelper", "✅ Audio file deleted: " + filePath);
                        } else if (audioFile.exists()) {
                            android.util.Log.w("DatabaseHelper", "⚠️ Could not delete audio file: " + filePath);
                        } else {
                            android.util.Log.w("DatabaseHelper", "⚠️ Audio file does not exist: " + filePath);
                        }
                    } catch (Exception e) {
                        android.util.Log.w("DatabaseHelper", "⚠️ Exception deleting audio file: " + e.getMessage());
                    }
                }

                return true;
            } else {
                android.util.Log.w("DatabaseHelper", "⚠️ No recording found with ID: " + recordingId);
                return false;
            }

        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "❌ Error deleting recording: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


}