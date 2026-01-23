package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EditAdminProfileActivity extends AppCompatActivity {

    EditText etAdminName, etAdminEmail, etAdminRole;
    EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    LinearLayout saveBtn;

    DatabaseHelper db;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin_profile);

        // INIT
        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etAdminName = findViewById(R.id.etAdminName);
        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminRole = findViewById(R.id.etAdminRole);

        // 🔐 PASSWORD FIELDS
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        saveBtn = findViewById(R.id.saveBtn);

        loadAdminData();

        saveBtn.setOnClickListener(v -> updateProfile());
    }

    // 🔹 Load admin data
    private void loadAdminData() {
        String email = sessionManager.getUserEmail();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etAdminEmail.setText(email);
        etAdminRole.setText("Admin");

        String username = db.getUsernameByEmail(email);
        if (username != null) {
            etAdminName.setText(username);
        }
    }

    // 🔹 Update name + password
    private void updateProfile() {

        String email = sessionManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ---- NAME UPDATE ----
        String newName = etAdminName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nameUpdated = db.updateUsername(email, newName);

        // ---- PASSWORD UPDATE (OPTIONAL) ----
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        boolean passwordUpdated = true;

        if (!currentPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {

            String dbPassword = db.getPasswordByEmail(email);

            if (!currentPass.equals(dbPassword)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            passwordUpdated = db.updatePassword(email, newPass);
        }

        // ---- FINAL RESULT ----
        if (nameUpdated && passwordUpdated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}
