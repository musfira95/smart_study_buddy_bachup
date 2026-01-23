package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ExportActivity extends AppCompatActivity {

    LinearLayout btnExportPDF, btnExportTXT;

    // ✅ Bottom Navigation Icons (ADDED ONLY)
    ImageView navHome, navAnalytics, navSettings, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // EXISTING BUTTONS (UNCHANGED)
        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportTXT = findViewById(R.id.btnExportTXT);

        btnExportPDF.setOnClickListener(v ->
                Toast.makeText(this, "PDF exported (dummy)", Toast.LENGTH_SHORT).show()
        );

        btnExportTXT.setOnClickListener(v ->
                Toast.makeText(this, "Text file exported (dummy)", Toast.LENGTH_SHORT).show()
        );

        // ✅ FIND BOTTOM NAV (ADDED)
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        // ✅ BOTTOM NAV ACTIONS (ADDED ONLY)
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        navAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSettingsActivity.class))
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }
}
