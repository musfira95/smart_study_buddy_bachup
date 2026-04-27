package com.example.smartstudybuddy2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 * HistoryAdapter - Displays Study Sessions in a RecyclerView
 * Shows: Title, Date, Duration, Quiz Score
 * Supports click and delete actions
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private ArrayList<Object> historyList;
    private OnItemClickListener clickListener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(StudySession session);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(StudySession session, int position);
    }

    public HistoryAdapter(Context context, ArrayList<Object> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public void setClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setDeleteListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Object item = historyList.get(position);

        if (item instanceof StudySession) {
            StudySession session = (StudySession) item;
            
            // Title: Show audio filename
            holder.tvTitle.setText(session.getTitle());
            
            // Date: Show created date
            holder.tvDate.setText(session.getCreatedDate());
            
            // Icon for Study Session
            holder.ivIcon.setImageResource(R.drawable.ic_study);
            
            // NO click on item - only delete button is functional
            holder.itemView.setClickable(false);
            
            // Delete button visible
            holder.btnDelete.setVisibility(android.view.View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(session, position);
                }
            });
            
        } else if (item instanceof Recording) {
            Recording r = (Recording) item;
            holder.tvTitle.setText(r.getTitle());
            holder.tvDate.setText(r.getDate());
            holder.ivIcon.setImageResource(R.drawable.ic_recording);
            
            // Non-clickable, but delete button VISIBLE
            holder.itemView.setClickable(false);
            holder.btnDelete.setVisibility(android.view.View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                // Show confirmation dialog before deleting
                Log.d("HistoryAdapter", "🗑️ Delete Recording requested: " + r.getTitle());
                
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppDialog);
                builder.setTitle("Delete Recording")
                        .setMessage("Are you sure you want to delete \"" + r.getTitle() + "\"?\n\nThis action cannot be undone.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Log.d("HistoryAdapter", "Delete cancelled by user");
                        })
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Delete from database
                            try {
                                DatabaseHelper dbHelper = new DatabaseHelper(context);
                                if (dbHelper.deleteRecording(r.getId())) {
                                    // Remove from list and notify adapter
                                    int pos = historyList.indexOf(r);
                                    if (pos >= 0) {
                                        historyList.remove(pos);
                                        notifyItemRemoved(pos);
                                        notifyItemRangeChanged(pos, historyList.size());
                                    }
                                    Log.d("HistoryAdapter", "✅ Recording deleted: " + r.getTitle());
                                    Toast.makeText(context, "Recording deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("HistoryAdapter", "❌ Failed to delete recording");
                                    Toast.makeText(context, "Failed to delete recording", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("HistoryAdapter", "❌ Error deleting recording: " + e.getMessage());
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
            });
            
        } else if (item instanceof QuizResult) {
            QuizResult q = (QuizResult) item;
            String scoreText = String.format("🎯 Quiz: %s - %.1f%%",
                    q.getCategory(), q.getScorePercentage());
            holder.tvTitle.setText(scoreText);
            holder.tvDate.setText(q.getCreatedDate());
            holder.ivIcon.setImageResource(R.drawable.ic_quiz);
            
            // Non-clickable, but show delete button for Quiz
            holder.itemView.setClickable(false);
            holder.btnDelete.setVisibility(android.view.View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppDialog);
                builder.setTitle("Delete Quiz Result")
                        .setMessage("Remove this quiz result from history?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            try {
                                DatabaseHelper dbHelper = new DatabaseHelper(context);
                                dbHelper.getWritableDatabase().delete("quiz_results", "id = ?",
                                        new String[]{String.valueOf(q.getId())});
                                int pos = historyList.indexOf(q);
                                if (pos >= 0) {
                                    historyList.remove(pos);
                                    notifyItemRemoved(pos);
                                    notifyItemRangeChanged(pos, historyList.size());
                                }
                                Toast.makeText(context, "Quiz result deleted", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDate;
        ImageView btnDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
