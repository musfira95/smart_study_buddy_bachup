package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuizActivity extends BaseActivity {

    TextView tvQuestion;
    RadioGroup rgOptions;
    RadioButton optionA, optionB, optionC, optionD;
    LinearLayout btnNext, btnSubmit;
    android.widget.ImageView btnBack;
    DatabaseHelper dbHelper;

    ArrayList<QuizQuestion> questions = new ArrayList<>();
    int currentIndex = 0;
    int correct = 0;
    int wrong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);

        // ✅ NEW: Check if transcription/summary passed from SummaryActivity
        String transcription = getIntent().getStringExtra("transcription");
        String summaryText = getIntent().getStringExtra("summaryText");

        // If we received transcription/summary, generate dynamic quiz
        if (transcription != null && !transcription.isEmpty()) {
            generateDynamicQuiz(transcription);
            Toast.makeText(this, "Generating Quiz from your lecture...", Toast.LENGTH_SHORT).show();
        } else {
            // Fallback: Load questions from database
            loadQuestionsFromDatabase();

            if (questions.isEmpty()) {
                Toast.makeText(this, "No quiz questions available. Loading defaults...", Toast.LENGTH_SHORT).show();
                loadDummyQuestions();
            }
        }

        if (!questions.isEmpty()) {
            showQuestion();
        } else {
            Toast.makeText(this, "Failed to generate quiz", Toast.LENGTH_SHORT).show();
        }

        // -------------------- Next Question --------------------
        btnNext.setOnClickListener(v -> {
            if (rgOptions.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show();
                return;
            }

            checkAnswer();
            currentIndex++;
            rgOptions.clearCheck();

            if (currentIndex < questions.size()) {
                showQuestion();
            } else {
                btnNext.setVisibility(LinearLayout.GONE);
                btnSubmit.setVisibility(LinearLayout.VISIBLE);
            }
        });

        // -------------------- Submit Quiz --------------------
        btnSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
            intent.putExtra("correctCount", correct);
            intent.putExtra("wrongCount", wrong);
            startActivity(intent);
            finish();
        });

        // Initialize and set listener for back button in Quiz Activity
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

    }

    /**
     * Generate dynamic quiz from transcription using QuizGenerator
     */
    private void generateDynamicQuiz(String transcription) {
        try {
            QuizGenerator quizGenerator = new QuizGenerator(transcription, dbHelper);
            questions = quizGenerator.generateQuiz();

            if (questions.isEmpty()) {
                Toast.makeText(this, "Could not generate questions. Using defaults.", Toast.LENGTH_SHORT).show();
                loadDummyQuestions();
            } else {
                Toast.makeText(this, "Generated " + questions.size() + " questions!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadDummyQuestions();
        }
    }

    // -------------------- Display current question --------------------
    private void showQuestion() {
        QuizQuestion q = questions.get(currentIndex);
        tvQuestion.setText(q.getQuestion());
        optionA.setText(q.getOptionA());
        optionB.setText(q.getOptionB());
        optionC.setText(q.getOptionC());
        optionD.setText(q.getOptionD());
    }

    // -------------------- Check selected answer --------------------
    private void checkAnswer() {
        QuizQuestion q = questions.get(currentIndex);
        RadioButton selected = findViewById(rgOptions.getCheckedRadioButtonId());
        String selectedText = selected.getText().toString();

        if (selectedText.equals(q.getAnswer())) correct++;
        else wrong++;
    }

    // -------------------- Load questions from database --------------------
    private void loadQuestionsFromDatabase() {
        questions = dbHelper.getAllQuizQuestions();
    }

    // -------------------- Fallback dummy questions --------------------
    private void loadDummyQuestions() {
        questions.add(new QuizQuestion(
                "What is AI?",
                "Artificial Input",
                "Automatic Intelligence",
                "Artificial Intelligence",
                "Auto Internet",
                "Artificial Intelligence"
        ));

        questions.add(new QuizQuestion(
                "Which one is a programming language?",
                "HTML",
                "CSS",
                "Java",
                "Photoshop",
                "Java"
        ));
    }
}
