package com.example.smartstudybuddy2;

public class ReminderModel {

    String title, time;

    public ReminderModel(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}
