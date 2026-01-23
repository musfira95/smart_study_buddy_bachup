package com.example.smartstudybuddy2;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchUsersActivity extends AppCompatActivity {

    SearchView searchView;
    TextView noResultsText;
    RecyclerView recyclerView;

    ArrayList<UserModel> fullList = new ArrayList<>();
    ArrayList<UserModel> filteredList = new ArrayList<>();

    SearchUserAdapter adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 Theme handling
        SharedPreferences prefs = getSharedPreferences("ADMIN_THEME", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("DARK_MODE", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        // 🔗 Views
        searchView = findViewById(R.id.searchView);
        noResultsText = findViewById(R.id.noResultsText);
        recyclerView = findViewById(R.id.searchRecycler);

        dbHelper = new DatabaseHelper(this);

        // 📥 Load users
        loadUsers();

        // 📋 RecyclerView setup
        adapter = new SearchUserAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 🔍 SearchView listener (CORRECT WAY)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    // 📥 Load users from DB
    private void loadUsers() {
        fullList.clear();
        filteredList.clear();

        Cursor cursor = dbHelper.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String username = cursor.getString(0);
                String email = cursor.getString(1);
                String role = cursor.getString(2);
                String password = cursor.getString(3);
                int isBlocked = cursor.getInt(4);

                fullList.add(new UserModel(username, email, role, password, isBlocked));

            } while (cursor.moveToNext());

            cursor.close();
        }

        filteredList.addAll(fullList);
    }

    // 🔎 Filter logic
    private void filterUsers(String text) {
        filteredList.clear();

        for (UserModel user : fullList) {
            if (
                    user.getUsername().toLowerCase().contains(text.toLowerCase()) ||
                            user.getEmail().toLowerCase().contains(text.toLowerCase())
            ) {
                filteredList.add(user);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            noResultsText.setVisibility(View.GONE);
        }
    }
}
