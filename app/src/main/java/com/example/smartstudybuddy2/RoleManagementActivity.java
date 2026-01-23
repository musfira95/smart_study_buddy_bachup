package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class RoleManagementActivity extends AppCompatActivity {

    TextView usernameLabel;
    TextInputLayout roleLayout;
    MaterialAutoCompleteTextView roleSpinner;
    MaterialButton saveBtn, cancelBtn;
    DatabaseHelper dbHelper;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_management);

        dbHelper = new DatabaseHelper(this);

        usernameLabel = findViewById(R.id.usernameLabel);
        roleLayout = findViewById(R.id.roleLayout);
        roleSpinner = findViewById(R.id.roleSpinner);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        String[] roles = new String[]{"User", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        roleSpinner.setAdapter(adapter);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        String username = intent.getStringExtra("username");
        String role = intent.getStringExtra("role");

        if (username != null) usernameLabel.setText(username);
        if (role != null) roleSpinner.setText(role.substring(0,1).toUpperCase(Locale.ROOT) + role.substring(1));

        saveBtn.setOnClickListener(v -> {
            String newRole = roleSpinner.getText() != null ? roleSpinner.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
            roleLayout.setError(null);
            if (newRole.isEmpty()) {
                roleLayout.setError("Please select a role");
                return;
            }

            // Prevent demoting the last admin
            String currentRole = dbHelper.getUserRole(email);
            if (currentRole.equalsIgnoreCase("admin") && newRole.equals("user") && dbHelper.isLastAdmin(email)) {
                Toast.makeText(this, "Cannot demote the last admin", Toast.LENGTH_LONG).show();
                return;
            }

            boolean ok = dbHelper.updateUserRole(email, newRole);
            if (ok) {
                Toast.makeText(this, "Role updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update role", Toast.LENGTH_SHORT).show();
            }
        });

        cancelBtn.setOnClickListener(v -> finish());
    }
}