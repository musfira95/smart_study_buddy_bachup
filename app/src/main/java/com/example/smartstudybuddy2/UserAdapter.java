package com.example.smartstudybuddy2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    Context context;
    ArrayList<UserModel> usersList;
    DatabaseHelper dbHelper;

    ArrayList<UserModel> fullList;

    public UserAdapter(Context context, ArrayList<UserModel> usersList, DatabaseHelper dbHelper) {
        this.context = context;
        this.usersList = new ArrayList<>(usersList);
        this.fullList = new ArrayList<>(usersList);
        this.dbHelper = dbHelper;
    }

    public void filter(String query) {
        usersList.clear();
        if (query == null || query.trim().isEmpty()) {
            usersList.addAll(fullList);
        } else {
            String q = query.toLowerCase().trim();
            for (UserModel u : fullList) {
                if ((u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                    usersList.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return usersList.size();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = usersList.get(position);

        holder.usernameText.setText(user.getUsername());
        holder.emailText.setText(user.getEmail());
        holder.roleText.setText(user.getRole());

        // Avatar initial
        String initial = user.getUsername() != null && !user.getUsername().isEmpty()
                ? String.valueOf(user.getUsername().charAt(0)).toUpperCase() : "U";
        if (holder.tvAvatar != null) holder.tvAvatar.setText(initial);

        // Audio stats
        int[] stats = dbHelper.getAudioStatsForUser(user.getEmail());
        if (holder.tvAudioCount != null)
            holder.tvAudioCount.setText(stats[0] + (stats[0] == 1 ? " audio" : " audios"));
        if (holder.tvRecordedLabel != null)
            holder.tvRecordedLabel.setText("recorded: " + stats[1]);
        if (holder.tvUploadedLabel != null)
            holder.tvUploadedLabel.setText("uploaded: " + stats[2]);

        // Blocked badge
        boolean isBlocked = user.getIsBlocked() == 1;
        if (holder.tvBlockedBadge != null)
            holder.tvBlockedBadge.setVisibility(isBlocked ? android.view.View.VISIBLE : android.view.View.GONE);
        if (holder.blockBtn != null) {
            holder.blockBtn.setText(isBlocked ? "Unblock" : "Block");
            holder.blockBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(isBlocked ? "#4F6F64" : "#9E9E9E")));
        }

        if (holder.blockBtn != null) holder.blockBtn.setOnClickListener(v -> {
            int newStatus = user.getIsBlocked() == 1 ? 0 : 1;

            if (dbHelper.updateBlockStatus(user.getEmail(), newStatus)) {
                user.setIsBlocked(newStatus);
                notifyItemChanged(holder.getAdapterPosition());

                Toast.makeText(context,
                        newStatus == 1 ? "User Blocked" : "User Unblocked",
                        Toast.LENGTH_SHORT).show();
            }
        });

        if (holder.deleteBtn != null) holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context, R.style.AppDialog)
                    .setTitle("Delete User")
                    .setMessage("Delete " + user.getUsername() + "?")
                    .setPositiveButton("Yes", (d, w) -> {

                        if (user.getRole().equals("admin") && dbHelper.isLastAdmin(user.getEmail())) {
                            Toast.makeText(context, "Cannot delete last admin", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (dbHelper.deleteUser(user.getEmail())) {
                            usersList.remove(position);
                            notifyItemRemoved(position);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        if (holder.manageRoleBtn != null) holder.manageRoleBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, RoleManagementActivity.class);
            intent.putExtra("email", user.getEmail());
            intent.putExtra("role", user.getRole());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, roleText, tvAvatar, tvBlockedBadge;
        TextView tvAudioCount, tvRecordedLabel, tvUploadedLabel;
        Button deleteBtn, manageRoleBtn, blockBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvBlockedBadge = itemView.findViewById(R.id.tvBlockedBadge);
            tvAudioCount = itemView.findViewById(R.id.tvAudioCount);
            tvRecordedLabel = itemView.findViewById(R.id.tvRecordedLabel);
            tvUploadedLabel = itemView.findViewById(R.id.tvUploadedLabel);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            manageRoleBtn = itemView.findViewById(R.id.manageRoleBtn);
            blockBtn = itemView.findViewById(R.id.blockBtn);
        }
    }
}