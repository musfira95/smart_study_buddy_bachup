package com.example.smartstudybuddy2;

public class SearchResult {
    public static final int TYPE_RECORDING = 1;
    public static final int TYPE_NOTE = 2;
    public static final int TYPE_FLASHCARD = 3;
    public static final int TYPE_QUIZ = 4;

    private int id;
    private int type;  // TYPE_RECORDING, TYPE_NOTE, TYPE_FLASHCARD, or TYPE_QUIZ
    private String title;
    private String contentPreview;
    private String timestamp;

    public SearchResult(int id, int type, String title, String contentPreview, String timestamp) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.contentPreview = contentPreview;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTypeLabel() {
        switch (type) {
            case TYPE_RECORDING:
                return "🎤 Recording";
            case TYPE_NOTE:
                return "📝 Note";
            case TYPE_FLASHCARD:
                return "🎴 Flashcard";
            case TYPE_QUIZ:
                return "❓ Quiz";
            default:
                return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id=" + id +
                ", type=" + type + " (" + getTypeLabel() + ")" +
                ", title='" + title + '\'' +
                ", preview='" + contentPreview + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
