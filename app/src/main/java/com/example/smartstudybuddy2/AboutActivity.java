package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvAboutDescription, tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        tvAboutDescription = findViewById(R.id.tvAboutDescription);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        // Load about content from database
        String description = dbHelper.getAboutAppDescription();
        String version = dbHelper.getAboutAppVersion();

        tvAboutDescription.setText(description);
        tvAppVersion.setText("App Version: " + version);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
