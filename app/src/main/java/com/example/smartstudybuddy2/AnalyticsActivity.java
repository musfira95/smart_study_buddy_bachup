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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Bind views
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvAvgStudy = findViewById(R.id.tvAvgStudy);
        tvTopSubject = findViewById(R.id.tvTopSubject);
        lineChart = findViewById(R.id.lineChart);

        // Sample data (replace with real data later)
        tvTotalSessions.setText("Total Sessions\n15");
        tvAvgStudy.setText("Avg Study\n2 hr");
        tvTopSubject.setText("Top Subject\nMathematics");

        // Sample chart data
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 1f));
        entries.add(new Entry(2, 2f));
        entries.add(new Entry(3, 1.5f));
        entries.add(new Entry(4, 2.5f));
        entries.add(new Entry(5, 3f));

        LineDataSet dataSet = new LineDataSet(entries, "Study Hours");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(getResources().getColor(R.color.purple_500));
        dataSet.setCircleColor(getResources().getColor(R.color.purple_500));
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh chart
    }
}
