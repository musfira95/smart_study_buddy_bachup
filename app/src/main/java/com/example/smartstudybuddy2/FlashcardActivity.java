package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * ✅ Flashcard Activity with Review Tracking & Mastery Levels
 * 
 * Features:
 * - Display all flashcards from database with mastery levels
 * - Track review count for each card
 * - Visual mastery indicators (🟥 Learning → 🟢 Mastered)
 * - Click to review and rate difficulty
 * - Summary stats showing mastered cards progress
 */
public class FlashcardActivity extends AppCompatActivity {
    private static final String TAG = "FlashcardActivity";
    
    private RecyclerView rvFlashcards;
    private FlashcardAdapter adapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Flashcard> flashcards;
    private TextView tvStatsBar;
    
    // ✅ NEW: Session tracking
    private int sessionId = -1;
    private String sessionTitle = "Flashcards";
    private StudySession currentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // Setup Toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        rvFlashcards = findViewById(R.id.rvFlashcards);
        tvStatsBar = findViewById(R.id.tvStatsBar);

        // ✅ NEW: Get session_id from Intent (passed from SelectSessionForFlashcardsActivity)
        sessionId = getIntent().getIntExtra("session_id", -1);
        sessionTitle = getIntent().getStringExtra("session_title");
        if (sessionTitle == null) {
            sessionTitle = "Flashcards";
        }
        
        Log.d(TAG, "📚 FlashcardActivity opened for session_id=" + sessionId + ", title=" + sessionTitle);
        
