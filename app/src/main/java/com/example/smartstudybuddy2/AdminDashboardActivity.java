package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvTotalUsers, tvTotalAudios, tvTotalNotes, tvBlockedUsers;
    DatabaseHelper db;

    // 🔹 Drawer items
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView btnMenu;   // hamburger icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // INIT DATABASE
        db = new DatabaseHelper(this);

        // INIT TEXTVIEWS
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalAudios = findViewById(R.id.tvTotalAudios);
        tvTotalNotes = findViewById(R.id.tvTotalNotes);
        tvBlockedUsers = findViewById(R.id.tvBlockedUsers);
        ImageView btnProfile = findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminProfileActivity.class);
            startActivity(intent);
        });


        // INIT DRAWER
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu = findViewById(R.id.btnMenu);

        // LOAD DATA
        loadDashboardData();

        // 🍔 HAMBURGER CLICK
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(Gravity.START)
        );

        // 📌 DRAWER MENU CLICK HANDLING
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

             if (id == R.id.nav_user_management) {
                startActivity(new Intent(this, UsersListActivity.class));
            }

            else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, ThemeSettingsActivity.class));
            }
             else if (id == R.id.search_users) {
                 startActivity(new Intent(this, SearchUsersActivity.class));
             }
            else if (id == R.id.nav_logout) {
                new SessionManager(this).logoutUser();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(Gravity.START);
            return true;
        });
    }

    private void loadDashboardData() {

        // 🔢 TOTAL USERS
        int totalUsers = db.getTotalUsers();
        tvTotalUsers.setText(String.valueOf(totalUsers));

        // 🎧 TOTAL AUDIOS
        int totalAudios = db.getTotalAudios();
        tvTotalAudios.setText(String.valueOf(totalAudios));

        // 📝 TOTAL NOTES / TRANSCRIPTIONS
        int totalNotes = db.getTotalNotes();
        tvTotalNotes.setText(String.valueOf(totalNotes));

        // 🚫 BLOCKED USERS
        int blockedUsers = db.getBlockedUsersCount();
        tvBlockedUsers.setText(String.valueOf(blockedUsers));
    }
}
