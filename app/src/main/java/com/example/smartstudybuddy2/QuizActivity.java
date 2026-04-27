package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.smartstudybuddy2.network.LocalApiClient;
import com.example.smartstudybuddy2.network.ApiService;
import com.example.smartstudybuddy2.network.QuizRequest;
import com.example.smartstudybuddy2.network.QuizResponse;

import java.util.ArrayList;

import com.example.smartstudybuddy2.Recording;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    long quizStartTime = 0;  // ✅ Track quiz start time
    long recordingId = -1;    // ✅ Store recording ID for saving quiz

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

        // ✅ Record quiz start time
        quizStartTime = System.currentTimeMillis();

        // ✅ GET RECORDING ID from intent (if available for saving)
        recordingId = getIntent().getLongExtra("recordingId", -1);
        Log.d("QuizActivity", "📝 Recording ID: " + recordingId);

        // ✅ NEW: Check if transcription/summary passed from SummaryActivity
        String transcription = getIntent().getStringExtra("transcription");
        String summaryText = getIntent().getStringExtra("summaryText");

        // If we received transcription/summary, generate dynamic quiz
        if (transcription != null && !transcription.isEmpty()) {
            Toast.makeText(this, "Generating Quiz from your lecture...", Toast.LENGTH_SHORT).show();
            generateDynamicQuiz(transcription);
            // ✅ showQuestion() will be called inside the callback after API response
        } else {
            // Fallback: Load questions from database
            loadQuestionsFromDatabase();

            if (questions.isEmpty()) {
                Toast.makeText(this, "No quiz questions available. Loading defaults...", Toast.LENGTH_SHORT).show();
                loadDummyQuestions();
            }
            
            // Show first question from database/dummy data
            if (!questions.isEmpty()) {
                showQuestion();
            }
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
            
            Log.d("QuizActivity", "📍 Question " + currentIndex + " of " + questions.size());

            if (currentIndex < questions.size()) {
                Log.d("QuizActivity", "✅ Showing question " + (currentIndex + 1));
                showQuestion();
            } else {
                Log.d("QuizActivity", "✅ All questions answered. Showing Submit button");
                btnNext.setVisibility(LinearLayout.GONE);
                btnSubmit.setVisibility(LinearLayout.VISIBLE);
            }
        });

        // -------------------- Submit Quiz --------------------
        btnSubmit.setOnClickListener(v -> {
            // ✅ Calculate quiz duration in seconds
            long quizEndTime = System.currentTimeMillis();
            int durationSeconds = (int) ((quizEndTime - quizStartTime) / 1000);
            
            Log.d("QuizActivity", "📊 QUIZ FINISHED!");
            Log.d("QuizActivity", "📊 Total Questions: " + questions.size());
            Log.d("QuizActivity", "✅ Correct: " + correct);
            Log.d("QuizActivity", "❌ Wrong: " + wrong);
            Log.d("QuizActivity", "⏱️ Duration: " + durationSeconds + "s");
            
            Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
            intent.putExtra("correctCount", correct);
            intent.putExtra("wrongCount", wrong);
            intent.putExtra("durationSeconds", durationSeconds);

            // Use recording title as category if available, else "General"
            String category = "General";
            if (recordingId != -1) {
                try {
                    Recording rec = dbHelper.getRecordingById((int) recordingId);
                    if (rec != null && rec.getTitle() != null && !rec.getTitle().isEmpty()) {
                        category = rec.getTitle();
                    }
                } catch (Exception ignored) {}
            }
            intent.putExtra("category", category);
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
            Log.d("QuizActivity", "🎯 Calling LOCAL FastAPI /quiz/ endpoint...");
            Log.d("QuizActivity", "📝 Transcription length: " + (transcription != null ? transcription.length() : 0) + " chars");
            Log.d("QuizActivity", "🔗 Using LocalApiClient (local backend at 192.168.100.96:8000)");
            
            // ✅ USE LOCAL API CLIENT FOR QUIZ (not ngrok transcription API)
            ApiService apiService = LocalApiClient.getClient().create(ApiService.class);
            QuizRequest request = new QuizRequest(transcription, 4, (int)recordingId);  // 4 questions
            Log.d("QuizActivity", "💾 Quiz request created - num_questions: 4, recording_id=" + recordingId);
            Log.d("QuizActivity", "📤 Sending request to LOCAL /quiz/ endpoint...");
            
            apiService.generateQuiz(request).enqueue(new Callback<QuizResponse>() {
                @Override
                public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                    Log.d("QuizActivity", "📨 API Response received - Status Code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("QuizActivity", "✅ Quiz API SUCCESS! Returned: " + response.body().getQuestions().size() + " questions");
                        Log.d("QuizActivity", "📊 First question preview: " + (response.body().getQuestions().size() > 0 ? response.body().getQuestions().get(0).question.substring(0, Math.min(50, response.body().getQuestions().get(0).question.length())) : "No questions"));
                        
                        // Convert API response to QuizQuestion objects
                        questions.clear();
                        for (QuizResponse.QuizQuestionItem apiQuestion : response.body().getQuestions()) {
                            if (apiQuestion.options != null && apiQuestion.options.size() >= 4) {
                                String correctAnswer = "";
                                String[] optionArray = new String[4];
                                
                                // Extract options and find correct answer
                                for (int i = 0; i < Math.min(4, apiQuestion.options.size()); i++) {
                                    optionArray[i] = apiQuestion.options.get(i).option;
                                    if (apiQuestion.options.get(i).isCorrect()) {
                                        correctAnswer = optionArray[i];
                                    }
                                }
                                
                                // Ensure correct answer was found
                                if (correctAnswer.isEmpty() && optionArray.length > 0) {
                                    correctAnswer = optionArray[0];  // Fallback to first option
                                }
                                
                                questions.add(new QuizQuestion(
                                    apiQuestion.question,
                                    optionArray[0],
                                    optionArray[1],
                                    optionArray[2],
                                    optionArray[3],
                                    correctAnswer
                                ));
                            }
                        }
                        
                        if (questions.isEmpty()) {
                            Log.e("QuizActivity", "❌ No valid questions received!");
                            loadDummyQuestions();
                            if (!questions.isEmpty()) {
                                showQuestion();
                            }
                        } else {
                            Log.d("QuizActivity", "✅ Generated " + questions.size() + " questions from API!");
                            Toast.makeText(QuizActivity.this, "✅ Generated " + questions.size() + " questions!", Toast.LENGTH_SHORT).show();
                            Log.d("QuizActivity", "📊 Quiz loaded: " + questions.size() + " total | Current Index: " + currentIndex);
                            
                            // ✅ SAVE QUIZ TO DATABASE
                            saveQuizToDatabase();
                            
                            showQuestion();
                        }
                    } else {
                        Log.e("QuizActivity", "❌ API ERROR Response!");
                        Log.e("QuizActivity", "   Status Code: " + response.code());
                        Log.e("QuizActivity", "   Message: " + response.message());
                        try {
                            if (response.errorBody() != null) {
                                String errorBodyStr = response.errorBody().string();
                                Log.e("QuizActivity", "   Error Body: " + errorBodyStr);
                            }
                        } catch (Exception e) {
                            Log.e("QuizActivity", "   Could not read error body: " + e.getMessage());
                        }
                        Log.e("QuizActivity", "❌ Quiz API error - falling back to dummy quiz");
                        loadDummyQuestions();
                        if (!questions.isEmpty()) {
                            showQuestion();
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<QuizResponse> call, Throwable t) {
                    Log.e("QuizActivity", "❌ API CALL FAILED - Network/Connection Error");
                    Log.e("QuizActivity", "   Error Type: " + t.getClass().getSimpleName());
                    Log.e("QuizActivity", "   Error Message: " + t.getMessage());
                    Log.e("QuizActivity", "   Cause: " + (t.getCause() != null ? t.getCause().getMessage() : "Unknown"));
                    t.printStackTrace();
                    Log.e("QuizActivity", "❌ Network error - falling back to dummy quiz");
                    loadDummyQuestions();
                    if (!questions.isEmpty()) {
                        showQuestion();
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e("QuizActivity", "❌ EXCEPTION in generateDynamicQuiz!");
            Log.e("QuizActivity", "   Error: " + e.getMessage());
            Log.e("QuizActivity", "   Class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            Log.e("QuizActivity", "❌ Exception - falling back to dummy quiz");
            loadDummyQuestions();
            if (!questions.isEmpty()) {
                showQuestion();
            }
        }
    }

    // -------------------- Display current question --------------------
    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            Log.e("QuizActivity", "❌ INVALID INDEX: currentIndex=" + currentIndex + ", total=" + questions.size());
            return;
        }
        
        QuizQuestion q = questions.get(currentIndex);
        tvQuestion.setText(q.getQuestion());
        optionA.setText(q.getOptionA());
        optionB.setText(q.getOptionB());
        optionC.setText(q.getOptionC());
        optionD.setText(q.getOptionD());
        
        Log.d("QuizActivity", "📝 Q" + (currentIndex + 1) + ": " + q.getQuestion().substring(0, Math.min(50, q.getQuestion().length())));
    }

    // -------------------- Check selected answer --------------------
    private void checkAnswer() {
        QuizQuestion q = questions.get(currentIndex);
        RadioButton selected = findViewById(rgOptions.getCheckedRadioButtonId());
        String selectedText = selected.getText().toString();
        
        boolean isCorrect = selectedText.equals(q.getAnswer());
        if (isCorrect) {
            correct++;
            Log.d("QuizActivity", "✅ Q" + (currentIndex + 1) + " CORRECT (Total: " + correct + ")");
        } else {
            wrong++;
            Log.d("QuizActivity", "❌ Q" + (currentIndex + 1) + " WRONG (Total: " + wrong + ")");
        }
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

    // ✅ NEW: Save quiz to database
    private void saveQuizToDatabase() {
        if (recordingId <= 0 || questions.isEmpty()) {
            Log.w("QuizActivity", "⚠️ Cannot save quiz - recordingId=" + recordingId + ", questions=" + questions.size());
            return;
        }

        try {
            // Build JSON from questions (same format as ProcessAudioActivity)
            StringBuilder quizJson = new StringBuilder("[");
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion q = questions.get(i);
                quizJson.append("{")
                    .append("\"question\":\"").append(q.getQuestion().replace("\"", "\\\"")).append("\",")
                    .append("\"options\":[")
                    .append("{\"option\":\"").append(q.getOptionA().replace("\"", "\\\"")).append("\"},")
                    .append("{\"option\":\"").append(q.getOptionB().replace("\"", "\\\"")).append("\"},")
                    .append("{\"option\":\"").append(q.getOptionC().replace("\"", "\\\"")).append("\"},")
                    .append("{\"option\":\"").append(q.getOptionD().replace("\"", "\\\"")).append("\"}")
                    .append("]")
                    .append("}");
                if (i < questions.size() - 1) quizJson.append(",");
            }
            quizJson.append("]");

            // Save to database
            boolean saved = dbHelper.updateRecordingQuiz((int) recordingId, quizJson.toString());
            Log.d("QuizActivity", "📝 Quiz saved to database: " + saved + " (recordingId=" + recordingId + ", questions=" + questions.size() + ")");
        } catch (Exception e) {
            Log.e("QuizActivity", "❌ Error saving quiz: " + e.getMessage(), e);
        }
    }
}
