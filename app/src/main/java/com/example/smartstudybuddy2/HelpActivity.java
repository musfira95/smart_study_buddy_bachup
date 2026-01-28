package com.example.smartstudybuddy2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvHelpDescription, tvHelpEmail, tvHelpPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        dbHelper = new DatabaseHelper(this);
        tvHelpDescription = findViewById(R.id.tvHelpDescription);
        tvHelpEmail = findViewById(R.id.tvHelpEmail);
        tvHelpPhone = findViewById(R.id.tvHelpPhone);

        // Load help content from database
        String helpContent = dbHelper.getHelpContent("getting_started");
        String email = dbHelper.getHelpSupportEmail();
        String phone = dbHelper.getHelpSupportPhone();

        // Set the content
        if (!helpContent.isEmpty()) {
            tvHelpDescription.setText(helpContent);
        }
        tvHelpEmail.setText("📧 Email: " + email);
        tvHelpPhone.setText("📞 Phone: " + phone);
    }
}
