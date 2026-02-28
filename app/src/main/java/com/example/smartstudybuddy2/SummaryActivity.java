package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

public class SummaryActivity extends BaseActivity {

    private TextView tvSummary;

    private LinearLayout btnGenerateQuiz, btnSkipQuiz;
    private android.widget.ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvSummary = findViewById(R.id.tvSummaryContent);
        btnGenerateQuiz = findViewById(R.id.btnGenerateQuiz);
        btnSkipQuiz = findViewById(R.id.btnSkipQuiz);
        btnBack = findViewById(R.id.btnBack);

        String transcriptionText = getIntent().getStringExtra("transcription");
        String fileName = getIntent().getStringExtra("fileName");
        long recordingId = getIntent().getLongExtra("recordingId", -1);  // ✅ GET RECORDING ID

        // Generate real summary from transcription
        String summaryText = generateSummary(transcriptionText);
        tvSummary.setText(summaryText);

        // Save summary to database
        DatabaseHelper db = new DatabaseHelper(this);
        db.insertSummary(fileName, summaryText, transcriptionText);
        
        // ✅ ALSO UPDATE AUDIO_FILES TABLE WITH SUMMARY (for PDF export)
        if (recordingId > 0) {
            boolean updated = db.updateRecordingSummary((int) recordingId, summaryText);
            android.util.Log.d("SummaryActivity", "📝 Summary saved to audio_files: " + updated + " (recordingId=" + recordingId + ")");
        } else {
            android.util.Log.w("SummaryActivity", "⚠️ No recordingId available to save summary");
        }

        Toast.makeText(this, "Summary Generated Successfully", Toast.LENGTH_SHORT).show();

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
    }

    /**
     * Generate summary from transcription using NLP techniques
     * Extracts key sentences and important keywords
     */
    private String generateSummary(String transcription) {
        if (transcription == null || transcription.isEmpty()) {
            return "No transcription available for summarization.";
        }

        try {
            // Step 1: Split into sentences
            String[] sentences = transcription.split("[.!?]+");

            // Step 2: Calculate sentence importance scores
            String[] words = transcription.toLowerCase().split("\\s+");

            // Step 3: Find key sentences (first 30% of sentences usually contain main ideas)
            int summaryLength = Math.max(1, sentences.length / 3);
            StringBuilder summary = new StringBuilder();

            // Add first few sentences as they usually contain main topic
            for (int i = 0; i < Math.min(summaryLength, sentences.length); i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    summary.append(sentence).append(". ");
                }
            }

            // Step 4: Extract important keywords
            String keywordsSummary = extractKeywords(transcription);

            return "📚 SUMMARY:\n\n" + summary.toString() + "\n\n" +
                   "🔑 KEY POINTS:\n" + keywordsSummary;

        } catch (Exception e) {
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
}
