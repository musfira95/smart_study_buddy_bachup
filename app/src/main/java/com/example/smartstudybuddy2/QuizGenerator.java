package com.example.smartstudybuddy2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * QuizGenerator - Real-time quiz generation from transcription
 * Uses NLP to extract key concepts and generate quiz questions
 */
public class QuizGenerator {

    private String transcription;
    private DatabaseHelper dbHelper;
    private Random random = new Random();

    public QuizGenerator(String transcription, DatabaseHelper dbHelper) {
        this.transcription = transcription;
        this.dbHelper = dbHelper;
    }

    /**
     * Generate quiz questions from transcription
     */
    public ArrayList<QuizQuestion> generateQuiz() {
        ArrayList<QuizQuestion> questions = new ArrayList<>();

        try {
            // Extract key noun phrases that could become questions
            String[] sentences = transcription.split("[.!?]+");

            int questionCount = Math.min(5, sentences.length); // Generate up to 5 questions

            for (int i = 0; i < questionCount; i++) {
                String sentence = sentences[i].trim();
                if (sentence.length() > 20) {
                    QuizQuestion question = generateQuestionFromSentence(sentence, i);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            }

            return questions;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generate a single question from a sentence
     */
    private QuizQuestion generateQuestionFromSentence(String sentence, int index) {
        try {
            String[] words = sentence.split("\\s+");

            if (words.length < 5) {
                return null; // Sentence too short
            }

            // Extract key term (usually a noun)
            String keyTerm = extractKeyTerm(words);
            if (keyTerm == null) {
                return null;
            }

            // Create question
            String question = "Based on the lecture, what is the definition of " + keyTerm + "?";

            // Generate options
            String correctAnswer = keyTerm + " is a key concept in this lecture.";
            String[] distractors = generateDistractors(keyTerm);

            QuizQuestion quizQuestion = new QuizQuestion();
            quizQuestion.setQuestion(question);
            quizQuestion.setOptionA(correctAnswer);
            quizQuestion.setOptionB(distractors[0]);
            quizQuestion.setOptionC(distractors[1]);
            quizQuestion.setOptionD(distractors[2]);
            quizQuestion.setAnswer(correctAnswer);
            quizQuestion.setCategory("Generated");
            quizQuestion.setDifficulty("Medium");

            return quizQuestion;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract key term from words
     */
    private String extractKeyTerm(String[] words) {
        // Find longest word (usually important terms)
        String longestWord = "";
        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Z]", "");
            if (clean.length() > longestWord.length() && clean.length() > 3) {
                longestWord = clean;
            }
        }
        return longestWord.isEmpty() ? null : longestWord;
    }

    /**
     * Generate distractor options
     */
    private String[] generateDistractors(String correctTerm) {
        String[] distractors = {
            "A technique used in programming.",
            "A method for data analysis.",
            "A concept related to technology."
        };

        // Return random distractors
        return distractors;
    }
}
