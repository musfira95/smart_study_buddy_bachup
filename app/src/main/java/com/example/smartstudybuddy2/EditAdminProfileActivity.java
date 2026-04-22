package com.example.smartstudybuddy2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class EditAdminProfileActivity extends AppCompatActivity {

    EditText etAdminName, etAdminEmail, etAdminRole;
    EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    Button saveBtn;
    ImageView btnBack, ivEditAvatar, btnChangePhoto;

    DatabaseHelper db;
    SessionManager sessionManager;

    private ActivityResultLauncher<String> imagePickerLauncher;

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
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        saveBtn = findViewById(R.id.saveBtn);
        btnBack = findViewById(R.id.btnBack);
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (saveBtn != null) saveBtn.setOnClickListener(v -> updateProfile());

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Persist permission
                            getContentResolver().takePersistableUriPermission(
                                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}

                        // Save URI to SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                        prefs.edit().putString("admin_profile_pic_uri", uri.toString()).apply();

                        // Show selected image
                        showPhoto(uri);
                    }
                }
        );

        // Click avatar or camera button to pick photo
        if (ivEditAvatar != null) ivEditAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        if (btnChangePhoto != null) btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        loadAdminData();
        loadExistingPhoto();
    }

    private void loadExistingPhoto() {
        try {
            SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
            String uriStr = prefs.getString("admin_profile_pic_uri", null);
            if (uriStr != null) {
                showPhoto(Uri.parse(uriStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPhoto(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null && ivEditAvatar != null) {
                ivEditAvatar.setPadding(0, 0, 0, 0);
                ivEditAvatar.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load admin data
    private void loadAdminData() {
        try {
            String email = sessionManager.getUserEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (etAdminEmail != null) etAdminEmail.setText(email);
            if (etAdminRole != null) etAdminRole.setText("Admin");
            String username = db.getUsernameByEmail(email);
            if (username != null && etAdminName != null) {
                etAdminName.setText(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update name + password
    private void updateProfile() {
        String email = sessionManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // NAME UPDATE
        String newName = etAdminName != null ? etAdminName.getText().toString().trim() : "";
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean nameUpdated = db.updateUsername(email, newName);

        // PASSWORD UPDATE (OPTIONAL)
        String currentPass = etCurrentPassword != null ? etCurrentPassword.getText().toString().trim() : "";
        String newPass = etNewPassword != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

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

        if (nameUpdated && passwordUpdated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}
