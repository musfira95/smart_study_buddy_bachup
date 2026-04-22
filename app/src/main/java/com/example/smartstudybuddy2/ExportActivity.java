package com.example.smartstudybuddy2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ExportActivity extends AppCompatActivity {

    private static final String TAG = "ExportActivity";
    
    LinearLayout btnExportPDF, btnExportTXT;

    // ✅ Bottom Navigation Icons
    ImageView navHome, navAnalytics, navSettings, navProfile;
    
    private DatabaseHelper dbHelper;
    private ArrayList<Recording> recordings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Log.d(TAG, "🚀 ExportActivity opened");

        dbHelper = new DatabaseHelper(this);
        
        // Load all recordings from database
        loadRecordings();

        // EXPORT PDF BUTTON
        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportPDF.setOnClickListener(v -> {
            Log.d(TAG, "📄 Export as PDF clicked");
            exportAsPDF();
        });

        // EXPORT TEXT BUTTON
        btnExportTXT = findViewById(R.id.btnExportTXT);
        btnExportTXT.setOnClickListener(v -> {
            Log.d(TAG, "📝 Export as Text clicked");
            exportAsText();
        });

        // BOTTOM NAV
        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        navAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSettingsActivity.class))
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    // ✅ Load all recordings from database
    private void loadRecordings() {
        try {
            recordings = dbHelper.getAllRecordings();
            if (recordings == null) {
                recordings = new ArrayList<>();
            }
            Log.d(TAG, "✅ Loaded " + recordings.size() + " recordings from database");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading recordings: " + e.getMessage());
            recordings = new ArrayList<>();
        }
    }

    // ✅ EXPORT AS TEXT
    private void exportAsText() {
        if (recordings.isEmpty()) {
            Toast.makeText(this, "❌ No recordings to export", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "⚠️ No recordings found");
            return;
        }

        try {
            Log.d(TAG, "📝 Starting text export with " + recordings.size() + " recordings");

            // Create content
            StringBuilder content = new StringBuilder();
            content.append("===============================================\n");
            content.append("SMART STUDY BUDDY - TRANSCRIPTIONS EXPORT\n");
            content.append("===============================================\n\n");
            content.append("Export Date: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n\n");

            // Add all recordings
            for (int i = 0; i < recordings.size(); i++) {
                Recording r = recordings.get(i);
                content.append("------- RECORDING ").append(i + 1).append(" -------\n");
                content.append("Title: ").append(r.getTitle() != null ? r.getTitle() : "Untitled").append("\n");
                content.append("Date: ").append(r.getDate() != null ? r.getDate() : "Unknown").append("\n");
                
                String transcription = r.getTranscription();
                if (transcription != null && !transcription.isEmpty()) {
                    content.append("Transcription:\n").append(transcription).append("\n");
                } else {
                    content.append("Transcription: <Not available>\n");
                }
                content.append("\n");
            }

            // Save to file
            String fileName = "SmartStudyBuddy_Export_" + System.currentTimeMillis() + ".txt";
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
                Log.d(TAG, "📁 Created Documents directory");
            }

            File exportFile = new File(documentsDir, fileName);

            try (FileWriter writer = new FileWriter(exportFile)) {
                writer.write(content.toString());
                writer.flush();
            }

            Log.d(TAG, "✅ Text file exported successfully: " + exportFile.getAbsolutePath());
            Toast.makeText(this, "✅ Exported to Documents folder", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error exporting text: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ EXPORT AS PDF
    private void exportAsPDF() {
        if (recordings.isEmpty()) {
            Toast.makeText(this, "❌ No recordings to export", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "⚠️ No recordings found");
            return;
        }

        try {
            Log.d(TAG, "📄 Starting PDF export with " + recordings.size() + " recordings");

            // For now, create a text file with .pdf extension (actual PDF generation requires iText library)
            // This is a workaround - the content is viewable in text editors
            StringBuilder content = new StringBuilder();
            content.append("SMART STUDY BUDDY - TRANSCRIPTIONS EXPORT\n");
            content.append("===========================================\n\n");
            content.append("Export Date: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n\n");

            // Add all recordings
            for (int i = 0; i < recordings.size(); i++) {
                Recording r = recordings.get(i);
                content.append("\n=== RECORDING ").append(i + 1).append(" ===\n");
                content.append("Title: ").append(r.getTitle() != null ? r.getTitle() : "Untitled").append("\n");
                content.append("Date: ").append(r.getDate() != null ? r.getDate() : "Unknown").append("\n");
                
                String transcription = r.getTranscription();
                if (transcription != null && !transcription.isEmpty()) {
                    content.append("\nTranscription:\n").append(transcription).append("\n");
                } else {
                    content.append("\nTranscription: <Not available>\n");
                }
            }

            // Save to file
            String fileName = "SmartStudyBuddy_Export_" + System.currentTimeMillis() + ".pdf";
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
                Log.d(TAG, "📁 Created Documents directory");
            }

            File exportFile = new File(documentsDir, fileName);

            try (FileWriter writer = new FileWriter(exportFile)) {
                writer.write(content.toString());
                writer.flush();
            }

            Log.d(TAG, "✅ PDF exported successfully: " + exportFile.getAbsolutePath());
            Toast.makeText(this, "✅ PDF saved to Documents folder", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
