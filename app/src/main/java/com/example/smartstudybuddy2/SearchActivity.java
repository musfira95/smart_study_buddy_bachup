package com.example.smartstudybuddy2;

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

public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    ListView listView;
    String[] dummyNotes = {
            "Physics Chapter 1 Notes",
            "Math Lecture 2 Summary",
            "Chemistry Quiz 1",
            "History Notes",
            "Biology Flashcards"
    };

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);

        // Custom adapter using grey text layout
        adapter = new ArrayAdapter<>(this, R.layout.list_item_note, R.id.textItem, dummyNotes);
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
}
