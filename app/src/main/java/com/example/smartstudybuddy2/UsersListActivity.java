package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity {

    RecyclerView usersRecyclerView;
    TextView tvEmpty;
    DatabaseHelper dbHelper;
    ArrayList<UserModel> usersList;
    UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        tvEmpty = findViewById(R.id.tvEmptyUsers);
        dbHelper = new DatabaseHelper(this);

        loadUsers();
    }

    private void loadUsers() {
        usersList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String username = cursor.getString(0);
                String email = cursor.getString(1);
                String role = cursor.getString(2);
                String password = cursor.getString(3);
                int isBlocked = cursor.getInt(4);

                usersList.add(new UserModel(username, email, role, password, isBlocked));

            } while (cursor.moveToNext());
            cursor.close();
        }

        if (usersList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
            return;
        }

        adapter = new UserAdapter(this, usersList, dbHelper);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);
    }
}