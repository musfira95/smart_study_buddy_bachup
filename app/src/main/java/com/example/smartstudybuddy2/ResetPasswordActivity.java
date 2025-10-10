package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText newPassEditText, confirmPassEditText;
    Button resetBtn;
    DatabaseHelper dbHelper;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        dbHelper = new DatabaseHelper(this);

        newPassEditText = findViewById(R.id.resetNewPasswordEditText);
        confirmPassEditText = findViewById(R.id.resetConfirmPasswordEditText);
        resetBtn = findViewById(R.id.resetPasswordButton);

        // get email from intent
        userEmail = getIntent().getStringExtra("email");

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String p1 = newPassEditText.getText().toString().trim();
                String p2 = confirmPassEditText.getText().toString().trim();

                if (p1.isEmpty() || p2.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (p1.length() < 6) {
                    Toast.makeText(ResetPasswordActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!p1.equals(p2)) {
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean updated = dbHelper.updatePassword(userEmail, p1);
                if (updated) {
                    Toast.makeText(ResetPasswordActivity.this, "Password updated. Please login.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
