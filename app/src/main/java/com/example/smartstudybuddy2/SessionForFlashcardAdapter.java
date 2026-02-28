package com.example.smartstudybuddy2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * ✅ Adapter for selecting study sessions for flashcard creation
 * Displays session title and creation date in a list
 */
public class SessionForFlashcardAdapter extends RecyclerView.Adapter<SessionForFlashcardAdapter.SessionViewHolder> {
    
    private ArrayList<StudySession> sessionList;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(StudySession session);
    }

    public SessionForFlashcardAdapter(ArrayList<StudySession> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_for_flashcard, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        StudySession session = sessionList.get(position);
        
        // Session title
        holder.tvSessionTitle.setText(session.getTitle());
        
        // Creation date
        holder.tvSessionDate.setText(session.getCreatedDate());
        
        // Optional: Show additional metadata
        String metadata = String.format("⏱ %ds | 📝 %d words",
                session.getDuration(),
                session.getWordCount());
        holder.tvSessionMetadata.setText(metadata);
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    /**
     * ViewHolder for session item
     */
    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle;
        TextView tvSessionDate;
        TextView tvSessionMetadata;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvSessionDate = itemView.findViewById(R.id.tvSessionDate);
            tvSessionMetadata = itemView.findViewById(R.id.tvSessionMetadata);
        }
    }
}
