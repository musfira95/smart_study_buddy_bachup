package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Recording> recordingList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recordingListView);
        dbHelper = new DatabaseHelper(this);
        dbHelper.insertDummyRecordings(); // testing only
        loadRecordings(); // call after inserting dummy data
    }



    private void loadRecordings() {
        recordingList = dbHelper.getAllRecordings();
        if (recordingList == null) recordingList = new ArrayList<>();

        if (recordingList.isEmpty()) {
            Toast.makeText(this, "No recordings found!", Toast.LENGTH_SHORT).show();
        }

        // ✔ FIXED – no listener, ONLY 2 PARAMETERS
        RecordingListAdapter adapter = new RecordingListAdapter(this, recordingList);
        listView.setAdapter(adapter);

        // ✔ Clicking an item opens detail screen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Recording selectedRecording = recordingList.get(position);

            Intent intent = new Intent(RecordingListActivity.this, RecordingDetailActivity.class);
            intent.putExtra("recording_id", selectedRecording.getId());
            startActivity(intent);
        });


    }
}
