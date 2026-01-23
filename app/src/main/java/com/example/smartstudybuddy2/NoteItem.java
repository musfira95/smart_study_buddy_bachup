package com.example.smartstudybuddy2;

public class NoteItem {

    private int id;
    private String title;
    private String content;
    private boolean isBookmarked; // new field for bookmark

    // Constructor
    public NoteItem(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isBookmarked = false; // default: not bookmarked
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // Bookmark methods
    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void toggleBookmark() {
        isBookmarked = !isBookmarked;
    }
}