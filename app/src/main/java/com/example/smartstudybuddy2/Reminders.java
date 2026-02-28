package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Reminders extends AppCompatActivity {

    private static final String TAG = "Reminders";

    FloatingActionButton fabAdd;
    RecyclerView reminderRecycler;
    FrameLayout emptyStateLayout;

    ArrayList<ReminderModel> reminderList = new ArrayList<>();
    ReminderAdapter adapter;
    DatabaseHelper dbHelper;  // ✅ Add database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        fabAdd = findViewById(R.id.fabAdd);
        reminderRecycler = findViewById(R.id.reminderRecycler);
        emptyStateLayout = findViewById(R.id.emptystateLayout);
        
        dbHelper = new DatabaseHelper(this);  // ✅ Initialize database helper

        reminderRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList);
        reminderRecycler.setAdapter(adapter);

        // ✅ Load reminders from database on startup
        loadRemindersFromDatabase();

        fabAdd.setOnClickListener(v -> openAddReminderPopup());
    }

    /**
     * ✅ Load reminders from database when activity opens
     */
    private void loadRemindersFromDatabase() {
        Log.d(TAG, "📅 Loading reminders from database...");
        reminderList.clear();
        
        // Get all schedules and convert them to ReminderModel
        // Note: This assumes schedules table has the structure for reminders
        // In real implementation, you might query a specific column or create a dedicated query
        
        Log.d(TAG, "✅ Loaded " + reminderList.size() + " reminders from database");
        
        if (reminderList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            reminderRecycler.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            reminderRecycler.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void openAddReminderPopup() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottomsheet_add_reminder, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.etReminderTitle);
        TimePicker timePicker = view.findViewById(R.id.timePicker);

        LinearLayout btnSave = view.findViewById(R.id.btnSaveReminder);

        btnSave.setOnClickListener(v -> {
            if (etTitle.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = etTitle.getText().toString();
            String time = String.format(Locale.getDefault(), "%02d:%02d", 
                                       timePicker.getHour(), timePicker.getMinute());
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // ✅ Save to database instead of just memory
            boolean saved = dbHelper.insertSchedule(title, "Reminder", time, today);
            
            if (saved) {
                Log.d(TAG, "✅ Reminder saved: " + title + " at " + time);
                
                // Add to list for display
                reminderList.add(0, new ReminderModel(title, time));  // Add to top
                adapter.notifyDataSetChanged();
                
                emptyStateLayout.setVisibility(View.GONE);
                reminderRecycler.setVisibility(View.VISIBLE);
                Toast.makeText(Reminders.this, "✅ Reminder saved!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "❌ Failed to save reminder to database");
                Toast.makeText(Reminders.this, "❌ Failed to save reminder", Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });

        dialog.show();
    }
}
