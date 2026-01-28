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
    ListView listNotifications;
    TextView tvEmpty;

    ArrayList<String> notificationList;
    NotificationAdapter adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        listNotifications = findViewById(R.id.listNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        dbHelper = new DatabaseHelper(this);

        // Load notifications from database
        notificationList = dbHelper.getAllNotifications();

        if (notificationList.isEmpty()) {
            checkEmptyState();
        } else {
            adapter = new NotificationAdapter(this, notificationList);
            listNotifications.setAdapter(adapter);
            checkEmptyState();
        }

        // Clear all notifications
        btnClearNotifications.setOnClickListener(v -> {
            dbHelper.clearAllNotifications();
            notificationList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            checkEmptyState();
            Toast.makeText(this, "Notifications cleared", Toast.LENGTH_SHORT).show();
        });
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

}
