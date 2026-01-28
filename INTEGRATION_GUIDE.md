# Dynamic Data Implementation - Integration Guide

## Quick Start Guide

This guide helps you understand and use the new dynamic data system in Smart Study Buddy.

---

## What Changed?

### Before
```java
// Old way - hardcoded data
ArrayList<String> notificationList = new ArrayList<>();
notificationList.add("📘 New note saved");
notificationList.add("⏰ Reminder: Study at 7 PM");
```

### After
```java
// New way - from database
DatabaseHelper dbHelper = new DatabaseHelper(this);
ArrayList<String> notificationList = dbHelper.getAllNotifications();
```

---

## Core Components

### 1. DatabaseHelper.java
**Location:** `app/src/main/java/com/example/smartstudybuddy2/DatabaseHelper.java`

**Purpose:** Handles all database operations

**Key Methods:**
- `getAllQuizQuestions()` - Get all quiz questions
- `getAllNotifications()` - Get all notifications
- `getAllNotes()` - Get all notes for search
- `getTotalUsers()` - Get total user count
- `getTotalAudios()` - Get total recordings
- `getTotalNotes()` - Get total notes/transcriptions

---

### 2. Admin Management Activities

#### QuizManagementActivity
**Purpose:** Add new quiz questions
**How to Use:**
1. Launch from admin panel
2. Enter question and 4 options
3. Select correct answer
4. Choose category and difficulty
5. Click "Add Question"

#### ContentManagementActivity
**Purpose:** Update app content (About, Help, Support info)
**How to Use:**
1. Launch from admin panel
2. Edit app version, description, email, phone
3. Click "Save Content"

---

## Using Dynamic Data in Activities

### Example 1: Load Quiz Questions
```java
DatabaseHelper dbHelper = new DatabaseHelper(this);
ArrayList<QuizQuestion> questions = dbHelper.getAllQuizQuestions();

if (questions.isEmpty()) {
    // Fallback to dummy questions
    loadDummyQuestions();
} else {
    showQuestion();
}
```

### Example 2: Display Analytics
```java
DatabaseHelper dbHelper = new DatabaseHelper(this);

int totalUsers = dbHelper.getTotalUsers();
int activeUsers = dbHelper.getTotalUsers() - dbHelper.getBlockedUsersCount();
int totalNotes = dbHelper.getTotalNotes();

tvTotalUsers.setText(String.valueOf(totalUsers));
tvActiveUsers.setText(String.valueOf(activeUsers));
tvTotalNotes.setText(String.valueOf(totalNotes));
```

### Example 3: Search Notes
```java
DatabaseHelper dbHelper = new DatabaseHelper(this);
Cursor cursor = dbHelper.getAllNotes();

ArrayList<String> notesList = new ArrayList<>();
if (cursor.moveToFirst()) {
    do {
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        notesList.add(title);
    } while (cursor.moveToNext());
}

// Use notesList for SearchView filtering
adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notesList);
searchView.setAdapter(adapter);
```

---

## Database Schema Reference

### quiz_questions
```
Columns: id, question, optionA, optionB, optionC, optionD, answer, category, difficulty, created_date
Purpose: Store quiz questions
Example: INSERT INTO quiz_questions (question, optionA, optionB, optionC, optionD, answer, category, difficulty)
         VALUES ('What is Java?', 'Programming Language', 'Coffee', 'Island', 'Person', 'Programming Language', 'Programming', 'Easy');
```

### notifications
```
Columns: id, title, message, timestamp, type, is_read
Purpose: Store app notifications
Example: INSERT INTO notifications (title, message, timestamp, type, is_read)
         VALUES ('Quiz Completed', 'You completed AI Quiz with 8/10', '2026-01-28 10:30:00', 'achievement', 0);
```

### help_content
```
Columns: id, title, content, category, last_updated
Purpose: Store help articles and FAQs
Example: INSERT INTO help_content (title, content, category, last_updated)
         VALUES ('Recording Audio', 'To record audio...', 'getting_started', '2026-01-28');
```

### about_content
```
Columns: id, app_name, app_version, description, support_email, support_phone, last_updated
Purpose: Store app metadata and contact info
Example: UPDATE about_content SET app_version = '1.1.0', support_email = 'support@newdomain.com';
```

---

## Common Operations

### Add a Quiz Question Programmatically
```java
dbHelper.insertQuizQuestion(
    "What is Android?",
    "Mobile OS",
    "Programming Language",
    "Web Browser",
    "Social Media",
    "Mobile OS",
    "Technology",
    "Easy"
);
```

### Add a Notification
```java
dbHelper.insertNotification(
    "Quiz Completed",
    "You scored 8/10 on AI Quiz",
    "achievement"
);
```

### Update App Content
```java
dbHelper.updateAboutContent(
    "1.1.0",
    "Updated description...",
    "newemail@example.com",
    "+1-555-123-4567"
);
```

### Clear All Notifications
```java
dbHelper.clearAllNotifications();
```

---

## Default Data

When the app first runs, the following data is automatically created:

