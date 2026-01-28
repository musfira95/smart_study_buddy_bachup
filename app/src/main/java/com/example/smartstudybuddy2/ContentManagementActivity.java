package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ContentManagementActivity extends AppCompatActivity {

    private EditText etAppVersion, etAppDescription, etSupportEmail, etSupportPhone;
    private Button btnSaveContent;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_management);

        // Initialize views
        etAppVersion = findViewById(R.id.etAppVersion);
        etAppDescription = findViewById(R.id.etAppDescription);
        etSupportEmail = findViewById(R.id.etSupportEmail);
        etSupportPhone = findViewById(R.id.etSupportPhone);
        btnSaveContent = findViewById(R.id.btnSaveContent);

        dbHelper = new DatabaseHelper(this);

        // Load current content
        loadCurrentContent();

        btnSaveContent.setOnClickListener(v -> saveContent());
    }

    private void loadCurrentContent() {
        String description = dbHelper.getAboutAppDescription();
        String version = dbHelper.getAboutAppVersion();
        String email = dbHelper.getHelpSupportEmail();
        String phone = dbHelper.getHelpSupportPhone();

        etAppDescription.setText(description);
        etAppVersion.setText(version);
        etSupportEmail.setText(email);
        etSupportPhone.setText(phone);
    }

    private void saveContent() {
        String version = etAppVersion.getText().toString().trim();
        String description = etAppDescription.getText().toString().trim();
        String email = etSupportEmail.getText().toString().trim();
        String phone = etSupportPhone.getText().toString().trim();

        // Validation
        if (version.isEmpty() || description.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update database
        boolean success = dbHelper.updateAboutContent(version, description, email, phone);

        if (success) {
            Toast.makeText(this, "Content updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update content", Toast.LENGTH_SHORT).show();
        }
    }
}
