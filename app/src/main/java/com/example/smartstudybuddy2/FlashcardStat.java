package com.example.smartstudybuddy2;

public class FlashcardStat {
    private int id;
    private int flashcardId;
    private int reviewCount;
    private int masteryLevel;  // 1=Learning, 2=Familiar, 3=Confident, 4=Mastering, 5=Mastered
    private String lastReviewedDate;

    public FlashcardStat(int id, int flashcardId, int reviewCount, int masteryLevel, String lastReviewedDate) {
        this.id = id;
        this.flashcardId = flashcardId;
        this.reviewCount = reviewCount;
        this.masteryLevel = masteryLevel;
        this.lastReviewedDate = lastReviewedDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getFlashcardId() {
        return flashcardId;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getMasteryLevel() {
        return masteryLevel;
    }

    public String getMasteryLabel() {
        switch (masteryLevel) {
            case 1: return "🟥 Learning";
            case 2: return "🟨 Familiar";
            case 3: return "🟦 Confident";
            case 4: return "🟩 Mastering";
            case 5: return "🟢 Mastered";
            default: return "Unknown";
        }
    }

    public String getLastReviewedDate() {
        return lastReviewedDate;
    }

    // Setters
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public void setLastReviewedDate(String lastReviewedDate) {
        this.lastReviewedDate = lastReviewedDate;
    }

    @Override
    public String toString() {
        return "FlashcardStat{" +
                "id=" + id +
                ", flashcardId=" + flashcardId +
                ", reviewCount=" + reviewCount +
                ", masteryLevel=" + masteryLevel + " (" + getMasteryLabel() + ")" +
                ", lastReviewedDate='" + lastReviewedDate + '\'' +
                '}';
    }
}
