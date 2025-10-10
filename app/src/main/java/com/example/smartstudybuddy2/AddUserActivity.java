package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;


public class AddUserActivity extends AppCompatActivity {

    EditText emailEditText, usernameEditText, passwordEditText;
    Spinner roleSpinner;
    Button addUserButton;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        dbHelper = new DatabaseHelper(this);

        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        addUserButton = findViewById(R.id.addUserButton);

        // Role options
        String[] roles = {"User", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // spinner main text black
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE); // dropdown ka background white
                ((TextView) view).setTextColor(Color.BLACK); // dropdown items black
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);



        addUserButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if(email.isEmpty() || username.isEmpty() || password.isEmpty()){
                Toast.makeText(AddUserActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
            } else if(password.length() < 6){
                Toast.makeText(AddUserActivity.this, "Password must be 6+ characters", Toast.LENGTH_SHORT).show();
            } else {
                boolean inserted = dbHelper.insertUser(email, username, password, role);
                if(inserted){
                    Toast.makeText(AddUserActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // go back to Admin Dashboard
                } else {
                    Toast.makeText(AddUserActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
