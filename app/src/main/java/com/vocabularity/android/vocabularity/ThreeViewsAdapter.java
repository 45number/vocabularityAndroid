package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ThreeViewsAdapter extends ArrayAdapter<ThreeViewsListItem> {

    public ThreeViewsAdapter(@NonNull Context context, ArrayList<ThreeViewsListItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ThreeViewsListItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_row_words_at_time, parent, false);
        }
        // Lookup view for data population
        TextView titleTextView = convertView.findViewById(R.id.title);
        TextView descriptionTextView = convertView.findViewById(R.id.description);
        TextView valueTextView = convertView.findViewById(R.id.value);
        // Populate the data into the template view using the data object
        titleTextView.setText(item.getTitle());
        descriptionTextView.setText(item.getDescription());
        valueTextView.setText(item.getValue());
        // Return the completed view to render on screen
        return convertView;



//        return super.getView(position, convertView, parent);
    }

}
