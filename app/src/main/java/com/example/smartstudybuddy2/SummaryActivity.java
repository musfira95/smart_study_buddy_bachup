package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;
import java.util.Locale;
import com.example.smartstudybuddy2.network.LocalApiClient;

public class SummaryActivity extends BaseActivity {

    private TextView tvSummary;
    private LinearLayout btnGenerateQuiz, btnSkipQuiz;
    private android.widget.ImageView btnBack;
    
    // 🔊 Text-to-Speech
    private TextToSpeech tts;
    private ImageButton btnPlaySummary, btnPauseSummary, btnStopSummary;
    private boolean isSpeaking = false;
    private String currentSummaryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvSummary = findViewById(R.id.tvSummaryContent);
        btnGenerateQuiz = findViewById(R.id.btnGenerateQuiz);
        btnSkipQuiz = findViewById(R.id.btnSkipQuiz);
        btnBack = findViewById(R.id.btnBack);
        
        // 🔊 Initialize Text-to-Speech buttons
        btnPlaySummary = findViewById(R.id.btnPlaySummary);
        btnPauseSummary = findViewById(R.id.btnPauseSummary);
        btnStopSummary = findViewById(R.id.btnStopSummary);

        String transcriptionText = getIntent().getStringExtra("transcription");
        String fileName = getIntent().getStringExtra("fileName");
        long recordingId = getIntent().getLongExtra("recordingId", -1);  // ✅ GET RECORDING ID

        // Generate real summary from transcription
        String summaryText = generateSummary(transcriptionText);
        currentSummaryText = summaryText;  // Store for TTS
        tvSummary.setText(summaryText);
        
