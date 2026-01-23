package com.example.smartstudybuddy2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TranscriptionAdapter extends RecyclerView.Adapter<TranscriptionAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Recording> transcriptions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recording recording);
    }

    public TranscriptionAdapter(Context context, ArrayList<Recording> transcriptions, OnItemClickListener listener) {
        this.context = context;
        this.transcriptions = transcriptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transcription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recording recording = transcriptions.get(position);
        holder.tvTitle.setText(recording.getTitle());
        holder.tvDate.setText(recording.getDate());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(recording));
    }

    @Override
    public int getItemCount() {
        return transcriptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTranscriptionTitle);
            tvDate = itemView.findViewById(R.id.tvTranscriptionDate);
        }
    }
}
