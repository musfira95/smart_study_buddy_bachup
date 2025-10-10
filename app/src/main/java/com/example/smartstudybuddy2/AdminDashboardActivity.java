package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartstudybuddy2.UserAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    RecyclerView usersRecyclerView;
    Button addUserButton;
    DatabaseHelper dbHelper;
    ArrayList<UserModel> usersList;
    String loggedInEmail;

    Button logoutButton;

    UserAdapter adapter; // ✅ Correct adapter name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        addUserButton = findViewById(R.id.addUserButton);

        loggedInEmail = getIntent().getStringExtra("email");

        loadUsers();

        addUserButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AddUserActivity.class));
        });
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // admin dashboard close kar dega
        });

    }

    private void loadUsers() {
        usersList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int emailIndex = cursor.getColumnIndex("email");
                int usernameIndex = cursor.getColumnIndex("username");
                int roleIndex = cursor.getColumnIndex("role");
                int passwordIndex = cursor.getColumnIndex("password");

                String email = (emailIndex != -1) ? cursor.getString(emailIndex) : "N/A";
                String username = (usernameIndex != -1) ? cursor.getString(usernameIndex) : "Unknown";
                String role = (roleIndex != -1) ? cursor.getString(roleIndex) : "user";
                String password = (passwordIndex != -1) ? cursor.getString(passwordIndex) : "";

                usersList.add(new UserModel(username, email, role, password));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
        }

        // ✅ RecyclerView setup
        UserAdapter adapter = new UserAdapter(this, usersList, dbHelper);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);
    }



    // ✅ Optional (refresh users on screen)
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}
