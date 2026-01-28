package com.example.smartstudybuddy2;

import java.util.*;
import java.util.regex.Pattern;

/**
 * TextProcessor - Real NLP processing for text analysis and manipulation
 * Handles summarization, keyword extraction, and text analysis
 */
public class TextProcessor {

    private String text;
    private String[] sentences;
    private String[] words;
    private Map<String, Integer> wordFrequency;

    public TextProcessor(String text) {
        this.text = text;
        this.sentences = text.split("[.!?]+");
        this.words = text.toLowerCase().split("\\s+");
        calculateWordFrequency();
    }

    /**
     * Calculate word frequency
     */
    private void calculateWordFrequency() {
        wordFrequency = new HashMap<>();
        String[] stopWords = getStopWords();

        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-z0-9]", "");
            if (cleanWord.length() > 3 && !isStopWord(cleanWord, stopWords)) {
                wordFrequency.put(cleanWord, wordFrequency.getOrDefault(cleanWord, 0) + 1);
            }
        }
    }

    /**
     * Extract summary using TF-IDF-like scoring
     */
    public String extractSummary(int summaryPercentage) {
        try {
            int summaryLength = Math.max(1, (sentences.length * summaryPercentage) / 100);
            StringBuilder summary = new StringBuilder();

            // Score sentences based on important word occurrences
            List<ScoredSentence> scoredSentences = new ArrayList<>();
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (sentence.length() > 0) {
                    float score = calculateSentenceScore(sentence);
                    scoredSentences.add(new ScoredSentence(i, sentence, score));
                }
            }

            // Sort by score and take top sentences (in original order)
            Collections.sort(scoredSentences, (a, b) -> Float.compare(b.score, a.score));
            List<ScoredSentence> topSentences = scoredSentences.subList(0, Math.min(summaryLength, scoredSentences.size()));
            Collections.sort(topSentences, (a, b) -> Integer.compare(a.index, b.index));

            for (ScoredSentence ss : topSentences) {
                summary.append(ss.sentence).append(". ");
            }

            return summary.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }

    /**
     * Calculate sentence score based on important words
     */
    private float calculateSentenceScore(String sentence) {
        float score = 0;
        String[] sentenceWords = sentence.toLowerCase().split("\\s+");

        for (String word : sentenceWords) {
            String cleanWord = word.replaceAll("[^a-z0-9]", "");
            if (wordFrequency.containsKey(cleanWord)) {
                score += wordFrequency.get(cleanWord);
            }
        }

        return score / (float) sentenceWords.length; // Normalize by sentence length
    }

    /**
     * Extract keywords
     */
    public List<String> extractKeywords(int topN) {
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
        sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sortedWords.size()); i++) {
            keywords.add(sortedWords.get(i).getKey());
        }
        return keywords;
    }

    /**
     * Highlight keywords in text
     */
    public String highlightKeywords(List<String> keywords) {
        String highlighted = text;
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            highlighted = pattern.matcher(highlighted).replaceAll("**" + keyword + "**");
        }
        return highlighted;
    }

    /**
     * Calculate readability score (Flesch Reading Ease)
     */
    public float calculateReadabilityScore() {
        try {
            int totalWords = words.length;
            int totalSentences = sentences.length;
            int totalSyllables = countSyllables();

            if (totalWords == 0 || totalSentences == 0) return 0;

            float score = 206.835f - (1.015f * (totalWords / (float) totalSentences))
                    - (84.6f * (totalSyllables / (float) totalWords));

            return Math.max(0, Math.min(100, score)); // Clamp between 0-100

        } catch (Exception e) {
            return 50;
        }
    }

    /**
     * Count syllables (approximation)
     */
    private int countSyllables() {
        int count = 0;
        for (String word : words) {
            count += countSyllablesInWord(word);
        }
        return count;
    }

    /**
     * Count syllables in a word
     */
    private int countSyllablesInWord(String word) {
        word = word.toLowerCase();
        int count = 0;
        boolean previousWasVowel = false;

        for (char c : word.toCharArray()) {
            boolean isVowel = "aeiouy".indexOf(c) >= 0;
            if (isVowel && !previousWasVowel) {
                count++;
            }
            previousWasVowel = isVowel;
        }

        // Adjust for silent e
        if (word.endsWith("e")) count--;

        return Math.max(1, count);
    }

    /**
     * Get text statistics
     */
    public TextStatistics getStatistics() {
        TextStatistics stats = new TextStatistics();
        stats.setTotalWords(words.length);
        stats.setTotalSentences(sentences.length);
        stats.setUniqueWords(wordFrequency.size());
        stats.setAverageWordLength(calculateAverageWordLength());
        stats.setAverageSentenceLength(words.length / (float) sentences.length);
        stats.setReadabilityScore(calculateReadabilityScore());
        return stats;
    }

    /**
     * Calculate average word length
     */
    private float calculateAverageWordLength() {
        int totalLength = 0;
        for (String word : words) {
            totalLength += word.replaceAll("[^a-z]", "").length();
        }
        return totalLength / (float) words.length;
    }

    /**
     * Get stop words
     */
    private String[] getStopWords() {
        return new String[]{
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
            "have", "has", "do", "does", "did", "will", "would", "could", "should",
            "as", "if", "while", "that", "this", "it", "its", "which", "who", "when"
        };
    }

    /**
     * Check if word is stop word
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
     * Scored Sentence class
     */
    private static class ScoredSentence {
        int index;
        String sentence;
        float score;

        ScoredSentence(int index, String sentence, float score) {
            this.index = index;
            this.sentence = sentence;
            this.score = score;
        }
    }

    /**
     * Text Statistics class
     */
    public static class TextStatistics {
        private int totalWords;
        private int totalSentences;
        private int uniqueWords;
        private float averageWordLength;
        private float averageSentenceLength;
        private float readabilityScore;

        // Getters
        public int getTotalWords() { return totalWords; }
        public void setTotalWords(int totalWords) { this.totalWords = totalWords; }

        public int getTotalSentences() { return totalSentences; }
        public void setTotalSentences(int totalSentences) { this.totalSentences = totalSentences; }

        public int getUniqueWords() { return uniqueWords; }
        public void setUniqueWords(int uniqueWords) { this.uniqueWords = uniqueWords; }

        public float getAverageWordLength() { return averageWordLength; }
        public void setAverageWordLength(float averageWordLength) { this.averageWordLength = averageWordLength; }

        public float getAverageSentenceLength() { return averageSentenceLength; }
        public void setAverageSentenceLength(float averageSentenceLength) { this.averageSentenceLength = averageSentenceLength; }

        public float getReadabilityScore() { return readabilityScore; }
        public void setReadabilityScore(float readabilityScore) { this.readabilityScore = readabilityScore; }

        @Override
        public String toString() {
            return "TextStatistics{" +
                    "words=" + totalWords +
                    ", sentences=" + totalSentences +
                    ", unique=" + uniqueWords +
                    ", readability=" + String.format("%.1f", readabilityScore) +
                    '}';
        }
    }
}
