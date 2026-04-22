package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";

    private TextView tvTotalSessions, tvAvgStudy, tvTopSubject;
    private TextView tvTotalRecordings, tvAverageQuizScore, tvTotalQuizzes;  // ✅ New stats
    private LineChart lineChart;
    private DatabaseHelper dbHelper;
    private LinearLayout statsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Bind views
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvAvgStudy = findViewById(R.id.tvAvgStudy);
        tvTopSubject = findViewById(R.id.tvTopSubject);
        tvTotalRecordings = findViewById(R.id.tvTotalRecordings);
        tvAverageQuizScore = findViewById(R.id.tvAverageQuizScore);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        lineChart = findViewById(R.id.lineChart);

        dbHelper = new DatabaseHelper(this);

        // Load real data from database
        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        Log.d(TAG, "📊 Loading analytics data...");
        
        // ===== STUDY SESSIONS DATA =====
        ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();

        // Calculate total sessions
        int totalSessions = sessions.size();
        tvTotalSessions.setText("Total Sessions\n" + totalSessions);

        // Calculate average study time
        int totalDuration = 0;
        String topSubject = "Not available";
        int maxSubjectDuration = 0;

        for (StudySession session : sessions) {
            try {
                int duration = session.getDuration();
                totalDuration += duration;

                // Find top subject
                if (duration > maxSubjectDuration) {
                    maxSubjectDuration = duration;
                    topSubject = session.getTitle();
                }
            } catch (Exception e) {
                Log.w(TAG, "Error processing session: " + e.getMessage());
            }
        }

        int avgStudy = sessions.isEmpty() ? 0 : totalDuration / sessions.size();
        tvAvgStudy.setText("Avg Study\n" + avgStudy + " min");
        tvTopSubject.setText("Top Subject\n" + topSubject);

        // ===== RECORDING DATA =====
        ArrayList<Recording> recordings = dbHelper.getAllRecordings();
        int totalRecordings = recordings.size();
        tvTotalRecordings.setText("Total Recordings\n" + totalRecordings);
        Log.d(TAG, "✅ Total Recordings: " + totalRecordings);

        // ===== QUIZ DATA =====
        ArrayList<QuizResult> quizResults = dbHelper.getAllQuizResults();
        int totalQuizzes = quizResults.size();
        double avgQuizScore = dbHelper.getAverageQuizScore();
        
        tvTotalQuizzes.setText("Total Quizzes\n" + totalQuizzes);
        tvAverageQuizScore.setText("Avg Quiz Score\n" + String.format("%.1f", avgQuizScore) + "%");
        Log.d(TAG, "✅ Total Quizzes: " + totalQuizzes + " | Avg Score: " + String.format("%.1f", avgQuizScore) + "%");

        // ===== CHART DATA =====
        createChart(sessions, quizResults);

        Log.d(TAG, "✅ Analytics loaded successfully!");
    }

    /**
     * Create chart data from sessions and quiz results
     */
    private void createChart(ArrayList<StudySession> sessions, ArrayList<QuizResult> quizResults) {
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Use study sessions data for the chart
        for (int i = 0; i < Math.min(sessions.size(), 10); i++) {
            try {
                int duration = sessions.get(i).getDuration();
                entries.add(new Entry(i + 1, duration));
            } catch (Exception e) {
                entries.add(new Entry(i + 1, 0f));
            }
        }

        // If no session data, show quiz scores instead
        if (entries.isEmpty() && !quizResults.isEmpty()) {
            Log.d(TAG, "No sessions found, using quiz scores for chart");
            for (int i = 0; i < Math.min(quizResults.size(), 10); i++) {
                entries.add(new Entry(i + 1, (float) quizResults.get(i).getScorePercentage()));
            }
        }

        // If still no data, show placeholder
        if (entries.isEmpty()) {
            entries.add(new Entry(1, 0f));
            entries.add(new Entry(2, 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Study / Quiz Performance");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(getResources().getColor(R.color.purple_500));
        dataSet.setCircleColor(getResources().getColor(R.color.purple_500));
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
