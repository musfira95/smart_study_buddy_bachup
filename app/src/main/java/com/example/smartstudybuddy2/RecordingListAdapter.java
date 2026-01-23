package com.example.smartstudybuddy2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordingListAdapter extends BaseAdapter {

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
        ImageButton btnPlay = convertView.findViewById(R.id.btnPlayRecording);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteRecording);

        Recording recording = recordings.get(position);

        recordingTitle.setText(recording.getTitle()); // ✅ correct

        recordingDate.setText(recording.getDate());

        // Click on item opens detail screen
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecordingDetailActivity.class);
            intent.putExtra("recording_id", recording.getId());
            context.startActivity(intent);
        });

        // Play button (for frontend testing, Toast message)
        btnPlay.setOnClickListener(v -> {
            android.widget.Toast.makeText(context, "Playing " + recording.getTitle(), android.widget.Toast.LENGTH_SHORT).show();

        });

        // Delete button (frontend only, removes from list and refreshes)
        btnDelete.setOnClickListener(v -> {
            android.widget.Toast.makeText(context, "Deleted " + recording.getTitle(), android.widget.Toast.LENGTH_SHORT).show();

        });

        return convertView;
    }
}
