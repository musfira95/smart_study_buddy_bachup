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
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminProfileActivity.class);
                startActivity(intent);
            });
        }


        // INIT DRAWER
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu = findViewById(R.id.btnMenu);

        // LOAD DATA
        loadDashboardData();

        // 🍔 HAMBURGER CLICK
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    drawerLayout.openDrawer(Gravity.START)
            );
        }

        // 📌 DRAWER MENU CLICK HANDLING
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

             if (id == R.id.nav_user_management) {
                startActivity(new Intent(this, UsersListActivity.class));
            }

            else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, ThemeSettingsActivity.class));
            }
            else if (id == R.id.nav_content_management) {
                startActivity(new Intent(this, ContentManagementActivity.class));
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
        try {
            int totalUsers = db.getTotalUsers();
            if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(totalUsers));

            int totalAudios = db.getTotalAudios();
            if (tvTotalAudios != null) tvTotalAudios.setText(String.valueOf(totalAudios));

            int totalNotes = db.getTotalNotes();
            if (tvTotalNotes != null) tvTotalNotes.setText(String.valueOf(totalNotes));

            int blockedUsers = db.getBlockedUsersCount();
            if (tvBlockedUsers != null) tvBlockedUsers.setText(String.valueOf(blockedUsers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
