package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class ThemeSettingsActivity extends AppCompatActivity {

    Switch switchNotifications;
    LinearLayout aboutLayout, helpLayout;

    SharedPreferences appPrefs;
    SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌗 Apply saved THEME before UI
        themePrefs = getSharedPreferences("ADMIN_THEME", MODE_PRIVATE);
        boolean darkMode = themePrefs.getBoolean("DARK_MODE", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        // INIT
        switchNotifications = findViewById(R.id.switchNotifications);
        aboutLayout = findViewById(R.id.aboutLayout);
        helpLayout = findViewById(R.id.helpLayout);

        appPrefs = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE);

        // 🔔 Load notification state
        boolean notifEnabled = appPrefs.getBoolean("NOTIFICATIONS_ENABLED", true);
        switchNotifications.setChecked(notifEnabled);

        // 🔔 Notification toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPrefs.edit()
                    .putBoolean("NOTIFICATIONS_ENABLED", isChecked)
                    .apply();

            Toast.makeText(
                    this,
                    isChecked ? "Notifications Enabled" : "Notifications Disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ℹ️ About App
        aboutLayout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class))
        );

        // 🆘 Help & Support
        helpLayout.setOnClickListener(v ->
                startActivity(new Intent(this, HelpActivity.class))
        );
    }
}
