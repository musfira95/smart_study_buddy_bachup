package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;



public class AnalyticsDashboardActivity extends AppCompatActivity {

    TextView totalUsers, activeUsers, summaryCount, blockedUsers;

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

        // Dummy Data (replace with real values if backend added)
        totalUsers.setText("50");
        activeUsers.setText("35");
        summaryCount.setText("90");
        blockedUsers.setText("5"); // ⭐ NEW BLOCKED USERS COUNT
    }
}