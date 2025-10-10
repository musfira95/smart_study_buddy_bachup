package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView signupRedirect;
    DatabaseHelper dbHelper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        dbHelper.updateUserRole("musfira@gmail.com", "admin");
        dbHelper.updateUserRole("admin@example.com", "user");


//        // Add admin user if not exists
//        if (!dbHelper.checkEmailExists("admin@example.com")) {
//            dbHelper.insertUser("admin@example.com", "Admin Name", "123456", "admin");
//        }


        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupRedirect = findViewById(R.id.signupRedirect);

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(LoginActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                }
                else if (password.length() < 6) {
                    Toast.makeText(LoginActivity.this, "Password must be 6+ characters", Toast.LENGTH_SHORT).show();
                }
                else {
                    boolean valid = dbHelper.checkUser(email, password);
                    if (valid) {
                        // ✅ Get user role from DB
                        String role = dbHelper.getUserRole(email);

                        // ✅ Save session with role
                        session.createLoginSession(email, role);

                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // ✅ Redirect based on role
                        if (role.equals("admin")) {
                            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            intent.putExtra("email", email); // ✅ Pass logged-in admin email
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("email", email); // ✅ Pass logged-in user email
                            startActivity(intent);
                            finish();
                        }

                    }
                }
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
