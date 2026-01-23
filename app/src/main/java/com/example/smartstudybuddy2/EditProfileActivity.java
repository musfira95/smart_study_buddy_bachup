package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etUniversity, etCourse,
            etSemester, etDailyGoal, etLanguage;
    Spinner spStudyPreference;
    LinearLayout btnSaveProfile;

    DatabaseHelper db;   // ✅ DatabaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
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
        Cursor c = db.getProfile();
        if (c != null && c.moveToFirst()) {
            etFullName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            etEmail.setText(c.getString(c.getColumnIndexOrThrow("email")));
            etUniversity.setText(c.getString(c.getColumnIndexOrThrow("university")));
            etCourse.setText(c.getString(c.getColumnIndexOrThrow("course")));
            etSemester.setText(c.getString(c.getColumnIndexOrThrow("semester")));
            etDailyGoal.setText(c.getString(c.getColumnIndexOrThrow("daily_goal")));
            etLanguage.setText(c.getString(c.getColumnIndexOrThrow("language")));
        }
        if (c != null) c.close();
    }

    private void saveProfile() {
        db.saveOrUpdateProfile(
                etFullName.getText().toString(),
                etEmail.getText().toString(),
                "", // phone future
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
}
