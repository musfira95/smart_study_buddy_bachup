# Next Admin Screens - Code Templates

## 1. AdminRecordingsActivity.java

```java
package com.example.smartstudybuddy2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

public class AdminRecordingsActivity extends AppCompatActivity {
    
    RecyclerView recordingsRecycler;
    TextView tvEmpty;
    DatabaseHelper dbHelper;
    ArrayList<RecordingModel> recordingsList;
    AdminRecordingsAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_recordings);
        
        dbHelper = new DatabaseHelper(this);
        recordingsRecycler = findViewById(R.id.recordingsRecycler);
        tvEmpty = findViewById(R.id.tvEmptyRecordings);
        
        loadRecordings();
    }
    
    private void loadRecordings() {
        recordingsList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllRecordings();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String filename = cursor.getString(1);
                String uploader = cursor.getString(2);
                String uploadDate = cursor.getString(3);
                String duration = cursor.getString(4);
                String status = cursor.getString(5);
                
                recordingsList.add(new RecordingModel(id, filename, uploader, uploadDate, duration, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        if (recordingsList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recordingsRecycler.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recordingsRecycler.setVisibility(View.VISIBLE);
            
            adapter = new AdminRecordingsAdapter(this, recordingsList, dbHelper);
            recordingsRecycler.setLayoutManager(new LinearLayoutManager(this));
            recordingsRecycler.setAdapter(adapter);
        }
    }
}
```

## 2. activity_admin_recordings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@android:color/white">
    
    <!-- HEADER -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:background="#4F6F64"
        android:gravity="center_vertical"
        android:paddingHorizontal="16dp"
        android:elevation="4dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Manage Recordings"
            android:textColor="@android:color/white"
            android:textSize="20sp"
            android:textStyle="bold" />
    </LinearLayout>
    
    <!-- EMPTY STATE -->
    <TextView
        android:id="@+id/tvEmptyRecordings"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:text="No recordings found"
        android:textSize="16sp"
        android:textColor="#777777"
        android:visibility="gone" />
    
    <!-- RECORDINGS LIST -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recordingsRecycler"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="8dp" />
</LinearLayout>
```

## 3. item_recording_admin.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">
        
        <!-- FILENAME -->
        <TextView
            android:id="@+id/filenameText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Recording.mp3"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#333333" />
        
        <!-- UPLOADER -->
        <TextView
            android:id="@+id/uploaderText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Uploader: user@email.com"
            android:textSize="13sp"
            android:textColor="#777777"
            android:layout_marginTop="4dp" />
        
        <!-- DATE -->
        <TextView
            android:id="@+id/dateText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Date: 15-02-2026"
            android:textSize="13sp"
            android:textColor="#777777"
            android:layout_marginTop="2dp" />
        
        <!-- STATUS -->
        <TextView
            android:id="@+id/statusText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Status: Pending"
            android:textSize="13sp"
            android:textColor="#FF6F00"
            android:layout_marginTop="2dp"
            android:layout_marginBottom="8dp" />
        
        <!-- DIVIDER -->
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#EEEEEE"
            android:layout_marginVertical="8dp" />
        
        <!-- BUTTONS -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="end"
            android:spacing="8dp">
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/approveBtn"
                android:layout_width="wrap_content"
                android:layout_height="36dp"
                android:text="Approve"
                android:textSize="12sp"
                app:backgroundTint="#43A047"
                android:paddingHorizontal="12dp" />
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/rejectBtn"
                android:layout_width="wrap_content"
                android:layout_height="36dp"
                android:text="Reject"
                android:textSize="12sp"
                app:backgroundTint="#E53935"
                android:paddingHorizontal="12dp" />
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/deleteBtn"
                android:layout_width="wrap_content"
                android:layout_height="36dp"
                android:text="Delete"
                android:textSize="12sp"
                style="@style/Widget.MaterialComponents.Button.OutlinedButton"
                android:paddingHorizontal="12dp" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

## 4. AdminRecordingsAdapter.java

```java
package com.example.smartstudybuddy2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.util.ArrayList;

