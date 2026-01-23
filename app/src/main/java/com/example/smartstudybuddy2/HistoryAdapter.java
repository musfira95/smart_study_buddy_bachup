package com.example.smartstudybuddy2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private ArrayList<Object> historyList;

    public HistoryAdapter(Context context, ArrayList<Object> historyList){
        this.context = context;
        this.historyList = historyList;
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

        if(item instanceof Recording){
            Recording r = (Recording)item;
            holder.tvTitle.setText(r.getTitle());
            holder.tvDate.setText(r.getDate());
            holder.ivIcon.setImageResource(R.drawable.ic_recording);
        } else if(item instanceof StudySession){
            StudySession s = (StudySession)item;
            holder.tvTitle.setText(s.subject + " - " + s.duration);
            holder.tvDate.setText(s.date);
            holder.ivIcon.setImageResource(R.drawable.ic_study);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder{
        ImageView ivIcon;
        TextView tvTitle, tvDate;

        public HistoryViewHolder(@NonNull View itemView){
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
