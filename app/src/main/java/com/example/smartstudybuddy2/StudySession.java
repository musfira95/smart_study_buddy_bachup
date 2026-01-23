package com.example.smartstudybuddy2;

public class StudySession {
    public int id;
    public String subject;
    public String duration;
    public String date;

    public StudySession(int id, String subject, String duration, String date){
        this.id = id;
        this.subject = subject;
        this.duration = duration;
        this.date = date;
    }
}
