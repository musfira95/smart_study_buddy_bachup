package com.example.smartstudybuddy2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TranscriptionAdapter extends RecyclerView.Adapter<TranscriptionAdapter.ViewHolder> {

    private static final String TAG = "TranscriptionAdapter";

    private Context context;
    private ArrayList<Recording> transcriptions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recording recording);
    }

    public TranscriptionAdapter(Context context, ArrayList<Recording> transcriptions, OnItemClickListener listener) {
        this.context = context;
        this.transcriptions = transcriptions != null ? transcriptions : new ArrayList<>();
        this.listener = listener;

        Log.d(TAG, "TranscriptionAdapter created with " + this.transcriptions.size() + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transcription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= transcriptions.size()) {
            Log.w(TAG, "❌ Invalid position: " + position);
            return;
        }

        Recording recording = transcriptions.get(position);

        if (recording != null) {
            String title = recording.getTitle() != null ? recording.getTitle() : "Untitled";
            String date = recording.getDate() != null ? recording.getDate() : "Unknown";
            String transcriptionFromDB = recording.getTranscription();
            
            // Display actual transcription from database, or indicate if pending
            String displayText;
            if (transcriptionFromDB != null && !transcriptionFromDB.isEmpty()) {
                displayText = transcriptionFromDB;
                Log.d(TAG, "✅ [" + position + "] Binding with DB transcription: \"" + title + "\" (" + transcriptionFromDB.length() + " chars)");
            } else {
                displayText = "⏳ Transcription pending...";
                Log.d(TAG, "⏳ [" + position + "] Binding without transcription yet: \"" + title + "\"");
            }

            holder.tvTitle.setText(title);
            holder.tvDate.setText(date);
            holder.tvTranscriptionPreview.setText(displayText);

            Log.d(TAG, "   Bound: Title=" + title + " | Date=" + date + " | HasTranscription=" + (transcriptionFromDB != null && !transcriptionFromDB.isEmpty()));
        } else {
            Log.w(TAG, "⚠️ Null recording at position " + position);
            holder.tvTitle.setText("No data");
            holder.tvDate.setText("Unknown");
            holder.tvTranscriptionPreview.setText("⚠️ No data available");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && recording != null) {
                listener.onItemClick(recording);
            }
        });

        holder.btnDeleteTranscription.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Delete Transcription")
                .setMessage("Are you sure you want to delete this transcription?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    if (dbHelper.deleteRecordingById(recording.getId())) {
                        transcriptions.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, transcriptions.size());
                        android.widget.Toast.makeText(context, "Transcription deleted", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(context, "Failed to delete", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    @Override
    public int getItemCount() {
        return transcriptions != null ? transcriptions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTranscriptionPreview;
        android.widget.ImageView btnDeleteTranscription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTranscriptionTitle);
            tvDate = itemView.findViewById(R.id.tvTranscriptionDate);
            tvTranscriptionPreview = itemView.findViewById(R.id.tvTranscriptionPreview);
            btnDeleteTranscription = itemView.findViewById(R.id.btnDeleteTranscription);
        }
    }
}
