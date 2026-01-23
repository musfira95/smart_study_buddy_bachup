package com.example.smartstudybuddy2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AllTranscriptionsActivity extends AppCompatActivity {

    private RecyclerView rvTranscriptions;
    private DatabaseHelper db;
    private ArrayList<Recording> allTranscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transcriptions);

        rvTranscriptions = findViewById(R.id.rvTranscriptions);
        db = new DatabaseHelper(this);

        allTranscriptions = db.getAllRecordings(); // DBHelper se fetch

        TranscriptionAdapter adapter = new TranscriptionAdapter(this, allTranscriptions, recording -> {
            // Item click: yahan pe tu chahe to detail screen khol sakti ho
        });

        rvTranscriptions.setLayoutManager(new LinearLayoutManager(this));
        rvTranscriptions.setAdapter(adapter);
    }
}
