package com.example.smartstudybuddy2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends ArrayAdapter<SearchResult> implements Filterable {

    private List<SearchResult> results;
    private List<SearchResult> filteredResults;
    private SearchFilter searchFilter;

    public SearchResultAdapter(Context context, int resource, List<SearchResult> results) {
        super(context, resource, results);
        this.results = new ArrayList<>(results);
        this.filteredResults = new ArrayList<>(results);
    }

    @Override
    public int getCount() {
        return filteredResults.size();
    }

    @Nullable
    @Override
    public SearchResult getItem(int position) {
        return filteredResults.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search_result, parent, false);
        }

        SearchResult result = getItem(position);
        if (result != null) {
            ImageView ivTypeIcon = convertView.findViewById(R.id.ivTypeIcon);
            TextView tvResultTitle = convertView.findViewById(R.id.tvResultTitle);
            TextView tvResultType = convertView.findViewById(R.id.tvResultType);
            TextView tvResultPreview = convertView.findViewById(R.id.tvResultPreview);
            TextView tvResultTimestamp = convertView.findViewById(R.id.tvResultTimestamp);

            tvResultTitle.setText(result.getTitle());
            tvResultType.setText(result.getTypeLabel());
            tvResultPreview.setText(result.getContentPreview());
            tvResultTimestamp.setText(result.getTimestamp());

            // Set icon based on type
            switch (result.getType()) {
                case SearchResult.TYPE_RECORDING:
                    ivTypeIcon.setImageResource(R.drawable.ic_mic);
                    break;
                case SearchResult.TYPE_NOTE:
                    ivTypeIcon.setImageResource(R.drawable.ic_note);
                    break;
                case SearchResult.TYPE_FLASHCARD:
                    ivTypeIcon.setImageResource(R.drawable.ic_flashcard);
                    break;
                case SearchResult.TYPE_QUIZ:
                    ivTypeIcon.setImageResource(R.drawable.ic_quiz);
                    break;
            }
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (searchFilter == null) {
            searchFilter = new SearchFilter();
        }
        return searchFilter;
    }

    private class SearchFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<SearchResult> filtered = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(SearchResultAdapter.this.results);
            } else {
                String query = constraint.toString().toLowerCase().trim();
                for (SearchResult result : SearchResultAdapter.this.results) {
                    if (result.getTitle().toLowerCase().contains(query) ||
                        result.getContentPreview().toLowerCase().contains(query) ||
                        result.getTypeLabel().toLowerCase().contains(query)) {
                        filtered.add(result);
                    }
                }
            }

            results.values = filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredResults = (List<SearchResult>) results.values;
            notifyDataSetChanged();
        }
    }
}
