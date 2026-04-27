package com.example.smartstudybuddy2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordingListAdapter extends BaseAdapter {

    private static final String TAG = "RecordingListAdapter";

    private Context context;
    private ArrayList<Recording> recordings;

    public RecordingListAdapter(Context context, ArrayList<Recording> recordings) {
        this.context = context;
        this.recordings = recordings;
    }

    @Override
    public int getCount() {
        return recordings.size();
    }

    @Override
    public Object getItem(int position) {
        return recordings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return recordings.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recording, parent, false);
        }

        TextView recordingTitle = convertView.findViewById(R.id.tvRecordingName);
        TextView recordingDate = convertView.findViewById(R.id.tvRecordingDate);
        ImageButton btnBookmark = convertView.findViewById(R.id.btnBookmark);
        ImageButton btnDeleteRecording = convertView.findViewById(R.id.btnDeleteRecording);

        Recording recording = recordings.get(position);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // Display title from database
        String title = recording.getTitle() != null ? recording.getTitle() : "Untitled";
        recordingTitle.setText(title);

        // Display date from database
        String date = recording.getDate() != null ? recording.getDate() : "Unknown";
        recordingDate.setText(date);

        Log.d(TAG, "✅ [" + position + "] Item: " + title + " (" + date + ")");

        // Update bookmark button visual state
        updateBookmarkButtonState(btnBookmark, recording.getId(), dbHelper);

        // Bookmark button click handler
        btnBookmark.setOnClickListener(v -> {
            boolean newBookmarkState = dbHelper.toggleBookmark(recording.getId());
            updateBookmarkButtonState(btnBookmark, recording.getId(), dbHelper);
            Log.d(TAG, "⭐ Bookmark toggled for " + title + ": " + newBookmarkState);
        });

        // Delete button click handler
        btnDeleteRecording.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Delete Recording")
                .setMessage("Are you sure you want to delete this recording?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteRecordingById(recording.getId())) {
                        recordings.remove(position);
                        notifyDataSetChanged();
                        android.widget.Toast.makeText(context, "Recording deleted", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(context, "Failed to delete", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Entire item is clickable - opens detail screen with recording ID
        convertView.setOnClickListener(v -> {
            Log.d(TAG, "📋 [" + position + "] Item clicked: " + title);
            Intent intent = new Intent(context, RecordingDetailActivity.class);
            intent.putExtra("recording_id", recording.getId());
            Log.d(TAG, "   Passing recording_id: " + recording.getId());
            context.startActivity(intent);
        });

        return convertView;
    }

    /**
     * Update bookmark button visual state (filled or empty star)
     */
    private void updateBookmarkButtonState(ImageButton button, int recordingId, DatabaseHelper dbHelper) {
        if (dbHelper.isBookmarked(recordingId)) {
            button.setImageResource(android.R.drawable.btn_star_big_on);
            button.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_dark), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            button.setImageResource(android.R.drawable.btn_star_big_off);
            button.setColorFilter(context.getResources().getColor(android.R.color.darker_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}

