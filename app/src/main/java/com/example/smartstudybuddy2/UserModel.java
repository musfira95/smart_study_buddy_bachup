package com.example.smartstudybuddy2;

public class UserModel {
    private String username, email, role, password;
    private int isBlocked;

    public UserModel(String username, String email, String role, String password, int isBlocked) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        this.isBlocked = isBlocked;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public int getIsBlocked() { return isBlocked; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setPassword(String password) { this.password = password; }
    public void setIsBlocked(int isBlocked) { this.isBlocked = isBlocked; }
}