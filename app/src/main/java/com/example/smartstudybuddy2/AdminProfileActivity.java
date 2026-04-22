package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class AdminProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvRole;
    ImageView btnEditProfile, btnBack, ivAdminAvatar;
    Button logoutBtn;

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
        btnBack = findViewById(R.id.btnBack);
        ivAdminAvatar = findViewById(R.id.ivAdminAvatar);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Edit profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(AdminProfileActivity.this, EditAdminProfileActivity.class))
            );
        }

        // Logout
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                sessionManager.logoutUser();
                Intent intent = new Intent(AdminProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile(); // refresh after edit
        loadProfilePhoto();
    }

    private void loadProfilePhoto() {
        try {
            SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
            String uriStr = prefs.getString("admin_profile_pic_uri", null);
            if (uriStr != null && ivAdminAvatar != null) {
                Uri uri = Uri.parse(uriStr);
                InputStream stream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (bitmap != null) {
                    ivAdminAvatar.setPadding(0, 0, 0, 0);
                    ivAdminAvatar.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        try {
            String loginEmail = sessionManager.getUserEmail();
            if (loginEmail == null) return;

            // Try profile table first
            Cursor c = db.getProfileByEmail(loginEmail);
            if (c != null && c.moveToFirst()) {
                int nameIdx = c.getColumnIndex("name");
                int emailIdx = c.getColumnIndex("email");
                if (nameIdx >= 0 && tvName != null) tvName.setText(c.getString(nameIdx));
                if (emailIdx >= 0 && tvEmail != null) tvEmail.setText(c.getString(emailIdx));
                c.close();
            } else {
                // Fallback: load from users table
                String username = db.getUsernameByEmail(loginEmail);
                if (tvName != null) tvName.setText(username != null && !username.isEmpty() ? username : "Admin");
                if (tvEmail != null) tvEmail.setText(loginEmail);
                if (c != null) c.close();
            }
            if (tvRole != null) tvRole.setText("Admin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
