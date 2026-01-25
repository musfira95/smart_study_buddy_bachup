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

public class QuizActivity extends AppCompatActivity {

    TextView tvQuestion;
    RadioGroup rgOptions;
    RadioButton optionA, optionB, optionC, optionD;
    LinearLayout btnNext, btnSubmit, btnSkip; // <-- Added btnSkip

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
        btnSkip = findViewById(R.id.btnSkip);

        loadDummyQuestions();
        showQuestion();

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

        // -------------------- Skip Quiz --------------------
        btnSkip.setOnClickListener(v -> {
            Toast.makeText(this, "Quiz Skipped", Toast.LENGTH_SHORT).show();
            finish();
        });
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

    // -------------------- Dummy questions --------------------
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
