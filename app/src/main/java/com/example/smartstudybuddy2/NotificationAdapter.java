package com.example.smartstudybuddy2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import java.util.ArrayList;

public class NotificationAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> list;

    public NotificationAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.notification_row, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.imgIcon);
        TextView text = convertView.findViewById(R.id.tvNotificationText);

        String full = list.get(position);
        int newline = full.indexOf('\n');
        if (newline > 0) {
            SpannableString ss = new SpannableString(full);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, newline, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(ss);
        } else {
            text.setText(full);
        }

        return convertView;
    }
}
