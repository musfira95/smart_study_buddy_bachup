package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone, etUniversity, etCourse,
            etSemester, etDailyGoal, etLanguage;
    Spinner spStudyPreference;
    LinearLayout btnSaveProfile;

    DatabaseHelper db;   // ✅ DatabaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etUniversity = findViewById(R.id.etUniversity);
        etCourse = findViewById(R.id.etCourse);
        etSemester = findViewById(R.id.etSemester);
        etDailyGoal = findViewById(R.id.etDailyGoal);
        etLanguage = findViewById(R.id.etLanguage);
        spStudyPreference = findViewById(R.id.spStudyPreference);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadProfileData();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadProfileData() {
        try {
            Cursor c = db.getProfile();
            if (c != null && c.moveToFirst()) {
                try { etFullName.setText(c.getString(c.getColumnIndexOrThrow("name"))); } catch (Exception ignored) {}
                try { etEmail.setText(c.getString(c.getColumnIndexOrThrow("email"))); } catch (Exception ignored) {}
                try { etPhone.setText(c.getString(c.getColumnIndexOrThrow("phone"))); } catch (Exception ignored) {}
                try { etUniversity.setText(c.getString(c.getColumnIndexOrThrow("university"))); } catch (Exception ignored) {}
                try { etCourse.setText(c.getString(c.getColumnIndexOrThrow("course"))); } catch (Exception ignored) {}
                try { etSemester.setText(c.getString(c.getColumnIndexOrThrow("semester"))); } catch (Exception ignored) {}
                try { etDailyGoal.setText(c.getString(c.getColumnIndexOrThrow("daily_goal"))); } catch (Exception ignored) {}
                try { etLanguage.setText(c.getString(c.getColumnIndexOrThrow("language"))); } catch (Exception ignored) {}
            }
            if (c != null) c.close();
        } catch (Exception e) {
            android.util.Log.e("EditProfile", "loadProfileData error: " + e.getMessage());
        }
    }

    private void saveProfile() {
        db.saveOrUpdateProfile(
                etFullName.getText().toString(),
                etEmail.getText().toString(),
                etPhone.getText().toString(),
                etUniversity.getText().toString(),
                etCourse.getText().toString(),
                etSemester.getText().toString(),
                spStudyPreference.getSelectedItem().toString(),
                etDailyGoal.getText().toString(),
                etLanguage.getText().toString()
        );

        Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
