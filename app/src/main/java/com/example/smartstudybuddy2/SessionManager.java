package com.example.smartstudybuddy2;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SmartStudyBuddySession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role"; // new key

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Create login session with role
    public void createLoginSession(String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role); // save role
        editor.apply();
    }

    // Check login state
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get stored email
    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // ✅ ADDED (sirf yeh naya method)
    // Helper method for currently logged-in email
    public String getLoggedInEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Get stored role
    public String getUserRole() {
        return prefs.getString(KEY_ROLE, "user"); // default user
    }

    // 1️⃣ Add a key for user name
    private static final String KEY_NAME = "name";

    // 2️⃣ Update createLoginSession to save name too
    public void createLoginSession(String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_NAME, name);      // save name
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);      // save role
        editor.apply();
    }

    // 3️⃣ Add getter for user name
    public String getUserName() {
        return prefs.getString(KEY_NAME, "User");
    }

    /* =======================
       🔹 PROFILE INFORMATION
       ======================= */

    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_UNIVERSITY = "profile_university";
    private static final String KEY_COURSE = "profile_course";
    private static final String KEY_SEMESTER = "profile_semester";
    private static final String KEY_PREFERENCE = "profile_preference";
    private static final String KEY_DAILY_GOAL = "profile_daily_goal";
    private static final String KEY_LANGUAGE = "profile_language";

    // Save profile
    public void saveUserProfile(String name,
                                String university,
                                String course,
                                String semester,
                                String preference,
                                String dailyGoal,
                                String language) {

        editor.putString(KEY_PROFILE_NAME, name);
        editor.putString(KEY_UNIVERSITY, university);
        editor.putString(KEY_COURSE, course);
        editor.putString(KEY_SEMESTER, semester);
        editor.putString(KEY_PREFERENCE, preference);
        editor.putString(KEY_DAILY_GOAL, dailyGoal);
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public String getProfileName() {
        return prefs.getString(KEY_PROFILE_NAME, "");
    }

    public String getProfileUniversity() {
        return prefs.getString(KEY_UNIVERSITY, "");
    }

    public String getProfileCourse() {
        return prefs.getString(KEY_COURSE, "");
    }

    public String getProfileSemester() {
        return prefs.getString(KEY_SEMESTER, "");
    }

    public String getProfilePreference() {
        return prefs.getString(KEY_PREFERENCE, "Both");
    }

    public String getProfileDailyGoal() {
        return prefs.getString(KEY_DAILY_GOAL, "");
    }

    public String getProfileLanguage() {
        return prefs.getString(KEY_LANGUAGE, "English");
    }

    // Logout user
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
