package com.example.smartstudybuddy2;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    ArrayList<ReminderModel> list;

    public ReminderAdapter(ArrayList<ReminderModel> list) {
        this.list = list;
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
        ReminderModel model = list.get(position);
        holder.title.setText(model.getTitle());
        holder.time.setText(model.getTime());

        holder.delete.setOnClickListener(v -> {
            list.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;
        ImageView delete;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvReminderTitle);
            time = itemView.findViewById(R.id.tvReminderTime);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