        // Update title/header
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(sessionTitle);
        }

        // Load flashcards with mastery tracking
        loadFlashcards();
        updateMasteryStats();


        Log.d(TAG, "✅ FlashcardActivity initialized with mastery tracking");
    }

    private void loadFlashcards() {
        try {
            flashcards = new ArrayList<>();
            
            if (sessionId <= 0) {
                Log.e(TAG, "❌ Invalid session ID: " + sessionId);
                Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            Log.d(TAG, "📚 Loading flashcards from session_id=" + sessionId);
            
            // Step 1: Fetch session from database
            currentSession = dbHelper.getStudySessionById(sessionId);
            
            if (currentSession == null) {
                Log.d(TAG, "⚠️ Not found in study_sessions, checking recordings...");
                Recording recording = dbHelper.getRecordingById(sessionId);
                
                if (recording == null) {
                    Log.e(TAG, "❌ Session/Recording not found with ID: " + sessionId);
                    Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                // Convert Recording to StudySession
                currentSession = new StudySession(
                        recording.getId(),
                        recording.getTitle() != null ? recording.getTitle() : "Recording",
                        recording.getFilePath() != null ? recording.getFilePath() : "",
                        recording.getTranscription() != null ? recording.getTranscription() : "",
                        "",  // No summary for recordings
                        "[]",
                        recording.getDate(),
                        0,
                        0,
                        0.0,
                        false
                );
            }
            
            if (currentSession == null) {
                Toast.makeText(this, "Could not load session data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Step 2: Get summary text from session (prefer summary, but fall back to transcription)
            String summaryText = currentSession.getSummary();
            Log.d(TAG, "📝 Summary text: " + (summaryText != null ? summaryText.substring(0, Math.min(80, summaryText.length())) + "..." : "NULL"));

            // ✅ If no summary, fall back to transcription
            if (summaryText == null || summaryText.trim().isEmpty()) {
                Log.w(TAG, "⚠️ No summary available, using transcription for flashcards");
                summaryText = currentSession.getTranscription();
            }
            
            // ✅ Only abort if both summary AND transcription are missing
            if (summaryText == null || summaryText.trim().isEmpty()) {
                Log.e(TAG, "❌ No summary or transcription available for session_id=" + sessionId);
                Toast.makeText(this, "No content available for flashcards", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Step 3: Split summary into sentences
            flashcards = generateFlashcardsFromSummary(summaryText);
            
            if (flashcards == null || flashcards.isEmpty()) {
                Log.e(TAG, "❌ No flashcards generated from summary");
                Toast.makeText(this, "Could not generate flashcards", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // ✅ CRITICAL: Save generated flashcards to database so /export-pdf can retrieve them
            saveGeneratedFlashcardsToDatabase(flashcards);
            
            // ✅ NEW: Load database stats back into flashcard model
            loadFlashcardStatsFromDatabase(flashcards);
            
            Log.d(TAG, "✅ Generated " + flashcards.size() + " flashcards from summary");
            
            // Step 4: Display in RecyclerView
            adapter = new FlashcardAdapter(flashcards, flashcard -> {
                if (flashcard != null) {
                    onFlashcardItemClick(flashcard);
                } else {
                    Log.e(TAG, "❌ Flashcard is null");
                    Toast.makeText(this, "Error: Flashcard data is invalid", Toast.LENGTH_SHORT).show();
                }
            });
            
            if (rvFlashcards != null) {
                rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
                rvFlashcards.setAdapter(adapter);
            } else {
                Log.e(TAG, "❌ RecyclerView not found");
            }
            
            Log.d(TAG, "✅ Flashcards displayed: " + flashcards.size() + " cards");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading flashcards: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * ✅ CRITICAL: Save generated flashcards to database AND backend storage
     * This ensures /export-pdf can retrieve them using recording_id
     * 
     * Uses sessionTitle as the topic for database storage
     */
    private void saveGeneratedFlashcardsToDatabase(ArrayList<Flashcard> cards) {
        if (cards == null || cards.isEmpty()) {
            Log.w(TAG, "⚠️ No flashcards to save");
            return;
        }
        
        try {
            int saved = 0;
            for (Flashcard card : cards) {
                // Extract question and answer from the formatted text
                String fullText = card.getQuestion();  // Contains "Q: ...\n\nA: ..."
                String answer = card.getAnswer();      // Original sentence
                
                // Use sessionTitle as topic for database
                String topic = sessionTitle != null ? sessionTitle : "Flashcards";
                
                // Insert into database - this links flashcard with current recording via timestamp
                boolean inserted = dbHelper.insertFlashcard(fullText, answer, topic);
                
                if (inserted) {
                    saved++;
                    Log.d(TAG, "✅ Saved flashcard " + saved + " to database: Topic=" + topic);
                } else {
                    Log.w(TAG, "⚠️ Failed to save flashcard: " + fullText.substring(0, Math.min(50, fullText.length())));
                }
            }
            
            Log.d(TAG, "✅ COMPLETE: Saved " + saved + "/" + cards.size() + " flashcards to database for /export-pdf");
            
            // ✅ AFTER saving to local database, also save to BACKEND storage
            // This ensures /export-pdf endpoint can fetch them
            if (sessionId > 0) {
                saveFlashcardsToBackendStorage(cards, sessionId);
            }
            
            Toast.makeText(this, "✅ " + saved + " flashcards saved", Toast.LENGTH_SHORT).show();
            if (saved > 0) {
                String topic = sessionTitle != null ? sessionTitle : "Recording";
                com.example.smartstudybuddy2.utils.NotificationManager.notifyFlashcardCreated(this, topic);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving flashcards to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ✅ NEW: Save flashcards to backend storage
     * Calls /save-flashcards/ endpoint on FastAPI backend
     * This allows /export-pdf to fetch flashcards using recording_id
     */
    private void saveFlashcardsToBackendStorage(ArrayList<Flashcard> cards, int recordingId) {
        new Thread(() -> {
            try {
                Log.d(TAG, "💾 Uploading flashcards to backend storage...");
                
                // Build list of flashcard maps
                java.util.List<java.util.Map<String, String>> flashcardsList = new java.util.ArrayList<>();
                for (Flashcard card : cards) {
                    java.util.Map<String, String> cardMap = new java.util.HashMap<>();
                    cardMap.put("question", card.getQuestion());
                    cardMap.put("answer", card.getAnswer());
                    flashcardsList.add(cardMap);
                }
                
                // Build request JSON
                java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
                requestMap.put("recording_id", recordingId);
                requestMap.put("flashcards", flashcardsList);
                
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String json = gson.toJson(requestMap);
                
                Log.d(TAG, "📤 POST to /save-flashcards/");
                Log.d(TAG, "   Recording ID: " + recordingId);
                Log.d(TAG, "   Flashcard count: " + flashcardsList.size());
                
                okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
                okhttp3.RequestBody body = okhttp3.RequestBody.create(json, JSON);
                
                // ✅ Use LocalApiClient for save-flashcards endpoint (local backend)
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
                        .build();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://192.168.100.96:8000/save-flashcards/")
                        .post(body)
                        .build();
                
                okhttp3.Response response = client.newCall(request).execute();
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Flashcards saved to backend successfully!");
                    Log.d(TAG, "   Response: " + response.body().string());
                } else {
                    Log.w(TAG, "⚠️ Backend save failed: HTTP " + response.code());
                    Log.w(TAG, "   Response: " + (response.body() != null ? response.body().string() : "No body"));
                }
                response.close();
                
            } catch (Exception e) {
                Log.w(TAG, "⚠️ Error uploading flashcards to backend: " + e.getMessage());
                e.printStackTrace();
                // Don't fail - local storage is already successful
            }
        }).start();
    }
    
    /**
     * ✅ NEW: Load database stats into flashcard model
     * Syncs reviewCount and isMastered from FlashcardStat table
     */
    private void loadFlashcardStatsFromDatabase(ArrayList<Flashcard> cards) {
        if (cards == null || cards.isEmpty()) return;
        
        try {
            for (Flashcard card : cards) {
                FlashcardStat stat = dbHelper.getFlashcardStat(card.getId());
                if (stat != null) {
                    card.setReviewCount(stat.getReviewCount());
                    if (stat.getMasteryLevel() >= 4) {
                        card.setMastered(true);
                    }
                    Log.d(TAG, "✅ Loaded stat for card " + card.getId() + ": reviews=" + stat.getReviewCount() + ", mastered=" + (stat.getMasteryLevel() >= 4));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading stats from database: " + e.getMessage());
        }
    }
    
    /**
     * Generate flashcards from summary text by splitting into sentences
     * One flashcard per sentence with dynamic questions based on content
     */
    private ArrayList<Flashcard> generateFlashcardsFromSummary(String summaryText) {
        ArrayList<Flashcard> generatedCards = new ArrayList<>();
        
        if (summaryText == null || summaryText.trim().isEmpty()) {
            Log.e(TAG, "❌ Summary text is null or empty");
            return generatedCards;
        }
        
        // Step 1: Split by period delimiter
        String[] sentences = summaryText.split("\\.");
        Log.d(TAG, "🔍 Split into " + sentences.length + " sentences");
        
        int cardId = 1;
        
        // Step 2: Process each sentence
        for (String sentence : sentences) {
            // Step 3: Trim whitespace
            sentence = sentence.trim();
            
            // Step 4: Ignore empty sentences
            if (sentence.isEmpty() || sentence.length() < 10) {
                continue;
            }
            
            // Step 5: Generate dynamic question based on sentence content
            String question = generateDynamicQuestion(sentence);
            String answer = sentence;
            
            // Format as: Q: <question>\n\nA: <answer>
            String formattedText = "Q: " + question + "\n\nA: " + answer;
            
            // Step 6: Create flashcard object with formatted text
            Flashcard card = new Flashcard(
                    cardId++,
                    formattedText,  // Full Q/A formatted text as question field
                    answer,         // Original sentence as answer field
                    sessionTitle,
                    java.time.LocalDateTime.now().toString()
            );
            
            // Step 7: Add to list
            generatedCards.add(card);
            Log.d(TAG, "  ✅ [Card " + (cardId-1) + "] Q: " + question);
            Log.d(TAG, "     A: " + answer.substring(0, Math.min(60, answer.length())));
        }
        
        Log.d(TAG, "✅ Total flashcards created: " + generatedCards.size());
        return generatedCards;
    }
    
    /**
     * Generate dynamic question based on sentence content
     * Creates questions that relate directly to the answer text
     * 
     * Strategy:
     * 1. Extract key terms from the beginning of the sentence
     * 2. Create "What is X?" or "Describe X" questions
     * 3. For sentences with "is/are" → "What is described?"
     * 4. For sentences with years → "When did this occur?"
     */
    private String generateDynamicQuestion(String sentence) {
        // ✅ Validate input
        if (sentence == null || sentence.trim().isEmpty()) {
            return "Explain this concept.";
        }
        
        sentence = sentence.trim();
        String lowerSentence = sentence.toLowerCase();
        
        // ✅ RULE 1: Extract first key noun/topic for "What is X?" questions
        // Look for first 1-3 words as the topic
        String[] words = sentence.split("[\\s,]");
        
        if (words.length > 0) {
            // Build a question from first few words
            String firstWord = words[0];
            String secondWord = words.length > 1 ? words[1] : "";
            String thirdWord = words.length > 2 ? words[2] : "";
            
            // Skip small words like "The", "It", "A"
            if (firstWord.length() <= 3 && words.length > 1) {
                firstWord = words[1];
                secondWord = words.length > 2 ? words[2] : "";
                thirdWord = words.length > 3 ? words[3] : "";
            }
            
            // Remove punctuation
            firstWord = firstWord.replaceAll("[^a-zA-Z0-9]", "");
            secondWord = secondWord.replaceAll("[^a-zA-Z0-9]", "");
            thirdWord = thirdWord.replaceAll("[^a-zA-Z0-9]", "");
            
            // Build topic phrase
            String topic = firstWord;
            if (!secondWord.isEmpty() && !secondWord.equalsIgnoreCase("and")) {
                topic += " " + secondWord;
            }
            if (!thirdWord.isEmpty() && !thirdWord.equalsIgnoreCase("and") && !thirdWord.equalsIgnoreCase("the")) {
                topic += " " + thirdWord;
            }
            
            // ✅ RULE 2: Check for year - "When did X occur?"
            if (sentence.matches(".*\\d{4}.*")) {
                Log.d(TAG, "📅 Year detected - " + topic);
                return "When did " + topic + " occur?";
            }
            
            // ✅ RULE 3: Check for "is/are/was/were" - "What is X?"
            if (lowerSentence.contains(" is ") || lowerSentence.contains(" are ") || 
                lowerSentence.contains(" was ") || lowerSentence.contains(" were ")) {
                Log.d(TAG, "🔍 Definition question - " + topic);
                return "What is " + topic + "?";
            }
            
            // ✅ RULE 4: Default - "Describe X" or "Explain X"
            if (topic.length() > 0) {
                Log.d(TAG, "⚙️ Default question - " + topic);
                return "Describe " + topic + ".";
            }
        }
        
        // Fallback
        return "Explain this concept.";
    }

    private void onFlashcardItemClick(Flashcard flashcard) {
        Log.d(TAG, "🎯 Flashcard review started: " + flashcard.getQuestion());
        
        // ✅ Calculate progress stats
        int totalCards = flashcards.size();
        int masteredCount = 0;
        for (Flashcard card : flashcards) {
            if (card.isMastered()) {
                masteredCount++;
            }
        }
        
        // Open flashcard detail for review with mastery tracking
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra("flashcard_id", flashcard.getId());
        intent.putExtra("question", flashcard.getQuestion());
        intent.putExtra("answer", flashcard.getAnswer());
        intent.putExtra("topic", flashcard.getTopic());
        
        // ✅ NEW: Pass progress tracking data
        intent.putExtra("total_cards", totalCards);
        intent.putExtra("review_count", flashcard.getReviewCount());
        intent.putExtra("is_mastered", flashcard.isMastered());
        intent.putExtra("mastered_count", masteredCount);
        
        startActivity(intent);
    }

    private void updateMasteryStats() {
        try {
            if (flashcards == null || flashcards.isEmpty()) {
                if (tvStatsBar != null) {
                    tvStatsBar.setText("🟥 Learning • 0/0 Mastered");
                }
                return;
            }
            
            // ✅ Use Flashcard model fields instead of database stats
            int masteredCount = 0;
            int totalCards = flashcards.size();
            int reviewedCount = 0;
            
            for (Flashcard card : flashcards) {
                if (card.getReviewCount() > 0) {
                    reviewedCount++;
                    if (card.isMastered()) {
                        masteredCount++;
                    }
                }
            }
            
            // ✅ Calculate mastery percentage
            String masteryLevel = "🟥 Learning";
            if (reviewedCount > 0) {
                double masteredPercent = (masteredCount * 100.0) / reviewedCount;
                if (masteredPercent >= 80) {
                    masteryLevel = "🟢 Mastered!";
                } else if (masteredPercent >= 60) {
                    masteryLevel = "🟩 Confident";
                } else if (masteredPercent >= 40) {
                    masteryLevel = "🟨 Familiar";
                } else {
                    masteryLevel = "🟥 Learning";
                }
            }
            
            // ✅ Display stats with total card count
            String statsText = String.format("%s • %d/%d Mastered", masteryLevel, masteredCount, totalCards);
            
            if (tvStatsBar != null) {
                tvStatsBar.setText(statsText);
            }
            
            Log.d(TAG, "📊 Mastery Progress: " + statsText + " (reviewed: " + reviewedCount + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating mastery stats: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh flashcard list whenever returning to this activity
        loadFlashcards();
        updateMasteryStats();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