### Default Quiz Questions
1. **"What is Artificial Intelligence?"**
   - Options: Artificial Input, Automatic Intelligence, Artificial Intelligence, Auto Internet
   - Answer: Artificial Intelligence
   - Category: Technology
   - Difficulty: Easy

2. **"Which one is a programming language?"**
   - Options: HTML, CSS, Java, Photoshop
   - Answer: Java
   - Category: Programming
   - Difficulty: Easy

### Default Help Content
- Getting Started guide
- Recording FAQ

### Default About Content
- App Name: Smart Study Buddy
- Version: 1.0.0
- Description: Comprehensive feature description
- Support Email: support@smartstudybuddy.com
- Support Phone: +1 234 567 890

---

## Troubleshooting

### Issue: "No quiz questions available"
**Solution:**
- Check if quiz_questions table exists: `db.execSQL(CREATE_QUIZ_TABLE);`
- Verify questions were inserted correctly
- Add questions using QuizManagementActivity

### Issue: "Search returns no results"
**Solution:**
- Ensure notes exist in database first
- Check that notes table is being populated correctly
- Add test notes through NotesActivity

### Issue: "Analytics showing 0 data"
**Solution:**
- Create test recordings and study sessions
- Verify data is in audio_files and study_sessions tables
- Check DatabaseHelper methods return correct counts

### Issue: "About/Help content not updating"
**Solution:**
- Verify about_content and help_content tables exist
- Ensure updateAboutContent() was called successfully
- Check database file permissions

---

## Performance Optimization Tips

### 1. Lazy Load Data
```java
// Don't load all at once for large datasets
// Load on demand instead
private void loadQuizQuestionsOnDemand() {
    // Load 10 questions at a time
    new Thread(() -> {
        ArrayList<QuizQuestion> batch = dbHelper.getAllQuizQuestions();
        // Process batch on main thread
    }).start();
}
```

### 2. Use Cursors for Large Datasets
```java
// Instead of loading entire ArrayList
Cursor cursor = dbHelper.getAllNotes();
while (cursor.moveToNext()) {
    // Process one at a time
}
cursor.close();
```

### 3. Cache Frequently Accessed Data
```java
// Cache app version (doesn't change often)
private String cachedAppVersion;

private String getAppVersion() {
    if (cachedAppVersion == null) {
        cachedAppVersion = dbHelper.getAboutAppVersion();
    }
    return cachedAppVersion;
}
```

---

## Adding New Dynamic Content Types

To add a new dynamic content type (e.g., Study Tips):

### Step 1: Create Database Table
```java
// In DatabaseHelper.java
public static final String TABLE_STUDY_TIPS = "study_tips";
public static final String CREATE_STUDY_TIPS_TABLE =
    "CREATE TABLE IF NOT EXISTS " + TABLE_STUDY_TIPS + "(" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "title TEXT," +
        "tip TEXT," +
        "category TEXT," +
        "created_date TEXT)";
```

### Step 2: Add CRUD Methods
```java
public boolean insertStudyTip(String title, String tip, String category) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put("title", title);
    cv.put("tip", tip);
    cv.put("category", category);
    cv.put("created_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    return db.insert(TABLE_STUDY_TIPS, null, cv) != -1;
}

public ArrayList<String> getAllStudyTips() {
    ArrayList<String> tips = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor c = db.rawQuery("SELECT title, tip FROM " + TABLE_STUDY_TIPS, null);
    if (c.moveToFirst()) {
        do {
            tips.add(c.getString(0) + ": " + c.getString(1));
        } while (c.moveToNext());
    }
    c.close();
    return tips;
}
```

### Step 3: Create Management Activity
```java
public class StudyTipManagementActivity extends AppCompatActivity {
    // Similar to QuizManagementActivity
}
```

### Step 4: Use in Activities
```java
ArrayList<String> tips = dbHelper.getAllStudyTips();
// Display tips in UI
```

---

## File References

| File | Purpose | Status |
|------|---------|--------|
| DatabaseHelper.java | Core database operations | ✅ Ready |
| QuizActivity.java | Load quiz questions | ✅ Ready |
| HistoryActivity.java | Load history data | ✅ Ready |
| NotificationActivity.java | Display notifications | ✅ Ready |
| SearchActivity.java | Search notes | ✅ Ready |
| AboutActivity.java | Display app info | ✅ Ready |
| HelpActivity.java | Show help content | ✅ Ready |
| AnalyticsActivity.java | Real analytics | ✅ Ready |
| QuizManagementActivity.java | Admin quiz editor | ✅ Ready |
| ContentManagementActivity.java | Admin content editor | ✅ Ready |

---

## Next Steps

1. **Test the app** - Verify all features work with real database
2. **Add initial content** - Use admin activities to populate quiz questions
3. **Monitor performance** - Check for any lag with large datasets
4. **Plan Phase 2** - AI integration, bulk import, etc.
5. **Document custom changes** - If you modify queries

---

**Last Updated:** January 28, 2026
**Version:** 1.0
**Status:** Complete and Ready for Use
