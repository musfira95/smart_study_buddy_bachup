package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "🚀 SplashActivity.onCreate() started");

        // Navigate after delay using Handler with Looper.getMainLooper()
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "⏰ Splash delay complete, navigating...");
            navigateToNextScreen();
        }, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        try {
            Log.d(TAG, "🔄 Starting navigation flow...");
            
            // Try to get session - wrap in try-catch
            SessionManager session = null;
            try {
                session = new SessionManager(this);
                Log.d(TAG, "✅ SessionManager created");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error creating SessionManager: " + e.getMessage());
                e.printStackTrace();
                // Go to login as fallback
                goToLogin();
                return;
            }

            // Check if logged in
            boolean isLoggedIn = false;
            try {
                isLoggedIn = session.isLoggedIn();
                Log.d(TAG, "📋 isLoggedIn: " + isLoggedIn);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error checking login status: " + e.getMessage());
                e.printStackTrace();
                goToLogin();
                return;
            }

            // Navigate based on login status
            if (isLoggedIn) {
                try {
                    String role = session.getUserRole();
                    Log.d(TAG, "👤 User role: " + role);
                    
                    if ("admin".equals(role)) {
                        Log.d(TAG, "🔄 Going to AdminDashboardActivity");
                        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("email", session.getUserEmail());
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "🔄 Going to DashboardActivity");
                        Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                        intent.putExtra("email", session.getUserEmail());
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error navigating after login: " + e.getMessage());
                    e.printStackTrace();
                    goToLogin();
                    return;
                }
            } else {
                goToLogin();
            }
            
            // Finish after navigation
            Log.d(TAG, "✅ Navigation complete, finishing SplashActivity");
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR in navigateToNextScreen: " + e.getMessage());
            e.printStackTrace();
            goToLogin();
        }
    }

    private void goToLogin() {
        try {
            Log.d(TAG, "🔄 Navigating to LoginActivity");
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL: Cannot start LoginActivity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
