package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // THEME MODE
        SharedPreferences prefs = getSharedPreferences("ADMIN_THEME", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("DARK_MODE", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        TextView welcome = findViewById(R.id.welcomeText);

        // CHANGE Button → LinearLayout
        LinearLayout goDashboard = findViewById(R.id.btnGoDashboard);

        goDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        });
    }
}
