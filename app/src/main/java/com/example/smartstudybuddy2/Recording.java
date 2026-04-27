package com.example.smartstudybuddy2;

public class Recording {
    private int id;
    private String title;
    private String filePath;
    private String date;
    private int duration;
    private String transcription; // ✅ Added
    private String summary; // ✅ Added
    private String quiz_json; // ✅ Added

    public Recording(int id, String title, String filePath, String date, int duration) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.date = date;
        this.duration = duration;
        this.transcription = "";
        this.summary = "";
        this.quiz_json = "[]";
    }

    // ✅ Constructor with transcription
    public Recording(int id, String title, String filePath, String date, int duration, String transcription) {
        this(id, title, filePath, date, duration);
        this.transcription = transcription;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getDate() { return date; }
    public int getDuration() { return duration; }
    public String getTranscription() { return transcription != null ? transcription : ""; } // ✅ Added
    public String getSummary() { return summary != null ? summary : ""; } // ✅ Added
    public String getQuizJson() { return quiz_json != null ? quiz_json : "[]"; } // ✅ Added
    
    // ✅ Setters for updating fields
    public void setSummary(String summary) { this.summary = summary; }
    public void setQuizJson(String quiz_json) { this.quiz_json = quiz_json; }
    
    private String topic; // ✅ Added
    public String getTopic() { return topic != null ? topic : "General"; } // ✅ Added
    public void setTopic(String topic) { this.topic = topic; } // ✅ Added
}
