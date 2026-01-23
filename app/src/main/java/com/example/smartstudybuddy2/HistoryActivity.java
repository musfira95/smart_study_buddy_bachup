package com.example.smartstudybuddy2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private ArrayList<Object> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();

        // ===== DUMMY DATA (AS PER YOUR EXISTING MODELS) =====

        historyList.add(new Recording(
                1,
                "Lecture 1",
                "/path/lecture1.mp3",
                "07 Dec 2025",
                15
        ));

        historyList.add(new Recording(
                2,
                "Lecture 2",
                "/path/lecture2.mp3",
                "06 Dec 2025",
                20
        ));

        historyList.add(new StudySession(
                1,
                "Math",
                "30",
                "08 Dec 2025"
        ));

        historyList.add(new StudySession(
                2,
                "Physics",
                "45",
                "05 Dec 2025"
        ));

        // ===================================================

        // Sort: latest date first
        Collections.sort(historyList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {

                String date1 = "";
                String date2 = "";

                if (o1 instanceof Recording) {
                    date1 = ((Recording) o1).getDate();
                } else if (o1 instanceof StudySession) {
                    date1 = ((StudySession) o1).date;
                }

                if (o2 instanceof Recording) {
                    date2 = ((Recording) o2).getDate();
                } else if (o2 instanceof StudySession) {
                    date2 = ((StudySession) o2).date;
                }

                return date2.compareTo(date1); // descending
            }
        });

        adapter = new HistoryAdapter(this, historyList);
        rvHistory.setAdapter(adapter);
    }
}
