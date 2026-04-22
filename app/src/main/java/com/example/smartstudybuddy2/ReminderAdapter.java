package com.example.smartstudybuddy2;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 * Adapter for displaying reminders in RecyclerView
 * Deprecated: Use RemindersAdapter instead
 * This class is kept for compatibility but should not be used
 */
@Deprecated
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    ArrayList<ScheduleReminder> list;
    private OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onReminderDelete(int reminderId);
        void onReminderEdit(ScheduleReminder reminder);
        void onReminderComplete(int reminderId, boolean isCompleted);
    }

    public ReminderAdapter(ArrayList<ScheduleReminder> list, OnReminderActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            ScheduleReminder reminder = list.get(position);
            if (reminder == null) return;

            // Set title and description
            if (holder.tvTitle != null) {
                holder.tvTitle.setText(reminder.getTitle());
            }
            
            // Set date and time
            if (holder.tvTime != null) {
                String timeDisplay = reminder.getDisplayTime() + " • " + reminder.getDate();
                holder.tvTime.setText(timeDisplay);
            }

            // Set feature type if available
            if (holder.tvFeatureType != null) {
                if (reminder.getFeatureType() != null && !reminder.getFeatureType().isEmpty()) {
                    holder.tvFeatureType.setText(reminder.getFeatureType().replace("_", " ").toUpperCase());
                    holder.tvFeatureType.setVisibility(View.VISIBLE);
                } else {
                    holder.tvFeatureType.setVisibility(View.GONE);
                }
            }

            // Set completion checkbox
            if (holder.cbCompleted != null) {
                holder.cbCompleted.setChecked(reminder.isCompleted());
                holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onReminderComplete(reminder.getId(), isChecked);
                    }
                });
            }

            // Edit button
            if (holder.btnEdit != null && listener != null) {
                holder.btnEdit.setOnClickListener(v -> listener.onReminderEdit(reminder));
            }

            // Delete button
            if (holder.btnDelete != null && listener != null) {
                holder.btnDelete.setOnClickListener(v -> listener.onReminderDelete(reminder.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void removeReminder(int reminderId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == reminderId) {
                list.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateReminder(ScheduleReminder reminder) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == reminder.getId()) {
                list.set(i, reminder);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvFeatureType;
        ImageButton btnEdit, btnDelete;
        CheckBox cbCompleted;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            tvFeatureType = itemView.findViewById(R.id.tvFeatureType);
            btnEdit = itemView.findViewById(R.id.btnEditReminder);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
            cbCompleted = itemView.findViewById(R.id.cbReminderCompleted);
        }
    }
}
