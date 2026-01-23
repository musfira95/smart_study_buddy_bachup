package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class Reminders extends AppCompatActivity {

    FloatingActionButton fabAdd;
    RecyclerView reminderRecycler;
    FrameLayout emptyStateLayout;

    ArrayList<ReminderModel> reminderList = new ArrayList<>();
    ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        fabAdd = findViewById(R.id.fabAdd);
        reminderRecycler = findViewById(R.id.reminderRecycler);
        emptyStateLayout = findViewById(R.id.emptystateLayout);

        reminderRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList);
        reminderRecycler.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> openAddReminderPopup());
    }

    private void openAddReminderPopup() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottomsheet_add_reminder, null);
        dialog.setContentView(view);

        EditText etTitle = view.findViewById(R.id.etReminderTitle);
        TimePicker timePicker = view.findViewById(R.id.timePicker);

        // ✅ FIX IS HERE
        LinearLayout btnSave = view.findViewById(R.id.btnSaveReminder);

        btnSave.setOnClickListener(v -> {
            if (etTitle.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = timePicker.getHour() + ":" + timePicker.getMinute();
            reminderList.add(new ReminderModel(etTitle.getText().toString(), time));

            adapter.notifyDataSetChanged();
            emptyStateLayout.setVisibility(View.GONE);
            reminderRecycler.setVisibility(View.VISIBLE);
            dialog.dismiss();
        });

        dialog.show();
    }
}
