package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class QuizManagementActivity extends AppCompatActivity {

    private EditText etQuestion, etOptionA, etOptionB, etOptionC, etOptionD, etAnswer;
    private Spinner spinnerCategory, spinnerDifficulty;
    private Button btnAddQuestion;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_management);

        // Initialize views (assuming layout exists)
        etQuestion = findViewById(R.id.etQuestion);
        etOptionA = findViewById(R.id.etOptionA);
        etOptionB = findViewById(R.id.etOptionB);
        etOptionC = findViewById(R.id.etOptionC);
        etOptionD = findViewById(R.id.etOptionD);
        etAnswer = findViewById(R.id.etAnswer);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnAddQuestion = findViewById(R.id.btnAddQuestion);

        dbHelper = new DatabaseHelper(this);

        btnAddQuestion.setOnClickListener(v -> addNewQuestion());
    }

    private void addNewQuestion() {
        String question = etQuestion.getText().toString().trim();
        String optionA = etOptionA.getText().toString().trim();
        String optionB = etOptionB.getText().toString().trim();
        String optionC = etOptionC.getText().toString().trim();
        String optionD = etOptionD.getText().toString().trim();
        String answer = etAnswer.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String difficulty = spinnerDifficulty.getSelectedItem().toString();

        // Validation
        if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
            optionC.isEmpty() || optionD.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert into database
        boolean success = dbHelper.insertQuizQuestion(question, optionA, optionB, optionC, optionD, answer, category, difficulty);

        if (success) {
            Toast.makeText(this, "Question added successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to add question", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etQuestion.setText("");
        etOptionA.setText("");
        etOptionB.setText("");
        etOptionC.setText("");
        etOptionD.setText("");
        etAnswer.setText("");
    }
}
