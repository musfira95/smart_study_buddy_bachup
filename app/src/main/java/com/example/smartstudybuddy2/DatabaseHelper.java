package com.example.smartstudybuddy2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SmartStudyBuddy.db";
    private static final int DB_VERSION = 15; // final merged version

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

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

        // AUDIO FILES (combined version – File1 + File2)
        db.execSQL("CREATE TABLE IF NOT EXISTS audio_files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "file_name TEXT, " +
                "file_path TEXT, " +
                "date TEXT)");

        // STUDY SESSION TABLE (only File 1 had it → added)
        db.execSQL("CREATE TABLE IF NOT EXISTS study_sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "subject TEXT, " +
                "duration INTEGER, " +
                "date TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS study_sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "subject TEXT, " +
                "duration INTEGER, " +
                "date TEXT)");

// ✅ ADD THIS LINE (approx line 65–66)
        db.execSQL(CREATE_PROFILE_TABLE);
        db.execSQL(CREATE_FLASHCARD_TABLE);
        db.execSQL(CREATE_SCHEDULE_TABLE);
        db.execSQL(CREATE_QUIZ_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        db.execSQL(CREATE_HELP_TABLE);
        db.execSQL(CREATE_ABOUT_TABLE);

        // Insert default help and about content
        insertDefaultHelpContent(db);
        insertDefaultAboutContent(db);
        insertDefaultQuizQuestions(db);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS audio_files");
        db.execSQL("DROP TABLE IF EXISTS study_sessions");
        db.execSQL("DROP TABLE IF EXISTS profile");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HELP_CONTENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ABOUT_CONTENT);
        onCreate(db);
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

    // SCHEDULES / TIMETABLE
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String CREATE_SCHEDULE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SCHEDULES + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "description TEXT," +
                    "time TEXT," +
                    "date TEXT)";

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
    // AUDIO RECORDINGS (File 1 + File 2 merge)
    // -----------------------------------------------------------------

    public void insertAudioFile(String fileName, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("file_name", fileName);
        cv.put("file_path", filePath);
        cv.put("date", new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));
        db.insert("audio_files", null, cv);
    }

    // File 1 special insert
    public void insertRecording(String fileName, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("file_name", fileName);
        cv.put("file_path", filePath);
        cv.put("date", System.currentTimeMillis());
        db.insert("audio_files", null, cv);
    }

    public Recording getLastRecording() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM audio_files ORDER BY id DESC LIMIT 1", null);

        Recording r = null;
        if (c.moveToFirst()) {
            r = new Recording(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("file_name")),
                    c.getString(c.getColumnIndexOrThrow("file_path")),
                    c.getString(c.getColumnIndexOrThrow("date")),
                    0
            );
        }
        c.close();
        return r;
    }

    public Recording getRecordingById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM audio_files WHERE id=?", new String[]{String.valueOf(id)});

        Recording r = null;
        if (c.moveToFirst()) {
            r = new Recording(
                    id,
                    c.getString(c.getColumnIndexOrThrow("file_name")),
                    c.getString(c.getColumnIndexOrThrow("file_path")),
                    c.getString(c.getColumnIndexOrThrow("date")),
                    0
            );
        }
        c.close();
        return r;
    }

    public ArrayList<Recording> getAllRecordings() {
        ArrayList<Recording> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM audio_files ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                list.add(new Recording(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("file_name")),
                        c.getString(c.getColumnIndexOrThrow("file_path")),
                        c.getString(c.getColumnIndexOrThrow("date")),
                        0
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void deleteRecording(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("audio_files", "id=?", new String[]{String.valueOf(id)});
    }

    // dummy recordings (File 1 only)
    public void insertDummyRecordings() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 1; i <= 3; i++) {
            cv.put("file_name", "DummyRecording_" + i + ".mp3");
            cv.put("file_path", "/dummy/path/recording_" + i + ".mp3");
            cv.put("date", "06 Dec 2025");
            db.insert("audio_files", null, cv);
        }
    }

    // -----------------------------------------------------------------
    // STUDY SESSIONS (File 1 only)
    // -----------------------------------------------------------------

    public void insertStudySession(String subject, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subject", subject);
        cv.put("duration", duration);
        cv.put("date", System.currentTimeMillis());
        db.insert("study_sessions", null, cv);
    }

    public ArrayList<StudySession> getAllStudySessions() {
        ArrayList<StudySession> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM study_sessions ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                list.add(new StudySession(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("subject")),
                        String.valueOf(c.getInt(c.getColumnIndexOrThrow("duration"))),
                        c.getString(c.getColumnIndexOrThrow("date"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
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
    // NOTIFICATIONS MANAGEMENT
    // ====================================================================

    public boolean insertNotification(String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT title, message FROM " + TABLE_NOTIFICATIONS + " ORDER BY id DESC LIMIT 20", null);

        if (c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String message = c.getString(c.getColumnIndexOrThrow("message"));
                list.add(title + ": " + message);
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
        db.delete(TABLE_NOTIFICATIONS, null, null);
    }

    // ====================================================================
    // HELP CONTENT MANAGEMENT
    // ====================================================================

    public String getHelpContent(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT content FROM " + TABLE_HELP_CONTENT + " WHERE category=? LIMIT 1",
                              new String[]{category});

        String content = "";
        if (c.moveToFirst()) {
            content = c.getString(c.getColumnIndexOrThrow("content"));
        }
        c.close();
        return content;
    }

    public String getHelpSupportEmail() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT support_email FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);

        String email = "support@smartstudybuddy.com";
        if (c.moveToFirst()) {
            email = c.getString(c.getColumnIndexOrThrow("support_email"));
        }
        c.close();
        return email;
    }

    public String getHelpSupportPhone() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT support_phone FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);

        String phone = "+1 234 567 890";
        if (c.moveToFirst()) {
            phone = c.getString(c.getColumnIndexOrThrow("support_phone"));
        }
        c.close();
        return phone;
    }

    // ====================================================================
    // ABOUT CONTENT MANAGEMENT
    // ====================================================================

    public String getAboutAppDescription() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT description FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);

        String desc = "Smart Study Buddy helps you record audio, generate transcriptions, save notes, and stay organized.";
        if (c.moveToFirst()) {
            desc = c.getString(c.getColumnIndexOrThrow("description"));
        }
        c.close();
        return desc;
    }

    public String getAboutAppVersion() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT app_version FROM " + TABLE_ABOUT_CONTENT + " LIMIT 1", null);

        String version = "1.0.0";
        if (c.moveToFirst()) {
            version = c.getString(c.getColumnIndexOrThrow("app_version"));
        }
        c.close();
        return version;
    }

    // ====================================================================
    // DEFAULT DATA INITIALIZATION
    // ====================================================================

    private void insertDefaultHelpContent(SQLiteDatabase db) {
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
    }

    private void insertDefaultAboutContent(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("app_name", "Smart Study Buddy");
        cv.put("app_version", "1.0.0");
        cv.put("description", "Smart Study Buddy helps you record audio, generate transcription, save notes, view analytics, and stay organized with your studies. Designed with simplicity and power — all in one app!");
        cv.put("support_email", "support@smartstudybuddy.com");
        cv.put("support_phone", "+1 234 567 890");
        cv.put("last_updated", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_ABOUT_CONTENT, null, cv);
    }

    private void insertDefaultQuizQuestions(SQLiteDatabase db) {
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
    }




}