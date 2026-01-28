package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvTotalSessions, tvAvgStudy, tvTopSubject;
    private LineChart lineChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Bind views
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvAvgStudy = findViewById(R.id.tvAvgStudy);
        tvTopSubject = findViewById(R.id.tvTopSubject);
        lineChart = findViewById(R.id.lineChart);

        dbHelper = new DatabaseHelper(this);

        // Load real data from database
        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
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
                int duration = Integer.parseInt(session.duration);
                totalDuration += duration;

                // Find top subject
                if (duration > maxSubjectDuration) {
                    maxSubjectDuration = duration;
                    topSubject = session.subject;
                }
            } catch (NumberFormatException e) {
                // Handle parsing error
            }
        }

        int avgStudy = sessions.isEmpty() ? 0 : totalDuration / sessions.size();
        tvAvgStudy.setText("Avg Study\n" + avgStudy + " min");
        tvTopSubject.setText("Top Subject\n" + topSubject);

        // Create chart data from sessions
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(sessions.size(), 10); i++) {
            try {
                int duration = Integer.parseInt(sessions.get(i).duration);
                entries.add(new Entry(i + 1, duration));
            } catch (NumberFormatException e) {
                entries.add(new Entry(i + 1, 0f));
            }
        }

        // If no data, show placeholder
        if (entries.isEmpty()) {
            entries.add(new Entry(1, 0f));
            entries.add(new Entry(2, 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Study Duration (minutes)");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(getResources().getColor(R.color.purple_500));
        dataSet.setCircleColor(getResources().getColor(R.color.purple_500));
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
