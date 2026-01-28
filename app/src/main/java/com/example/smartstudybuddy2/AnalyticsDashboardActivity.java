package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;


public class AnalyticsDashboardActivity extends AppCompatActivity {

    TextView totalUsers, activeUsers, summaryCount, blockedUsers;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("ADMIN_THEME", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("DARK_MODE", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_dashboard);

        totalUsers = findViewById(R.id.total_users);
        activeUsers = findViewById(R.id.active_users);
        summaryCount = findViewById(R.id.summary_count);
        blockedUsers = findViewById(R.id.blocked_users);

        dbHelper = new DatabaseHelper(this);

        // Load actual data from database
        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        // Total users from database
        int totalUsersCount = dbHelper.getTotalUsers();
        totalUsers.setText(String.valueOf(totalUsersCount));

        // Active users (approximate - users not blocked)
        int totalCount = dbHelper.getTotalUsers();
        int blockedCount = dbHelper.getBlockedUsersCount();
        int activeUsersCount = totalCount - blockedCount;
        activeUsers.setText(String.valueOf(activeUsersCount));

        // Total notes/transcriptions
        int summaryCountVal = dbHelper.getTotalNotes();
        summaryCount.setText(String.valueOf(summaryCountVal));

        // Blocked users
        int blockedUsersCount = dbHelper.getBlockedUsersCount();
        blockedUsers.setText(String.valueOf(blockedUsersCount));
    }
}