public class AdminRecordingsAdapter extends RecyclerView.Adapter<AdminRecordingsAdapter.RecordingViewHolder> {
    
    Context context;
    ArrayList<RecordingModel> recordingsList;
    DatabaseHelper dbHelper;
    
    public AdminRecordingsAdapter(Context context, ArrayList<RecordingModel> recordingsList, DatabaseHelper dbHelper) {
        this.context = context;
        this.recordingsList = recordingsList;
        this.dbHelper = dbHelper;
    }
    
    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecordingViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recording_admin, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        RecordingModel recording = recordingsList.get(position);
        
        holder.filenameText.setText(recording.getFilename());
        holder.uploaderText.setText("Uploader: " + recording.getUploader());
        holder.dateText.setText("Date: " + recording.getUploadDate());
        holder.statusText.setText("Status: " + recording.getStatus());
        
        // Update status color
        if (recording.getStatus().equals("pending")) {
            holder.statusText.setTextColor(0xFFFF6F00);
        } else if (recording.getStatus().equals("approved")) {
            holder.statusText.setTextColor(0xFF43A047);
        } else {
            holder.statusText.setTextColor(0xFFE53935);
        }
        
        holder.approveBtn.setOnClickListener(v -> {
            if (dbHelper.approveRecording(recording.getId())) {
                recording.setStatus("approved");
                notifyItemChanged(position);
                Toast.makeText(context, "Recording approved", Toast.LENGTH_SHORT).show();
            }
        });
        
        holder.rejectBtn.setOnClickListener(v -> {
            if (dbHelper.rejectRecording(recording.getId())) {
                recording.setStatus("rejected");
                notifyItemChanged(position);
                Toast.makeText(context, "Recording rejected", Toast.LENGTH_SHORT).show();
            }
        });
        
        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Recording")
                    .setMessage("Delete " + recording.getFilename() + "?")
                    .setPositiveButton("Yes", (d, w) -> {
                        if (dbHelper.deleteRecording(recording.getId())) {
                            recordingsList.remove(position);
                            notifyItemRemoved(position);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
    
    @Override
    public int getItemCount() {
        return recordingsList.size();
    }
    
    static class RecordingViewHolder extends RecyclerView.ViewHolder {
        TextView filenameText, uploaderText, dateText, statusText;
        MaterialButton approveBtn, rejectBtn, deleteBtn;
        
        RecordingViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            filenameText = itemView.findViewById(R.id.filenameText);
            uploaderText = itemView.findViewById(R.id.uploaderText);
            dateText = itemView.findViewById(R.id.dateText);
            statusText = itemView.findViewById(R.id.statusText);
            approveBtn = itemView.findViewById(R.id.approveBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
```

## 5. RecordingModel.java

```java
package com.example.smartstudybuddy2;

public class RecordingModel {
    String id, filename, uploader, uploadDate, duration, status;
    
    public RecordingModel(String id, String filename, String uploader, String uploadDate, String duration, String status) {
        this.id = id;
        this.filename = filename;
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.duration = duration;
        this.status = status;
    }
    
    public String getId() { return id; }
    public String getFilename() { return filename; }
    public String getUploader() { return uploader; }
    public String getUploadDate() { return uploadDate; }
    public String getDuration() { return duration; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }
}
```

## Adding to AndroidManifest.xml

Add this line to AndroidManifest.xml:
```xml
<activity android:name=".AdminRecordingsActivity" />
```

## Adding to Navigation Drawer

In `activity_admin_dashboard.xml` or drawer menu, add:
```xml
<item
    android:id="@+id/nav_recordings"
    android:title="Manage Recordings" />
```

Then in `AdminDashboardActivity.java`:
```java
else if (id == R.id.nav_recordings) {
    startActivity(new Intent(this, AdminRecordingsActivity.class));
}
```

---

**Same pattern applies for:**
- AdminUploadsActivity
- AdminNotesActivity
- ExportActivity (enhanced)

Just replace "Recording" with "Upload" or "Note" and adjust the database calls.
