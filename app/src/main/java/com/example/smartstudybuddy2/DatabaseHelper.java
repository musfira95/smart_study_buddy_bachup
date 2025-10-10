package com.example.smartstudybuddy2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SmartStudyBuddy.db";
    private static final int DB_VERSION = 2; // updated version

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ✅ Create Users table with role column
        db.execSQL("CREATE TABLE Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "username TEXT, " +
                "password TEXT, " +
                "role TEXT DEFAULT 'user')");

        // ✅ Default admin account
        db.execSQL("INSERT INTO Users (email, username, password, role) " +
                "VALUES ('musfira@gmail.com', 'Musfira', '123456', 'admin')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table and recreate
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    // ✅ Insert new user with role
    public boolean insertUser(String email, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if user already exists
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email=?", new String[]{email});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);

        long result = db.insert("Users", null, cv);
        db.close();
        return result != -1;
    }

    // ✅ Check login credentials
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email=? AND password=?", new String[]{email, password});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // ✅ Get user role
    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM Users WHERE email=?", new String[]{email});
        String role = "user"; // default
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return role;
    }

    // ✅ Check if email already exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Users WHERE email=?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // ✅ Update password
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        int rows = db.update("Users", cv, "email=?", new String[]{email});
        db.close();
        return rows > 0;
    }

    // ✅ Get all users (for Admin Dashboard)
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT username, email, role, password FROM Users", null);

    }

    // ✅ Update user role (Make Admin)
    public boolean updateUserRole(String email, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("role", newRole);
        int rows = db.update("Users", cv, "email=?", new String[]{email});
        db.close();
        return rows > 0;
    }
    // Update user (email can change too)
    public boolean updateUser(String oldEmail, String newEmail, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", newEmail);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        int rows = db.update("Users", cv, "email=?", new String[]{oldEmail});
        return rows > 0;
    }

    // Delete user by email
    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("Users", "email=?", new String[]{email});
        return rows > 0;
    }

}
