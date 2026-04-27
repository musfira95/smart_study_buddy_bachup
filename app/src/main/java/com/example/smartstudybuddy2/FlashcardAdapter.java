package com.example.smartstudybuddy2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * ✅ Adapter for displaying flashcards with mastery levels
 * 
 * Shows:
 * - Question preview
 * - Topic
 * - Mastery level (color-coded emoji indicator)
 * - Review count
 */
public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    
    private ArrayList<Flashcard> flashcards;
    private OnFlashcardClickListener listener;

    public interface OnFlashcardClickListener {
        void onFlashcardClick(Flashcard flashcard);
    }

    public FlashcardAdapter(ArrayList<Flashcard> flashcards, OnFlashcardClickListener listener) {
        this.flashcards = flashcards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        try {
            Flashcard card = flashcards.get(position);
            
            if (card == null) {
                holder.tvQuestion.setText("Invalid card");
                return;
            }
            
            // Show full question
            String question = card.getQuestion() != null ? card.getQuestion() : "No question";
            holder.tvQuestion.setText(question);
            
            // Show answer preview in place of topic
            String answer = card.getAnswer() != null ? card.getAnswer() : "No answer";
            holder.tvTopic.setText(answer);
            
            // Open detail on click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFlashcardClick(card);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("FlashcardAdapter", "Error binding viewholder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }



    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvTopic;
        DatabaseHelper dbHelper;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvTopic = itemView.findViewById(R.id.tvTopic);
        }
    }
}
