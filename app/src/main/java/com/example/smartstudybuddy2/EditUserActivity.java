package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.util.Patterns;
import android.content.SharedPreferences;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.appcompat.app.AppCompatDelegate;

public class EditUserActivity extends AppCompatActivity {

    TextInputEditText emailEditText, usernameEditText, passwordEditText;
    TextInputLayout emailLayout, usernameLayout, passwordLayout, roleLayout;
    MaterialAutoCompleteTextView roleSpinner;
    LinearLayout updateUserButton;
    LinearLayout deleteUserButton;
    DatabaseHelper dbHelper;

    private int userId = -1;
    private String originalEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences("ADMIN_THEME", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("DARK_MODE", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        dbHelper = new DatabaseHelper(this);

        // Text fields and layouts
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        emailLayout = findViewById(R.id.emailLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        roleLayout = findViewById(R.id.roleLayout);

        // Role dropdown
        roleSpinner = findViewById(R.id.roleSpinner);
        updateUserButton = findViewById(R.id.updateUserButton);
        deleteUserButton = findViewById(R.id.deleteUserButton);

        // Get user ID from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("USER_ID", -1);
            String name = extras.getString("USERNAME", "");
            String email = extras.getString("EMAIL", "");
            String role = extras.getString("ROLE", "User");

            originalEmail = email;

            // Pre-fill form
            usernameEditText.setText(name);
            emailEditText.setText(email);
            roleSpinner.setText(role, false);
        }

        // Role options
        String[] roles = {"User", "Admin"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setThreshold(0);

        roleSpinner.setOnClickListener(v -> roleSpinner.showDropDown());
        roleSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) roleSpinner.showDropDown();
        });

        roleSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRole = roles[position];
            roleSpinner.setText(selectedRole, false);
            roleLayout.setError(null);
            roleLayout.setErrorEnabled(false);
        });

        // UPDATE BUTTON
        updateUserButton.setOnClickListener(v -> {

            usernameLayout.setError(null);
            emailLayout.setError(null);
            passwordLayout.setError(null);
            roleLayout.setError(null);

            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
            String role = roleSpinner.getText() != null ? roleSpinner.getText().toString().trim() : "";

            boolean valid = true;

            if (username.isEmpty()) {
                usernameLayout.setError("Username is required");
                valid = false;
            }

            if (email.isEmpty()) {
                emailLayout.setError("Email is required");
                valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError("Enter a valid email");
                valid = false;
            } else if (!email.equals(originalEmail) && dbHelper.isEmailExists(email)) {
                emailLayout.setError("Email already exists");
                valid = false;
            }

            if (password.isEmpty()) {
                passwordLayout.setError("Password is required");
                valid = false;
            } else if (password.length() < 6) {
                passwordLayout.setError("Password must be at least 6 characters");
                valid = false;
            }

            if (role.isEmpty()) {
                roleLayout.setError("Please select a role");
                valid = false;
            }

            if (!valid) {
                if (usernameLayout.getError() != null) {
                    usernameEditText.requestFocus();
                } else if (emailLayout.getError() != null) {
                    emailEditText.requestFocus();
                } else if (passwordLayout.getError() != null) {
                    passwordEditText.requestFocus();
                } else if (roleLayout.getError() != null) {
                    roleSpinner.requestFocus();
                }
                return;
            }

            // Update user in database
            boolean updated = dbHelper.updateUser(userId, email, username, password, role);
            if (updated) {
                Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(EditUserActivity.this, "Error updating user", Toast.LENGTH_SHORT).show();
            }
        });

        // DELETE BUTTON
        deleteUserButton.setOnClickListener(v -> {
            // Confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(EditUserActivity.this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteUserById(userId);
                        if (deleted) {
                            Toast.makeText(EditUserActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditUserActivity.this, "Error deleting user", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}
