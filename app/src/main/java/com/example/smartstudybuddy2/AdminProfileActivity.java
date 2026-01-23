package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdminProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvRole;
    ImageView btnEditProfile;
    LinearLayout logoutBtn;

    DatabaseHelper db;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // INIT
        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tvName = findViewById(R.id.tvAdminName);
        tvEmail = findViewById(R.id.tvAdminEmail);
        tvRole = findViewById(R.id.tvAdminRole);
        btnEditProfile = findViewById(R.id.btnEditAdminProfile);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Edit profile
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AdminProfileActivity.this, EditAdminProfileActivity.class))
        );

        // Logout
        logoutBtn.setOnClickListener(v -> {
            sessionManager.logoutUser();

            Intent intent = new Intent(AdminProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile(); // refresh after edit
    }

    private void loadProfile() {

        String loginEmail = sessionManager.getUserEmail();
        if (loginEmail == null) return;

        // admin ka data
        Cursor c = db.getProfileByEmail(loginEmail);

        if (c != null && c.moveToFirst()) {
            tvName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            tvEmail.setText(c.getString(c.getColumnIndexOrThrow("email")));
            tvRole.setText("Admin");
        }

        if (c != null) c.close();
    }
}
