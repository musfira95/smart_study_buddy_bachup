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
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification); // ✅ correct layout

        // ✅ MATCHING IDs FROM XML
        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        listNotifications = findViewById(R.id.listNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Dummy notifications (example)
        notificationList = new ArrayList<>();
        notificationList.add("📘 New note saved");
        notificationList.add("⏰ Reminder: Study at 7 PM");
        notificationList.add("✅ Quiz completed");
        notificationList.add("💾 Backup created successfully");

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                notificationList
        );

        NotificationAdapter adapter;

        notificationList = new ArrayList<>();
        notificationList.add("New note saved");
        notificationList.add("Reminder: Study at 7 PM");
        notificationList.add("Quiz completed successfully");
        notificationList.add("Your notes are safely stored");


        adapter = new NotificationAdapter(this, notificationList);
        listNotifications.setAdapter(adapter);

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
