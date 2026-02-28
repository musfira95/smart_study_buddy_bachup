package com.example.smartstudybuddy2.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("questions")
    public List<QuizQuestionItem> questions;

    @SerializedName("total_questions")
    public int totalQuestions;

    public QuizResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public List<QuizQuestionItem> getQuestions() {
        return questions;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public static class QuizQuestionItem {
        @SerializedName("question")
        public String question;

        @SerializedName("options")
        public List<OptionItem> options;

        @SerializedName("explanation")
        public String explanation;

        public QuizQuestionItem() {
        }

        public String getQuestion() {
            return question;
        }

        public List<OptionItem> getOptions() {
            return options;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    public static class OptionItem {
        @SerializedName("option")
        public String option;

        @SerializedName("is_correct")
        public boolean isCorrect;

        public OptionItem() {
        }

        public String getOption() {
            return option;
        }

        public boolean isCorrect() {
            return isCorrect;
        }
    }
}
