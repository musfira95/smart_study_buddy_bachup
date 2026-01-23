package com.example.smartstudybuddy2;

public class Recording {
    private int id;
    private String title;
    private String filePath;
    private String date;
    private int duration;

    public Recording(int id, String title, String filePath, String date, int duration) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.date = date;
        this.duration = duration;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getDate() { return date; }
    public int getDuration() { return duration; }
}
