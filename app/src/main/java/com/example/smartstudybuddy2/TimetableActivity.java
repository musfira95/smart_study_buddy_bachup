package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimetableActivity extends AppCompatActivity {

    LinearLayout btnAddReminder, btnViewCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // LinearLayouts as buttons
        btnAddReminder = findViewById(R.id.btnAddReminder);
        btnViewCalendar = findViewById(R.id.btnViewCalendar);

        // 🔗 OPEN REMINDERS SCREEN
        btnAddReminder.setOnClickListener(v -> {
            Intent intent = new Intent(TimetableActivity.this, RemindersListActivity.class);
            startActivity(intent);
        });

        btnViewCalendar.setOnClickListener(v ->
                Toast.makeText(this, "Calendar view (dummy)", Toast.LENGTH_SHORT).show()
        );
    }
}
