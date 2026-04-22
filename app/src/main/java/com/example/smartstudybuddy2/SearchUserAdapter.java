package com.example.smartstudybuddy2;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {

    Context context;
    ArrayList<UserModel> userList;
    String searchQuery = "";

    public SearchUserAdapter(Context context, ArrayList<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    // Call this whenever text changes
    public void updateSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.usernameText.setText(highlightText(user.getUsername(), searchQuery));
        holder.emailText.setText(highlightText(user.getEmail(), searchQuery));

        // Avatar initial
        String initial = user.getUsername() != null && !user.getUsername().isEmpty()
                ? String.valueOf(user.getUsername().charAt(0)).toUpperCase() : "U";
        if (holder.tvAvatar != null) holder.tvAvatar.setText(initial);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private Spannable highlightText(String fullText, String query) {
        Spannable spannable = new SpannableString(fullText);

        if (query.isEmpty()) return spannable;

        int start = fullText.toLowerCase().indexOf(query);
        if (start >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#4F6F64")),  // Green highlight
                    start,
                    start + query.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return spannable;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, tvAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.searchUsernameText);
            emailText = itemView.findViewById(R.id.searchEmailText);
            tvAvatar = itemView.findViewById(R.id.tvSearchAvatar);
        }
    }
}