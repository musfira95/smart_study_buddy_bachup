package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionManager session = new SessionManager(SplashActivity.this);

                Intent intent;
                if (session.isLoggedIn()) {
                    String role = session.getUserRole(); // get role from session
                    if (role.equals("admin")) {
                        intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, DashboardActivity.class);
                    }
                    // ✅ Send logged-in email
                    intent.putExtra("email", session.getUserEmail());
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }


                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // animation
                finish();
            }
        }, SPLASH_DELAY);
    }
}
