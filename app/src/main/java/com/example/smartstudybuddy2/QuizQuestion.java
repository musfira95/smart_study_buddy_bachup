package com.example.smartstudybuddy2;

public class QuizQuestion {
    private String question, optionA, optionB, optionC, optionD, answer;
    private String category;
    private String difficulty;

    // Default constructor (no-arg)
    public QuizQuestion() {
        this.question = "";
        this.optionA = "";
        this.optionB = "";
        this.optionC = "";
        this.optionD = "";
        this.answer = "";
        this.category = "";
        this.difficulty = "";
    }

    // Constructor with all parameters
    public QuizQuestion(String question, String optionA, String optionB, String optionC, String optionD, String answer) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.answer = answer;
        this.category = "";
        this.difficulty = "";
    }

    // Getters
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getAnswer() { return answer; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }

    // Setters
    public void setQuestion(String question) { this.question = question; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setCategory(String category) { this.category = category; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}

