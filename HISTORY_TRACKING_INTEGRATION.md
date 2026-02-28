# History Tracking System - Integration Guide

## Overview
The History Tracking feature automatically saves completed study sessions to the SQLite database. Each session captures:
- Transcription text
- Summary
- Generated quiz questions
- Performance metrics (duration, word count, quiz score)

## Database Integration

### StudySession Model
Located in: `StudySession.java`

**Required Fields:**
- `id` - Auto-incremented primary key
- `title` - Audio file name
- `audioPath` - Path to audio file
- `transcription` - Full transcription text
- `summary` - Generated summary
- `quizJson` - Quiz questions in JSON format
- `createdDate` - Timestamp (formatted string)
- `duration` - Recording duration in seconds
- `wordCount` - Number of words in transcription
- `quizScore` - Quiz performance score (0-100)
- `pdfGenerated` - Whether PDF report was created

### Database Methods

All CRUD operations are in `DatabaseHelper.java`:

#### Insert Study Session (Complete Data)
```java
long sessionId = dbHelper.insertStudySession(
    title,           // String: "Audio_20250107_120000"
    audioPath,       // String: "/path/to/audio.mp3"
    transcription,   // String: "Full transcribed text..."
    summary,         // String: "Generated summary..."
    quizJson,        // String: "[{\"question\":\"...\",\"answer\":\"...\"}]"
    createdDate,     // String: "07 Jan 2025 12:30:45"
    duration,        // int: 300 (seconds)
    wordCount,       // int: 2500
    quizScore,       // double: 0.0 (initially)
    pdfGenerated     // boolean: false (initially)
);
```

#### Get All Sessions
```java
ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();
// Returns: List ordered by created_date DESC (newest first)
```

#### Get Single Session
```java
StudySession session = dbHelper.getStudySessionById(sessionId);
```

#### Update Quiz Score
```java
dbHelper.updateStudySession(sessionId, newScore, pdfGenerated);
```

#### Delete Session
```java
dbHelper.deleteStudySession(sessionId);
```

## Integration with Audio Processing Workflow

### When to Save a Study Session

The ideal place to save a study session is after ALL processing is complete:
1. ✅ Transcription received
2. ✅ Summary generated
3. ✅ Quiz generated

### Recommended Implementation

In `ProcessAudioActivity.java`, after transcription, summary, and quiz are all successfully generated:

```java
// After all three async operations complete (transcription, summary, quiz)
private void saveStudySession(String transcription, String summary, String quizJson) {
    try {
        DatabaseHelper db = new DatabaseHelper(this);
        
        // Calculate word count from transcription
        int wordCount = transcription.split("\\s+").length;
        
        // Get file duration from audio file
        int duration = getAudioFileDuration(audioUri);
        
        // Save to database
        long sessionId = db.insertStudySession(
            fileName,
            audioUriString,
            transcription,
            summary,
            quizJson,
            getCurrentFormattedDate(),
            duration,
            wordCount,
            0.0,  // Initial quiz score
            false // PDF not generated yet
        );
        
        if (sessionId > 0) {
            Log.d("ProcessAudioActivity", "✅ Study Session saved: ID " + sessionId);
            Toast.makeText(this, "Session saved to history", Toast.LENGTH_SHORT).show();
        }
    } catch (Exception e) {
        Log.e("ProcessAudioActivity", "❌ Error saving study session: " + e.getMessage());
    }
}

// Helper to get current timestamp
private String getCurrentFormattedDate() {
    return new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
}

// Helper to calculate audio duration
private int getAudioFileDuration(Uri audioUri) {
    try {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(ProcessAudioActivity.this, audioUri);
        mediaPlayer.prepare();
        int duration = mediaPlayer.getDuration() / 1000; // Convert to seconds
        mediaPlayer.release();
        return duration;
    } catch (Exception e) {
        Log.e("ProcessAudioActivity", "Error getting audio duration: " + e.getMessage());
        return 0;
    }
}
```

### Challenge: Asynchronous API Calls

The current implementation has three asynchronous API calls:
- `uploadAudio()` - Transcription
- `generateSummary()` - Summary
- `generateQuiz()` - Quiz

**Solution:** Store responses in member variables and check if all three are complete before saving:

```java
// Member variables to store results
private String transcriptionResult = null;
private String summaryResult = null;
private String quizResult = null;

// In each callback's onResponse():
if (response.isSuccessful()) {
    // Store result
    summaryResult = response.body().getSummary();
    
    // Check if all three are complete
    if (allProcessingComplete()) {
        saveStudySession(transcriptionResult, summaryResult, quizResult);
    }
}

private boolean allProcessingComplete() {
    return transcriptionResult != null && 
           summaryResult != null && 
           quizResult != null;
}
```

## Displaying History

### HistoryActivity
- **Location:** `HistoryActivity.java`
- **Loads:** All Study Sessions from database
- **Display:** RecyclerView with latest-first ordering
- **Click:** Opens StudySessionDetailActivity
- **Delete:** Removes from database

### StudySessionDetailActivity
- **Location:** `StudySessionDetailActivity.java`
- **Shows:** Full transcription, summary, quiz results
- **Features:** Update quiz score, delete session
- **Layout:** `activity_study_session_detail.xml`

## Testing

### Manual Test Scenario
1. Upload audio file via `ProcessAudioActivity`
2. Wait for transcription, summary, and quiz generation
3. Navigate to History Tracking button
4. Verify new session appears in list (latest first)
5. Click on session to view full details
6. Test update quiz score functionality
7. Test delete functionality

### Debugging
```java
// Check database for study sessions
ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();
Log.d("Debug", "Total sessions: " + sessions.size());
for (StudySession s : sessions) {
    Log.d("Debug", s.toString());
}
```

## Future Enhancements
- [ ] Batch saving (wait for all 3 processes)
- [ ] PDF report generation for sessions
- [ ] Export session to file
- [ ] Share session results
- [ ] Analytics dashboard per session
- [ ] Favorite/bookmark sessions
- [ ] Full-text search in transcriptions
- [ ] Tags/categories for sessions

## References
- **StudySession Model:** `StudySession.java`
- **Database Helper:** `DatabaseHelper.java` (Study Sessions section)
- **History UI:** `HistoryActivity.java`, `HistoryAdapter.java`
- **Detail View:** `StudySessionDetailActivity.java`
- **Processing:** `ProcessAudioActivity.java`
