package com.example.smartstudybuddy2;

/**
 * ScheduleReminder Model Class
 * Represents a reminder/schedule item linked to study features
 */
public class ScheduleReminder {
    private int id;
    private String title;
    private String description;
    private String date;              // Format: yyyy-MM-dd
    private String time;              // Format: HH:mm
    private String featureType;       // reminder, recording, flashcard, quiz, pdf
    private int featureId;            // ID of linked feature (0 if none)
    private boolean isCompleted;      // true if reminder completed

    /**
     * Constructor
     */
    public ScheduleReminder(int id, String title, String description, String date, 
                           String time, String featureType, int featureId, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.featureType = featureType;
        this.featureId = featureId;
        this.isCompleted = isCompleted;
    }

    // ========== GETTERS ==========

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFeatureType() {
        return featureType;
    }

    public int getFeatureId() {
        return featureId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // ========== SETTERS ==========

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    // ========== HELPER METHODS ==========

    /**
     * Get icon emoji based on feature type
     */
    public String getFeatureIcon() {
        if (featureType == null) return "📌";
        switch (featureType.toLowerCase()) {
            case "recording":
                return "🎙️";
            case "flashcard":
                return "📚";
            case "quiz":
                return "❓";
            case "pdf":
                return "📄";
            default:
                return "📌";
        }
    }

    /**
     * Get display time format (e.g., "3:30 PM")
     */
    public String getDisplayTime() {
        if (time == null || time.isEmpty()) return "";
        
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            String ampm = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0) hour = 12;
            
            return String.format("%d:%02d %s", hour, minute, ampm);
        } catch (Exception e) {
            return time;  // Return original if parsing fails
        }
    }

    /**
     * Get status text
     */
    public String getStatus() {
        return isCompleted ? "✅ Completed" : "⏳ Pending";
    }

    /**
     * Check if this is a past reminder
     */
    public boolean isPast() {
        try {
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            return date.compareTo(today) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if this is today's reminder
     */
    public boolean isToday() {
        try {
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            return date.equals(today);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "ScheduleReminder{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", featureType='" + featureType + '\'' +
                ", featureId=" + featureId +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
