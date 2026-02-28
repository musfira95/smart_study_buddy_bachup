package com.example.smartstudybuddy2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

        Recording recording = recordings.get(position);

        // Display title from database
        String title = recording.getTitle() != null ? recording.getTitle() : "Untitled";
        recordingTitle.setText(title);

        // Display date from database
        String date = recording.getDate() != null ? recording.getDate() : "Unknown";
        recordingDate.setText(date);

        Log.d(TAG, "✅ [" + position + "] Item: " + title + " (" + date + ")");

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
}

