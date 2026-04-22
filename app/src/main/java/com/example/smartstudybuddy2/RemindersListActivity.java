package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

/**
 * Reminders List Activity
 * Displays all reminders with filtering by Today/Upcoming/All
 */
public class RemindersListActivity extends AppCompatActivity implements RemindersAdapter.OnReminderActionListener {

    private static final String TAG = "RemindersListActivity";
    private static final int ADD_EDIT_REMINDER_REQUEST = 101;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerReminders;
    private FloatingActionButton fabAddReminder;
    private LinearLayout emptyStateLayout;

    private RemindersAdapter adapter;
    private DatabaseHelper dbHelper;
    private ArrayList<ScheduleReminder> reminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_reminders_list);

            // Initialize views
            toolbar = findViewById(R.id.toolbar);
            tabLayout = findViewById(R.id.tabLayout);
            recyclerReminders = findViewById(R.id.recyclerReminders);
            fabAddReminder = findViewById(R.id.fabAddReminder);
            emptyStateLayout = findViewById(R.id.emptyStateLayout);

            // Validate views
            if (toolbar == null || recyclerReminders == null || fabAddReminder == null) {
                throw new RuntimeException("Failed to inflate layout - missing views");
            }

            // Setup toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            if (dbHelper == null) {
                throw new RuntimeException("Database helper is null");
            }

            // Setup RecyclerView
            recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
            reminders = new ArrayList<>();
            adapter = new RemindersAdapter(this, reminders, this);
            recyclerReminders.setAdapter(adapter);

            // Load reminders
            loadReminders(0);  // 0 = Today

            // Setup tab listener
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    loadReminders(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });

            // FAB click listener - Use startActivityForResult
            fabAddReminder.setOnClickListener(v -> {
                Intent intent = new Intent(RemindersListActivity.this, AddEditReminderActivity.class);
                startActivityForResult(intent, ADD_EDIT_REMINDER_REQUEST);
            });

            Log.d(TAG, "✅ RemindersListActivity initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reminders screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Reload reminders when coming back from editing
            Log.d(TAG, "onResume - reloading reminders");
            loadReminders(tabLayout.getSelectedTabPosition());
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
            Toast.makeText(this, "Error reloading reminders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == ADD_EDIT_REMINDER_REQUEST && resultCode == RESULT_OK) {
                Log.d(TAG, "✅ Reminder added/edited successfully, reloading list");
                loadReminders(0);  // Reload Today's reminders
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult: " + e.getMessage(), e);
        }
    }

    /**
     * Load reminders based on selected tab
     * @param tabPosition 0=Today, 1=Upcoming, 2=All
     */
    private void loadReminders(int tabPosition) {
        reminders.clear();

        try {
            ArrayList<ScheduleReminder> loadedReminders = null;

            if (tabPosition == 0) {
                // Today
                Log.d(TAG, "Loading Today's reminders...");
                loadedReminders = dbHelper.getTodaySchedules();
            } else if (tabPosition == 1) {
                // Upcoming
                Log.d(TAG, "Loading Upcoming reminders...");
                loadedReminders = dbHelper.getUpcomingSchedules();
            } else {
                // All
                Log.d(TAG, "Loading All reminders...");
                loadedReminders = dbHelper.getAllScheduleReminders();
            }

            // Handle null result
            if (loadedReminders != null && !loadedReminders.isEmpty()) {
                reminders.addAll(loadedReminders);
                Log.d(TAG, "✅ Loaded " + reminders.size() + " reminders");
            } else {
                Log.d(TAG, "No reminders found for tab " + tabPosition);
                reminders.clear();
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException loading reminders: " + npe.getMessage(), npe);
            Toast.makeText(this, "Database error: Null pointer", Toast.LENGTH_SHORT).show();
            reminders.clear();
        } catch (Exception e) {
            Log.e(TAG, "Error loading reminders: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reminders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            reminders.clear();
        }

        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (reminders.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerReminders.setVisibility(View.GONE);
            Log.d(TAG, "Showing empty state");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerReminders.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing " + reminders.size() + " reminders");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ========== ADAPTER CALLBACKS ==========

    @Override
    public void onReminderDelete(int reminderId) {
        try {
            // Show confirmation dialog before deleting
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(RemindersListActivity.this, R.style.AppDialog)
                    .setTitle("Delete Reminder?")
                    .setMessage("Are you sure you want to delete this reminder?")
                    .setNegativeButton("Cancel", (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        Log.d(TAG, "Delete cancelled by user");
                    })
                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                        try {
                            Log.d(TAG, "Confirming delete of reminder: " + reminderId);
                            if (dbHelper.deleteScheduleReminder(reminderId)) {
                                adapter.removeReminder(reminderId);
                                reminders.removeIf(r -> r.getId() == reminderId);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(RemindersListActivity.this, "✅ Reminder deleted", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Reminder deleted successfully");

                                // Reload to update empty state
                                if (reminders.isEmpty()) {
                                    emptyStateLayout.setVisibility(View.VISIBLE);
                                    recyclerReminders.setVisibility(View.GONE);
                                }
                            }
                            dialogInterface.dismiss();
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting reminder: " + e.getMessage(), e);
                            Toast.makeText(RemindersListActivity.this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create();
            
            Log.d(TAG, "Showing delete confirmation dialog for reminder: " + reminderId);
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onReminderDelete: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderEdit(ScheduleReminder reminder) {
        try {
            Log.d(TAG, "Editing reminder: " + reminder.getId());
            Intent intent = new Intent(RemindersListActivity.this, AddEditReminderActivity.class);
            intent.putExtra("REMINDER_ID", reminder.getId());
            startActivityForResult(intent, ADD_EDIT_REMINDER_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error launching edit activity: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening reminder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderComplete(int reminderId, boolean isCompleted) {
        try {
            Log.d(TAG, "Marking reminder " + reminderId + " as completed: " + isCompleted);
            dbHelper.markReminderCompleted(reminderId);
            
            // Update reminder in list
            for (ScheduleReminder reminder : reminders) {
                if (reminder.getId() == reminderId) {
                    reminder.setIsCompleted(isCompleted);
                    adapter.updateReminder(reminder);
                    break;
                }
            }
            
            String message = isCompleted ? "Reminder completed! ✅" : "Reminder marked pending";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error updating reminder: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating reminder", Toast.LENGTH_SHORT).show();
        }
    }

}
