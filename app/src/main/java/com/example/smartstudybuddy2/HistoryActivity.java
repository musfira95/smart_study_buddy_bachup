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
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);

        // Load data from database
        loadHistoryFromDatabase();

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

    // Load history from database
    private void loadHistoryFromDatabase() {
        // Load recordings
        ArrayList<Recording> recordings = dbHelper.getAllRecordings();
        historyList.addAll(recordings);

        // Load study sessions
        ArrayList<StudySession> sessions = dbHelper.getAllStudySessions();
        historyList.addAll(sessions);
    }
}
