package com.example.smartstudybuddy2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

        // --- Edit Button ---
        holder.editBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_user, null);
            builder.setView(view);

            EditText updateUsername = view.findViewById(R.id.updateUsername);
            EditText updateEmail = view.findViewById(R.id.updateEmail);
            EditText updatePassword = view.findViewById(R.id.updatePassword);
            Button updateButton = view.findViewById(R.id.updateButton);

            updateUsername.setText(user.getUsername());
            updateEmail.setText(user.getEmail());
            updatePassword.setText(user.getPassword());

            AlertDialog dialog = builder.create();
            dialog.show();

            updateButton.setOnClickListener(btn -> {
                String newUsername = updateUsername.getText().toString().trim();
                String newEmail = updateEmail.getText().toString().trim();
                String newPassword = updatePassword.getText().toString().trim();

                if (newUsername.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean updated = dbHelper.updateUser(user.getEmail(), newEmail, newUsername, newPassword, user.getRole());
                if (updated) {
                    Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show();
                    user.setUsername(newUsername);
                    user.setEmail(newEmail);
                    user.setPassword(newPassword);
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });

        // --- Delete Button ---
        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteUser(user.getEmail());
                        if (deleted) {
                            usersList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, roleText;
        Button editBtn, deleteBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
