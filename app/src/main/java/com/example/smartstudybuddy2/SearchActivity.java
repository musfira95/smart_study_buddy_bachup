package com.example.smartstudybuddy2;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    ListView listView;
    ArrayList<String> allNotes;
    ArrayAdapter<String> adapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);

        dbHelper = new DatabaseHelper(this);
        allNotes = new ArrayList<>();

        // Load notes from database
        loadNotesFromDatabase();

        // If no notes in database, use placeholder
        if (allNotes.isEmpty()) {
            allNotes.add("No notes found. Create a note to search.");
        }

        // Custom adapter using grey text layout
        adapter = new ArrayAdapter<>(this, R.layout.list_item_note, R.id.textItem, allNotes);
        listView.setAdapter(adapter);

        // ---- MAKE HINT COLOR + TEXT COLOR GREY ----
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        TextView searchText = searchView.findViewById(id);
        searchText.setHintTextColor(Color.GRAY);
        searchText.setTextColor(Color.GRAY);

        // ---- SEARCH ICON GREY ----
        int searchIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = searchView.findViewById(searchIconId);
        searchIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        // ---- CLOSE ICON GREY ----
        int closeIconId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = searchView.findViewById(closeIconId);
        closeIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        // Filter on typing
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    // Load notes from database
    private void loadNotesFromDatabase() {
        Cursor cursor = dbHelper.getAllNotes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                allNotes.add(title);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}
