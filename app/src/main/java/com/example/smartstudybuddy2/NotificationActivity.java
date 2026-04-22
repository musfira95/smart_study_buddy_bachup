package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    ImageView btnClearNotifications;
    ImageView btnBack;
    ListView listNotifications;
    TextView tvEmpty;

    ArrayList<String> notificationList;
    NotificationAdapter adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        try {
            btnClearNotifications = findViewById(R.id.btnClearNotifications);
            btnBack = findViewById(R.id.btnBack);
            listNotifications = findViewById(R.id.listNotifications);
            tvEmpty = findViewById(R.id.tvEmpty);

            // Back arrow
            if (btnBack != null) {
                btnBack.setColorFilter(android.graphics.Color.parseColor("#4F6F64"));
                btnBack.setOnClickListener(v -> finish());
            }

            dbHelper = new DatabaseHelper(this);

            // Load notifications from database with error handling
            notificationList = dbHelper.getAllNotifications();
            
            if (notificationList == null) {
                notificationList = new ArrayList<>();
            }

            if (notificationList.isEmpty()) {
                checkEmptyState();
            } else {
                adapter = new NotificationAdapter(this, notificationList);
                if (listNotifications != null) {
                    listNotifications.setAdapter(adapter);
                }
                checkEmptyState();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("NotificationActivity", "Error: " + e.getMessage());
            notificationList = new ArrayList<>();
            checkEmptyState();
        }

        // Clear all notifications
        if (btnClearNotifications != null) {
            btnClearNotifications.setOnClickListener(v -> {
                try {
                    dbHelper.clearAllNotifications();
                    if (notificationList != null) {
                        notificationList.clear();
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    checkEmptyState();
                    Toast.makeText(this, "Notifications cleared", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error clearing notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkEmptyState() {
        if (notificationList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listNotifications.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
