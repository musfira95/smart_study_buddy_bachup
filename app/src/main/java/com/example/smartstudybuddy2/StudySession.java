package com.example.smartstudybuddy2;

/**
 * StudySession Model - Represents a completed study session with audio processing results
 * Stores: transcription, summary, quiz, and performance metrics
 */
public class StudySession {
    private int id;
    private String title;              // Audio file name
    private String audioPath;          // Path to uploaded/recorded audio
    private String transcription;      // Full transcription text
    private String summary;            // Extracted summary
    private String quizJson;           // Quiz questions in JSON format
    private String createdDate;        // Timestamp formatted as string
    private int duration;              // Duration in seconds
    private int wordCount;             // Number of words in transcription
    private double quizScore;          // Quiz score out of 100
    private boolean pdfGenerated;      // Whether PDF report was generated

    // Constructor with all fields
    public StudySession(int id, String title, String audioPath, String transcription,
                       String summary, String quizJson, String createdDate,
                       int duration, int wordCount, double quizScore, boolean pdfGenerated) {
        this.id = id;
        this.title = title;
        this.audioPath = audioPath;
        this.transcription = transcription;
        this.summary = summary;
        this.quizJson = quizJson;
        this.createdDate = createdDate;
        this.duration = duration;
        this.wordCount = wordCount;
        this.quizScore = quizScore;
        this.pdfGenerated = pdfGenerated;
    }

    // Backward compatible constructor (for legacy code)
    public StudySession(int id, String subject, String duration, String date) {
        this.id = id;
        this.title = subject;
        this.duration = Integer.parseInt(duration);
        this.createdDate = date;
        this.audioPath = "";
        this.transcription = "";
        this.summary = "";
        this.quizJson = "[]";
        this.wordCount = 0;
        this.quizScore = 0;
        this.pdfGenerated = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getQuizJson() { return quizJson; }
    public void setQuizJson(String quizJson) { this.quizJson = quizJson; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public double getQuizScore() { return quizScore; }
    public void setQuizScore(double quizScore) { this.quizScore = quizScore; }

    public boolean isPdfGenerated() { return pdfGenerated; }
    public void setPdfGenerated(boolean pdfGenerated) { this.pdfGenerated = pdfGenerated; }

    @Override
    public String toString() {
        return "StudySession{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", duration=" + duration + " sec" +
                ", wordCount=" + wordCount +
                ", quizScore=" + quizScore +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
