package com.example.smartstudybuddy2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Add/Edit Reminder Activity
 * Allows users to create and modify reminders
 */
public class AddEditReminderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etTitle, etDescription, etDate, etTime;
    private Spinner spinnerFeatureType, spinnerFeatureSelection;
    private TextView tvFeatureLabel;
    private Button btnCancel, btnSave;
    private android.widget.ImageView btnBack;

    private DatabaseHelper dbHelper;
    private ScheduleReminder editingReminder;
    private int reminderId = -1;  // -1 means creating new

    private String selectedFeatureType = null;
    private int selectedFeatureId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_add_edit_reminder);

            // Initialize views
            toolbar = findViewById(R.id.toolbar);
            etTitle = findViewById(R.id.etTitle);
            etDescription = findViewById(R.id.etDescription);
            etDate = findViewById(R.id.etDate);
            etTime = findViewById(R.id.etTime);
            spinnerFeatureType = findViewById(R.id.spinnerFeatureType);
            spinnerFeatureSelection = findViewById(R.id.spinnerFeatureSelection);
            tvFeatureLabel = findViewById(R.id.tvFeatureLabel);
            btnCancel = findViewById(R.id.btnCancel);
            btnSave = findViewById(R.id.btnSave);
            btnBack = findViewById(R.id.btnBack);

            // Validate all views are found
            if (etTitle == null || btnSave == null) {
                throw new RuntimeException("Failed to inflate layout - missing views");
            }

            // Setup toolbar if present
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
            }

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            if (dbHelper == null) {
                throw new RuntimeException("Failed to initialize database helper");
            }

            //Check if editing
            reminderId = getIntent().getIntExtra("REMINDER_ID", -1);
            if (reminderId > 0) {
                loadReminderForEditing();
            } else {
                // Set today's date as default
                setDefaultDate();
            }

            // Setup feature type spinner
            setupFeatureTypeSpinner();

            // Setup date/time pickers
            setupDateTimePickers();

            // Button listeners
            btnCancel.setOnClickListener(v -> finish());
            btnSave.setOnClickListener(v -> saveReminder());
            if (btnBack != null) {
                btnBack.setColorFilter(android.graphics.Color.parseColor("#4F6F64"));
                btnBack.setOnClickListener(v -> finish());
            }

            android.util.Log.d("AddEditReminder", "✅ Activity initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("AddEditReminder", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading form: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Load reminder data for editing
     */
    private void loadReminderForEditing() {
        try {
            editingReminder = dbHelper.getScheduleReminderById(reminderId);
            if (editingReminder == null) {
                Toast.makeText(this, "Reminder not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Update toolbar if present
            if (toolbar != null) toolbar.setTitle("Edit Reminder");
            
            // Set form fields with reminder data
            if (etTitle != null) etTitle.setText(editingReminder.getTitle());
            if (etDescription != null) etDescription.setText(editingReminder.getDescription());
            if (etDate != null) etDate.setText(editingReminder.getDate());
            if (etTime != null) etTime.setText(editingReminder.getTime());

            selectedFeatureType = editingReminder.getFeatureType();
            selectedFeatureId = editingReminder.getFeatureId();
            
            android.util.Log.d("AddEditReminder", "✅ Loaded reminder: " + editingReminder.getTitle() + " | Type: " + selectedFeatureType);
        } catch (Exception e) {
            android.util.Log.e("AddEditReminder", "Error loading reminder: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Set today's date as default
     */
    private void setDefaultDate() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etDate.setText(today);
    }

    /**
     * Setup feature type spinner
     */
    private void setupFeatureTypeSpinner() {
        ArrayList<String> featureTypes = new ArrayList<>();
        featureTypes.add("Simple Reminder");
        featureTypes.add("Recording");
        featureTypes.add("Flashcard");
        featureTypes.add("Quiz");
        featureTypes.add("PDF Export");

        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, featureTypes) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    android.widget.TextView textView = (android.widget.TextView) view;
                    textView.setTextColor(android.graphics.Color.parseColor("#4F6F64"));
                    textView.setTextSize(16);
                    textView.setPadding(14, 14, 14, 14);
                    return view;
                }
                
                @Override
                public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getDropDownView(position, convertView, parent);
                    android.widget.TextView textView = (android.widget.TextView) view;
                    textView.setTextColor(android.graphics.Color.parseColor("#4F6F64"));
                    textView.setTextSize(15);
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFeatureType.setAdapter(adapter);

            spinnerFeatureType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    try {
                        String selected = featureTypes.get(position);
                        if (position == 0) {
                            // Simple Reminder
                            selectedFeatureType = null;  // ✅ null for simple reminder
                            selectedFeatureId = 0;
                            tvFeatureLabel.setVisibility(android.view.View.GONE);
                            spinnerFeatureSelection.setVisibility(android.view.View.GONE);
                            android.util.Log.d("AddEditReminder", "Selected: Simple Reminder");
                        } else {
                            // Convert display name to database key
                            selectedFeatureType = selected.toLowerCase().replace(" ", "_");
                            tvFeatureLabel.setVisibility(android.view.View.VISIBLE);
                            spinnerFeatureSelection.setVisibility(android.view.View.VISIBLE);
                            android.util.Log.d("AddEditReminder", "Selected feature type: " + selectedFeatureType);
                            loadFeaturesForType(selectedFeatureType);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AddEditReminder", "Error in onItemSelected: " + e.getMessage());
                        selectedFeatureType = null;
                        selectedFeatureId = 0;
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    selectedFeatureType = null;
                    selectedFeatureId = 0;
                }
            });

            // Select current type if editing
            if (editingReminder != null && editingReminder.getFeatureType() != null) {
                String currentType = editingReminder.getFeatureType();
                for (int i = 0; i < featureTypes.size(); i++) {
                    if (featureTypes.get(i).toLowerCase().replace(" ", "_").equals(currentType)) {
                        spinnerFeatureType.setSelection(i);
                        break;
                    }
                }
            } else {
                // Default to Simple Reminder for new reminders
                spinnerFeatureType.setSelection(0);
            }
        } catch (Exception e) {
            android.util.Log.e("AddEditReminder", "Error setting up feature type spinner: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up options: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load features for selected type
     */
    private void loadFeaturesForType(String featureType) {
        ArrayList<String> features = new ArrayList<>();
        selectedFeatureId = 0;  // Reset feature ID

        try {
            if (featureType.equals("recording")) {
                ArrayList<Recording> recordings = dbHelper.getAllRecordings();
                if (recordings != null && !recordings.isEmpty()) {
                    for (Recording r : recordings) {
                        if (r != null && r.getTitle() != null) {
                            features.add(r.getId() + " - " + r.getTitle());
                        }
                    }
                    tvFeatureLabel.setText("Select Recording");
                } else {
                    android.util.Log.w("AddEditReminder", "No recordings found");
                }
            } else if (featureType.equals("flashcard")) {
                ArrayList<Flashcard> flashcards = dbHelper.getAllFlashcardsAsArray();
                if (flashcards != null && !flashcards.isEmpty()) {
                    for (Flashcard f : flashcards) {
                        if (f != null && f.getQuestion() != null) {
                            features.add(f.getId() + " - " + f.getQuestion());
                        }
                    }
                    tvFeatureLabel.setText("Select Flashcard");
                } else {
                    android.util.Log.w("AddEditReminder", "No flashcards found");
                }
            } else if (featureType.equals("quiz")) {
                ArrayList<QuizQuestion> quizzes = dbHelper.getAllQuizQuestions();
                if (quizzes != null && !quizzes.isEmpty()) {
                    for (int i = 0; i < quizzes.size(); i++) {
                        QuizQuestion q = quizzes.get(i);
                        if (q != null && q.getQuestion() != null) {
                            features.add((i + 1) + " - " + q.getQuestion());
                        }
                    }
                    tvFeatureLabel.setText("Select Quiz");
                } else {
                    android.util.Log.w("AddEditReminder", "No quiz questions found");
                }
            } else if (featureType.equals("pdf_export")) {
                ArrayList<Recording> recordings = dbHelper.getAllRecordings();
                if (recordings != null && !recordings.isEmpty()) {
                    for (Recording r : recordings) {
                        if (r != null && r.getTitle() != null) {
                            features.add(r.getId() + " - " + r.getTitle());
                        }
                    }
                    tvFeatureLabel.setText("Select Recording to Export");
                } else {
                    android.util.Log.w("AddEditReminder", "No recordings found for PDF");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AddEditReminder", "Error loading features: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading features: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Always add at least one option
        if (features.isEmpty()) {
            features.add("No items available");
            selectedFeatureId = 0;
        }

        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, features) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    android.widget.TextView textView = (android.widget.TextView) view;
                    textView.setTextColor(android.graphics.Color.parseColor("#4F6F64"));
                    textView.setTextSize(15);
                    textView.setPadding(14, 14, 14, 14);
                    return view;
                }
                
                @Override
                public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View view = super.getDropDownView(position, convertView, parent);
                    android.widget.TextView textView = (android.widget.TextView) view;
                    textView.setTextColor(android.graphics.Color.parseColor("#4F6F64"));
                    textView.setTextSize(14);
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFeatureSelection.setAdapter(adapter);

            spinnerFeatureSelection.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    try {
                        if (position >= 0 && position < features.size()) {
                            String selected = features.get(position);
                            if (selected != null && !selected.equals("No items available")) {
                                String[] parts = selected.split(" - ");
                                if (parts.length > 0) {
                                    selectedFeatureId = Integer.parseInt(parts[0].trim());
                                } else {
                                    selectedFeatureId = 0;
                                }
                            } else {
                                selectedFeatureId = 0;
                            }
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        android.util.Log.e("AddEditReminder", "Error parsing feature: " + e.getMessage());
                        selectedFeatureId = 0;
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    selectedFeatureId = 0;
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AddEditReminder", "Error setting up spinner: " + e.getMessage(), e);
            Toast.makeText(this, "Spinner setup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup date and time pickers
     */
    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            String dateStr = etDate.getText().toString();
            if (!dateStr.isEmpty()) {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                calendar.setTime(date);
            }
        } catch (Exception e) {
            // Use current date if parsing fails
        }

        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }

    /**
     * Show time picker dialog
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            String timeStr = etTime.getText().toString();
            if (!timeStr.isEmpty()) {
                Date date = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeStr);
                calendar.setTime(date);
            }
        } catch (Exception e) {
            // Use current time if parsing fails
        }

        TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            etTime.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        picker.show();
    }

    /**
     * Save reminder
     */
    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date format
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time format
        if (!time.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (dbHelper == null) {
                Toast.makeText(this, "Database not initialized", Toast.LENGTH_SHORT).show();
                android.util.Log.e("AddEditReminder", "Database helper is null");
                return;
            }

            if (reminderId > 0) {
                // Update existing reminder
                android.util.Log.d("AddEditReminder", "Updating reminder ID: " + reminderId);
                boolean updated = dbHelper.updateScheduleReminder(reminderId, title, description, date, time, selectedFeatureType, selectedFeatureId);
                if (updated) {
                    Toast.makeText(this, "Reminder updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update reminder", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("AddEditReminder", "Update returned false for ID: " + reminderId);
                }
            } else {
                // Create new reminder
                android.util.Log.d("AddEditReminder", "Creating new reminder: " + title + " at " + time + " on " + date);
                long result = dbHelper.insertScheduleWithFeature(title, description, date, time, selectedFeatureType, selectedFeatureId);
                if (result > 0) {
                    Toast.makeText(this, "Reminder created successfully!", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("AddEditReminder", "✅ Reminder saved with ID: " + result);
                    setResult(RESULT_OK);
                    finish();  // ✅ Use finish() instead of starting new activity
                } else if (result == -1) {
                    Toast.makeText(this, "Failed to create reminder - database error", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("AddEditReminder", "Database insert failed, result: " + result);
                } else {
                    Toast.makeText(this, "Failed to create reminder", Toast.LENGTH_SHORT).show();
                    android.util.Log.w("AddEditReminder", "Unexpected result from insert: " + result);
                }
            }
        } catch (NullPointerException npe) {
            Toast.makeText(this, "Null pointer error: " + npe.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("AddEditReminder", "NullPointerException: ", npe);
        } catch (Exception e) {
            Toast.makeText(this, "Error saving reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("AddEditReminder", "Exception saving reminder: ", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
