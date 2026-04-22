package com.example.smartstudybuddy2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvUniversity, tvCourse;
    TextView tvStatRecordings, tvStatQuizzes, tvStatFlashcards, tvStatAvgScore;
    ImageView ivProfilePic, btnBackProfile;
    LinearLayout logoutBtn, btnChangePassword, btnEditProfile;

    DatabaseHelper db;
    SessionManager sessionManager;

    // Gallery picker
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Bind views
        tvName         = findViewById(R.id.tvName);
        tvEmail        = findViewById(R.id.tvEmail);
        tvPhone        = findViewById(R.id.tvPhone);
        tvUniversity   = findViewById(R.id.tvUniversity);
        tvCourse       = findViewById(R.id.tvCourse);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        logoutBtn      = findViewById(R.id.logoutBtn);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        ivProfilePic   = findViewById(R.id.ivProfilePic);
        btnBackProfile = findViewById(R.id.btnBackProfile);

        // Back arrow
        btnBackProfile.setOnClickListener(v -> finish());

        // Make profile picture perfectly round
        ivProfilePic.post(() -> {
            int size = Math.min(ivProfilePic.getWidth(), ivProfilePic.getHeight());
            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(0xFFFFFFFF);
            ivProfilePic.setBackground(circle);
            ivProfilePic.setClipToOutline(true);
        });

        tvStatRecordings = findViewById(R.id.tvStatRecordings);
        tvStatQuizzes    = findViewById(R.id.tvStatQuizzes);
        tvStatFlashcards = findViewById(R.id.tvStatFlashcards);
        tvStatAvgScore   = findViewById(R.id.tvStatAvgScore);

        // Gallery launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Persist permission so image stays after restart
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}

                        ivProfilePic.setImageURI(uri);
                        ivProfilePic.setPadding(0, 0, 0, 0);
                        // Save URI to SharedPreferences
                        getSharedPreferences("profile_prefs", MODE_PRIVATE)
                                .edit().putString("profile_pic_uri", uri.toString()).apply();
                    }
                });

        // Profile pic click → open gallery
        ivProfilePic.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // Edit profile → inline name edit dialog
        btnEditProfile.setOnClickListener(v -> showEditNameDialog());

        // Change Password
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Logout
        logoutBtn.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadProfile();
        loadStats();
        loadProfilePic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadStats();
    }

    private void loadProfile() {
        try {
            String loginEmail = sessionManager.getUserEmail();
            if (loginEmail == null) return;

            Cursor c = db.getProfileByEmail(loginEmail);
            if (c != null && c.moveToFirst()) {
                try { tvName.setText(c.getString(c.getColumnIndexOrThrow("name"))); } catch (Exception ignored) {}
                try { tvEmail.setText(c.getString(c.getColumnIndexOrThrow("email"))); } catch (Exception e) { tvEmail.setText(loginEmail); }
                try {
                    String phone = c.getString(c.getColumnIndexOrThrow("phone"));
                    tvPhone.setText((phone != null && !phone.isEmpty()) ? phone : "Not set");
                } catch (Exception ignored) {}
                try {
                    String uni = c.getString(c.getColumnIndexOrThrow("university"));
                    tvUniversity.setText((uni != null && !uni.isEmpty()) ? uni : "Not set");
                } catch (Exception ignored) {}
                try {
                    String course = c.getString(c.getColumnIndexOrThrow("course"));
                    tvCourse.setText((course != null && !course.isEmpty()) ? course : "Not set");
                } catch (Exception ignored) {}
            } else {
                tvEmail.setText(loginEmail);
            }
            if (c != null) c.close();
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "loadProfile error: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            tvStatRecordings.setText(String.valueOf(db.getTotalAudios()));
            tvStatQuizzes.setText(String.valueOf(db.getQuizResultsCount()));
            tvStatFlashcards.setText(String.valueOf(db.getAllFlashcardsAsArray().size()));
            double avg = db.getAverageQuizScore();
            tvStatAvgScore.setText(avg > 0 ? String.format("%.1f%%", avg) : "N/A");
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "loadStats error: " + e.getMessage());
        }
    }

    private void loadProfilePic() {
        try {
            SharedPreferences prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE);
            String uriStr = prefs.getString("profile_pic_uri", null);
            if (uriStr != null) {
                Uri uri = Uri.parse(uriStr);
                ivProfilePic.setImageURI(uri);
                ivProfilePic.setPadding(0, 0, 0, 0);
            }
        } catch (Exception ignored) {}
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppDialog);
        builder.setTitle("Edit Name");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText etName = new EditText(this);
        etName.setHint("Full Name");
        etName.setHintTextColor(0xFF888888);
        etName.setTextColor(0xFF222222);
        etName.setText(tvName.getText().toString());
        layout.addView(etName);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String email = sessionManager.getUserEmail();
                db.updateProfileName(email, newName);
                tvName.setText(newName);
                Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppDialog);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 10);

        EditText etCurrent = new EditText(this);
        etCurrent.setHint("Current Password");
        etCurrent.setHintTextColor(0xFF888888);
        etCurrent.setTextColor(0xFF222222);
        etCurrent.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrent);

        EditText etNew = new EditText(this);
        etNew.setHint("New Password");
        etNew.setHintTextColor(0xFF888888);
        etNew.setTextColor(0xFF222222);
        android.widget.LinearLayout.LayoutParams lpNew = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lpNew.topMargin = 24;
        etNew.setLayoutParams(lpNew);
        etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        EditText etConfirm = new EditText(this);
        etConfirm.setHint("Confirm New Password");
        etConfirm.setHintTextColor(0xFF888888);
        etConfirm.setTextColor(0xFF222222);
        android.widget.LinearLayout.LayoutParams lpConfirm = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lpConfirm.topMargin = 24;
        etConfirm.setLayoutParams(lpConfirm);
        etConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String current = etCurrent.getText().toString().trim();
            String newPass  = etNew.getText().toString().trim();
            String confirm  = etConfirm.getText().toString().trim();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = sessionManager.getUserEmail();
            // Verify current password
            if (!db.checkUser(email, current)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update password
            boolean updated = db.updatePassword(email, newPass);
            if (updated) {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
