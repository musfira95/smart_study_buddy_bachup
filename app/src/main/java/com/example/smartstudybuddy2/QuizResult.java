package com.example.smartstudybuddy2;

public class QuizResult {
    private int id;
    private String category;
    private int correctCount;
    private int wrongCount;
    private int totalQuestions;
    private double scorePercentage;
    private int durationSeconds;
    private String createdDate;

    // Constructor
    public QuizResult(int id, String category, int correctCount, int wrongCount, 
                     int totalQuestions, double scorePercentage, int durationSeconds, String createdDate) {
        this.id = id;
        this.category = category;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.totalQuestions = totalQuestions;
        this.scorePercentage = scorePercentage;
        this.durationSeconds = durationSeconds;
        this.createdDate = createdDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public double getScorePercentage() {
        return scorePercentage;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setScorePercentage(double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "QuizResult{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", correctCount=" + correctCount +
                ", wrongCount=" + wrongCount +
                ", totalQuestions=" + totalQuestions +
                ", scorePercentage=" + String.format("%.1f", scorePercentage) + "%" +
                ", durationSeconds=" + durationSeconds +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
