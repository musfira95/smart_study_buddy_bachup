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

    public UserAdapter(Context context, ArrayList<UserModel> usersList, DatabaseHelper dbHelper) {
        this.context = context;
        this.usersList = usersList;
        this.dbHelper = dbHelper;
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

        holder.usernameText.setText("Username: " + user.getUsername());
        holder.emailText.setText("Email: " + user.getEmail());
        holder.roleText.setText("Role: " + user.getRole());

        holder.blockBtn.setText(user.getIsBlocked() == 1 ? "Unblock" : "Block");

        holder.blockBtn.setOnClickListener(v -> {
            int newStatus = user.getIsBlocked() == 1 ? 0 : 1;

            if (dbHelper.updateBlockStatus(user.getEmail(), newStatus)) {
                user.setIsBlocked(newStatus);
                notifyItemChanged(position);

                Toast.makeText(context,
                        newStatus == 1 ? "User Blocked" : "User Unblocked",
                        Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
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

        holder.manageRoleBtn.setOnClickListener(v -> {
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
        TextView usernameText, emailText, roleText;
        Button deleteBtn, manageRoleBtn, blockBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            manageRoleBtn = itemView.findViewById(R.id.manageRoleBtn);
            blockBtn = itemView.findViewById(R.id.blockBtn);
        }
    }
}