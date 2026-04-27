package com.example.smartstudybuddy2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for displaying reminders/schedules
 */
public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder> {

    private ArrayList<ScheduleReminder> reminders;
    private Context context;
    private OnReminderActionListener listener;
    private DatabaseHelper dbHelper;

    public interface OnReminderActionListener {
        void onReminderDelete(int reminderId);
        void onReminderEdit(ScheduleReminder reminder);
        void onReminderComplete(int reminderId, boolean isCompleted);
    }

    public RemindersAdapter(Context context, ArrayList<ScheduleReminder> reminders, OnReminderActionListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ScheduleReminder reminder = reminders.get(position);

        // Set title
        holder.tvTitle.setText(reminder.getTitle());

        // Set time
        holder.tvTime.setText(reminder.getDisplayTime());

        // Set date
        android.util.Log.d("DEBUG_DATE", "[ADAPTER] Reminder ID: " + reminder.getId() + " | Title: " + reminder.getTitle() + " | Displaying date: " + reminder.getDate() + " | Time: " + reminder.getTime());
        holder.tvDate.setText(reminder.getDate());

        // Set feature type name
        holder.tvFeatureType.setText(reminder.getFeatureType() != null ? reminder.getFeatureType() : "Reminder");

        // Set description if available
        if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
            holder.tvDescription.setText(reminder.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Set completion checkbox
        holder.cbCompleted.setChecked(reminder.isCompleted());
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onReminderComplete(reminder.getId(), isChecked);
            }
        });

        // Click on card to open linked feature or edit
        holder.cardView.setOnClickListener(v -> {
            openReminderFeature(reminder);
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderEdit(reminder);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderDelete(reminder.getId());
            }
        });

        // Strikethrough if completed
        if (reminder.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.cardView.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.cardView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    /**
     * Open the linked feature based on feature type
     */
    private void openReminderFeature(ScheduleReminder reminder) {
        if (reminder.getFeatureType() == null || reminder.getFeatureType().equals("reminder")) {
            Toast.makeText(context, "This is a simple reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = null;
        try {
            switch (reminder.getFeatureType().toLowerCase()) {
                case "recording":
                    intent = new Intent(context, RecordingDetailActivity.class);
                    intent.putExtra("RECORDING_ID", reminder.getFeatureId());
                    break;

                case "flashcard":
                    intent = new Intent(context, FlashcardActivity.class);
                    intent.putExtra("FLASHCARD_ID", reminder.getFeatureId());
                    break;

                case "quiz":
                    intent = new Intent(context, QuizActivity.class);
                    intent.putExtra("QUIZ_ID", reminder.getFeatureId());
                    break;

                case "pdf_export":
                    intent = new Intent(context, ExportActivity.class);
                    intent.putExtra("RECORDING_ID", reminder.getFeatureId());
                    break;

                default:
                    Toast.makeText(context, "Unknown feature type", Toast.LENGTH_SHORT).show();
                    return;
            }

            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error opening feature: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Remove reminder from list
     */
    public void removeReminder(int reminderId) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId() == reminderId) {
                reminders.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Update reminder in list
     */
    public void updateReminder(ScheduleReminder updatedReminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId() == updatedReminder.getId()) {
                reminders.set(i, updatedReminder);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * ViewHolder class
     */
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvTime;
        TextView tvDate;
        TextView tvFeatureType;
        TextView tvDescription;
        CheckBox cbCompleted;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardReminder);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            tvDate = itemView.findViewById(R.id.tvReminderDate);
            tvFeatureType = itemView.findViewById(R.id .tvFeatureType);
            tvDescription = itemView.findViewById(R.id.tvReminderDescription);
            cbCompleted = itemView.findViewById(R.id.cbReminderCompleted);
            btnEdit = itemView.findViewById(R.id.btnEditReminder);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
        }
    }
}
