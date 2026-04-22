package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity {

    RecyclerView usersRecyclerView;
    TextView tvEmpty;
    ImageView btnBack;
    DatabaseHelper dbHelper;
    ArrayList<UserModel> usersList;
    UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        tvEmpty = findViewById(R.id.tvEmptyUsers);
        btnBack = findViewById(R.id.btnBack);
        dbHelper = new DatabaseHelper(this);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadUsers();

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString());
                        int count2 = adapter.getFilteredCount();
                        if (count2 == 0) {
                            if (tvEmpty != null) {
                                tvEmpty.setText(s.length() > 0 ? "User not found" : "No users found");
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                            if (usersRecyclerView != null) usersRecyclerView.setVisibility(View.GONE);
                        } else {
                            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                            if (usersRecyclerView != null) usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadUsers() {
        try {
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
                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                if (usersRecyclerView != null) usersRecyclerView.setVisibility(View.GONE);
                return;
            }

            adapter = new UserAdapter(this, usersList, dbHelper);
            if (usersRecyclerView != null) {
                usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                usersRecyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}