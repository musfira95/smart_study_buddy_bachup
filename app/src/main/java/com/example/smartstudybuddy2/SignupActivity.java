package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;


public class SignupActivity extends AppCompatActivity {

    EditText emailEditText, usernameEditText, passwordEditText;
    Button signupButton;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if(email.isEmpty() || username.isEmpty() || password.isEmpty()){
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(SignupActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                }
                else if(password.length() < 6){
                    Toast.makeText(SignupActivity.this, "Password must be 6+ characters", Toast.LENGTH_SHORT).show();
                }
                else {
                    // ✅ Updated insertUser with role "user"
                    boolean inserted = dbHelper.insertUser(email, username, password, "user");
                    if(inserted){
                        Toast.makeText(SignupActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        LinearLayout googleSignUpBtn, facebookSignUpBtn;

        googleSignUpBtn = findViewById(R.id.googleSignUpBtn);
        facebookSignUpBtn = findViewById(R.id.facebookSignUpBtn);

        googleSignUpBtn.setOnClickListener(v -> {
            Toast.makeText(SignupActivity.this, "Google SignUp Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        facebookSignUpBtn.setOnClickListener(v -> {
            Toast.makeText(SignupActivity.this, "Facebook SignUp Coming Soon!", Toast.LENGTH_SHORT).show();
        });


    }
}
