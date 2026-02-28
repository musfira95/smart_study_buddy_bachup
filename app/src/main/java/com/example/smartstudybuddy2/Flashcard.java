package com.example.smartstudybuddy2;

public class Flashcard {
    private int id;
    private String question;
    private String answer;
    private String topic;
    private String createdDate;
    private int reviewCount;      // ✅ Track how many times reviewed
    private boolean isMastered;   // ✅ Track if card is mastered

    public Flashcard(int id, String question, String answer, String topic, String createdDate) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.topic = topic;
        this.createdDate = createdDate;
        this.reviewCount = 0;
        this.isMastered = false;
    }

    public Flashcard(int id, String question, String answer, String topic) {
        this(id, question, answer, topic, "");
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTopic() {
        return topic;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean isMastered() {
        return isMastered;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void setMastered(boolean mastered) {
        isMastered = mastered;
    }

    // ✅ Helper methods
    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public void markAsMastered() {
        this.isMastered = true;
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", topic='" + topic + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", reviewCount=" + reviewCount +
                ", isMastered=" + isMastered +
                '}';
    }
}
