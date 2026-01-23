package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvUniversity, tvCourse;
    ImageView btnEditProfile;
    LinearLayout logoutBtn;

    DatabaseHelper db;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // INIT
        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvUniversity = findViewById(R.id.tvUniversity);
        tvCourse = findViewById(R.id.tvCourse);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        logoutBtn = findViewById(R.id.logoutBtn); // ✅ logout button

        // Edit profile click
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        // Logout click
        logoutBtn.setOnClickListener(v -> {
            sessionManager.logoutUser();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadProfile();   // first time load
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();   // edit ke baad refresh
    }

    private void loadProfile() {

        String loginEmail = sessionManager.getUserEmail();
        if (loginEmail == null) return;

        Cursor c = db.getProfileByEmail(loginEmail);

        if (c != null && c.moveToFirst()) {
            tvName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            tvEmail.setText(c.getString(c.getColumnIndexOrThrow("email")));
            tvUniversity.setText(c.getString(c.getColumnIndexOrThrow("university")));
            tvCourse.setText(c.getString(c.getColumnIndexOrThrow("course")));
        }

        if (c != null) c.close();
    }
}
