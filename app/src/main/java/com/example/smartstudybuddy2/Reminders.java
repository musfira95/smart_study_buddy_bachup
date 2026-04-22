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

public class Reminders extends AppCompatActivity implements ReminderAdapter.OnReminderActionListener {

    private static final String TAG = "Reminders";

    FloatingActionButton fabAdd;
    RecyclerView reminderRecycler;
    FrameLayout emptyStateLayout;
    ImageButton btnBack;

    ArrayList<ScheduleReminder> reminderList = new ArrayList<>();
    ReminderAdapter adapter;
    DatabaseHelper dbHelper;  // ✅ Add database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        fabAdd = findViewById(R.id.fabAdd);
        reminderRecycler = findViewById(R.id.reminderRecycler);
        emptyStateLayout = findViewById(R.id.emptystateLayout);
        btnBack = findViewById(R.id.btnBack);
        
        dbHelper = new DatabaseHelper(this);  // ✅ Initialize database helper

        reminderRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this);
        reminderRecycler.setAdapter(adapter);

        // ✅ Load reminders from database on startup
        loadRemindersFromDatabase();

        fabAdd.setOnClickListener(v -> openAddReminderPopup());
        
        // ✅ Back button click listener
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * ✅ Load reminders from database when activity opens
     */
    private void loadRemindersFromDatabase() {
        Log.d(TAG, "📅 Loading reminders from database...");
        reminderList.clear();
        
        ArrayList<ScheduleReminder> reminders = dbHelper.getAllScheduleReminders();
        if (reminders != null && !reminders.isEmpty()) {
            reminderList.addAll(reminders);
        }
        
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
        ImageButton btnClose = view.findViewById(R.id.btnCloseReminder);

        // ✅ Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (etTitle.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = etTitle.getText().toString();
            String time = String.format(Locale.getDefault(), "%02d:%02d", 
                                       timePicker.getHour(), timePicker.getMinute());
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Log.d(TAG, "Saving reminder: " + title + " at " + time + " on " + today);
             
            // ✅ Save to database instead of just memory
            long reminderId = dbHelper.insertScheduleWithFeature(title, "", today, time, null, -1);
            
            Log.d(TAG, "Database returned ID: " + reminderId);
            
            if (reminderId > 0) {
                Log.d(TAG, "✅ Reminder saved: " + title + " at " + time);
                
                // Reload from database
                loadRemindersFromDatabase();
                Toast.makeText(Reminders.this, "✅ Reminder saved!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "❌ Failed to save reminder to database. ID: " + reminderId);
                Toast.makeText(Reminders.this, "❌ Failed to save reminder", Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onReminderDelete(int reminderId) {
        try {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(Reminders.this, R.style.AppDialog)
                    .setTitle("Delete Reminder?")
                    .setMessage("Are you sure you want to delete this reminder?")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton("Delete", (dialog, which) -> {
                        try {
                            dbHelper.deleteScheduleReminder(reminderId);
                            loadRemindersFromDatabase();
                            Toast.makeText(Reminders.this, "✅ Reminder deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(Reminders.this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderEdit(ScheduleReminder reminder) {
        try {
            android.content.Intent intent = new android.content.Intent(Reminders.this, AddEditReminderActivity.class);
            intent.putExtra("REMINDER_ID", reminder.getId());
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderComplete(int reminderId, boolean isCompleted) {
        dbHelper.markReminderCompleted(reminderId);
        loadRemindersFromDatabase();
        Toast.makeText(this, isCompleted ? "Marked complete!" : "Marked pending", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d(TAG, "Activity result - reloading reminders");
            loadRemindersFromDatabase();
        }
    }
}