        // 🔊 Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set language to English (or device default)
                int langResult = tts.setLanguage(Locale.ENGLISH);
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    android.util.Log.w("SummaryActivity", "⚠️ English language not supported, using default");
                    tts.setLanguage(Locale.getDefault());
                }
                // Set speech rate
                tts.setSpeechRate(1.0f);  // Normal speed
                android.util.Log.d("SummaryActivity", "✅ TextToSpeech initialized successfully");
            } else {
                android.util.Log.e("SummaryActivity", "❌ TextToSpeech initialization failed");
            }
        });

        // Save summary to database
        DatabaseHelper db = new DatabaseHelper(this);
        db.insertSummary(fileName, summaryText, transcriptionText);
        
        // ✅ SAVE ENHANCED SUMMARY TO SUMMARIES TABLE FOR PDF EXPORT
        if (recordingId > 0) {
            try {
                // Get text length for stats
                int originalLength = transcriptionText != null ? transcriptionText.length() : 0;
                int summaryLength = summaryText != null ? summaryText.length() : 0;
                float compressionRatio = originalLength > 0 ? (float) summaryLength / originalLength : 0;
                
                // Save to summaries table (same one used by PDF export)
                android.database.sqlite.SQLiteDatabase sqliteDb = db.getWritableDatabase();
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("recording_id", (int) recordingId);
                values.put("summary_text", summaryText);
                values.put("original_length", originalLength);
                values.put("summary_length", summaryLength);
                values.put("compression_ratio", compressionRatio);
                values.put("created_at", System.currentTimeMillis());
                values.put("updated_at", System.currentTimeMillis());
                
                // Insert or update
                long result = sqliteDb.insertWithOnConflict(
                    "summaries", 
                    null, 
                    values, 
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                );
                
                if (result >= 0) {
                    android.util.Log.d("SummaryActivity", "✅ Enhanced summary saved to summaries table for PDF export (recordingId=" + recordingId + ")");
                } else {
                    android.util.Log.e("SummaryActivity", "❌ Failed to save to summaries table");
                }
                sqliteDb.close();
            } catch (Exception e) {
                android.util.Log.e("SummaryActivity", "⚠️ Error saving to summaries table: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Also update audio_files for backward compatibility
            boolean updated = db.updateRecordingSummary((int) recordingId, summaryText);
            android.util.Log.d("SummaryActivity", "📝 Summary saved to audio_files: " + updated + " (recordingId=" + recordingId + ")");
            
            // 🎯 DETECT TOPIC from summary
            detectAndSaveTopic((int) recordingId, summaryText);
        } else {
            android.util.Log.w("SummaryActivity", "⚠️ No recordingId available to save summary");
        }

        Toast.makeText(this, "Summary Generated Successfully", Toast.LENGTH_SHORT).show();
        com.example.smartstudybuddy2.utils.NotificationManager.notifySummaryReady(this, fileName != null ? fileName : "Recording");

        // Generate quiz
        btnGenerateQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, QuizActivity.class);
            intent.putExtra("summaryText", summaryText);
            intent.putExtra("transcription", transcriptionText);
            intent.putExtra("recordingId", recordingId);  // ✅ PASS RECORDING ID
            startActivity(intent);
        });

        // Skip quiz
        btnSkipQuiz.setOnClickListener(v -> {
            Toast.makeText(SummaryActivity.this, "Quiz Skipped", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SummaryActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // 🔊 Text-to-Speech Buttons
        if (btnPlaySummary != null) {
            btnPlaySummary.setOnClickListener(v -> {
                if (tts != null && !tts.isSpeaking() && !currentSummaryText.isEmpty()) {
                    // Clean text for better pronunciation
                    String cleanText = currentSummaryText.replaceAll("[^a-zA-Z0-9\\s.,!?]", "");
                    tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null);
                    isSpeaking = true;
                    Toast.makeText(SummaryActivity.this, "🔊 Playing Summary...", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("SummaryActivity", "🔊 Speaking summary (" + cleanText.length() + " chars)");
                } else if (tts != null && tts.isSpeaking()) {
                    Toast.makeText(SummaryActivity.this, "⚠️ Already speaking", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SummaryActivity.this, "❌ TextToSpeech not ready", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (btnPauseSummary != null) {
            btnPauseSummary.setOnClickListener(v -> {
                if (tts != null && tts.isSpeaking()) {
                    tts.stop();
                    isSpeaking = false;
                    Toast.makeText(SummaryActivity.this, "⏸ Paused", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("SummaryActivity", "⏸ Speech paused");
                } else {
                    Toast.makeText(SummaryActivity.this, "⚠️ Nothing is playing", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (btnStopSummary != null) {
            btnStopSummary.setOnClickListener(v -> {
                if (tts != null) {
                    tts.stop();
                    isSpeaking = false;
                    Toast.makeText(SummaryActivity.this, "⏹ Stopped", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("SummaryActivity", "⏹ Speech stopped");
                }
            });
        }
    }

    /**
     * Generate ENHANCED summary with KEY POINTS (same logic as ProcessAudioActivity)
     * Returns format: "SUMMARY:\n[text]\n\nKEY POINTS:\n• keyword1\n• keyword2\n..."
     */
    private String generateSummary(String transcription) {
        if (transcription == null || transcription.isEmpty()) {
            return "No transcription available for summarization.";
        }

        try {
            // Step 1: Split into sentences
            String[] sentences = transcription.split("[.!?]+");

            // Step 2: Find key sentences (first 30% of sentences usually contain main ideas)
            int summaryLength = Math.max(1, sentences.length / 3);
            StringBuilder summary = new StringBuilder();

            // Add "SUMMARY:" header and main paragraph
            summary.append("SUMMARY:\n");
            for (int i = 0; i < Math.min(summaryLength, sentences.length); i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    summary.append(sentence).append(". ");
                }
            }

            // Step 3: Extract important keywords
            String keywordsSummary = extractKeywords(transcription);

            // Combine into enhanced format (SAME FORMAT AS ProcessAudioActivity)
            return summary.toString() + "\n\nKEY POINTS:\n" + keywordsSummary;

        } catch (Exception e) {
            android.util.Log.e("SummaryActivity", "Error generating summary: " + e.getMessage());
            return "Error generating summary: " + e.getMessage();
        }
    }

    /**
     * Extract important keywords from transcription
     */
    private String extractKeywords(String text) {
        // Common stop words to exclude
        String[] stopWords = {
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been"
        };

        try {
            String[] words = text.toLowerCase().split("\\s+");
            java.util.Map<String, Integer> wordFreq = new java.util.HashMap<>();

            // Count word frequencies
            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-z0-9]", "");
                if (cleanWord.length() > 4 && !isStopWord(cleanWord, stopWords)) {
                    wordFreq.put(cleanWord, wordFreq.getOrDefault(cleanWord, 0) + 1);
                }
            }

            // Sort by frequency
            java.util.List<java.util.Map.Entry<String, Integer>> sortedWords =
                new java.util.ArrayList<>(wordFreq.entrySet());
            sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // Get top 10 keywords
            StringBuilder keywords = new StringBuilder();
            int count = 0;
            for (java.util.Map.Entry<String, Integer> entry : sortedWords) {
                if (count >= 10) break;
                keywords.append("• ").append(entry.getKey()).append("\n");
                count++;
            }

            return keywords.toString();

        } catch (Exception e) {
            return "Unable to extract keywords: " + e.getMessage();
        }
    }

    /**
     * Check if word is a stop word
     */
    private boolean isStopWord(String word, String[] stopWords) {
        for (String stopWord : stopWords) {
            if (word.equals(stopWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 🔊 Cleanup TextToSpeech resources
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            android.util.Log.d("SummaryActivity", "🔊 TextToSpeech shutdown");
        }
    }

    /**
     * 🎯 Detect topic from summary/transcription and save to database
     */
    private void detectAndSaveTopic(int recordingId, String summaryText) {
        new Thread(() -> {
            try {
                // Create request JSON
                org.json.JSONObject request = new org.json.JSONObject();
                request.put("text", summaryText != null ? summaryText : "");

                // Make API call
                okhttp3.Request apiRequest = new okhttp3.Request.Builder()
                    .url(LocalApiClient.BASE_URL + "detect-topic/")
                    .post(okhttp3.RequestBody.create(request.toString(), okhttp3.MediaType.parse("application/json")))
                    .build();

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Response response = client.newCall(apiRequest).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    org.json.JSONObject responseJson = new org.json.JSONObject(responseBody);
                    
                    String detectedTopic = responseJson.optString("topic", "General");
                    double confidence = responseJson.optDouble("confidence", 0.0);
                    
                    android.util.Log.d("SummaryActivity", "🎯 Topic Detected: " + detectedTopic + " (confidence: " + confidence + ")");

                    // Save to database
                    DatabaseHelper db = new DatabaseHelper(SummaryActivity.this);
                    boolean updated = db.updateRecordingTopic(recordingId, detectedTopic);
                    
                    if (updated) {
                        android.util.Log.d("SummaryActivity", "✅ Topic saved to database: " + detectedTopic);
                    } else {
                        android.util.Log.w("SummaryActivity", "⚠️ Failed to save topic to database");
                    }
                } else {
                    android.util.Log.e("SummaryActivity", "❌ Topic detection failed: " + response.code());
                }
                response.close();

            } catch (Exception e) {
                android.util.Log.e("SummaryActivity", "❌ Topic detection error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
