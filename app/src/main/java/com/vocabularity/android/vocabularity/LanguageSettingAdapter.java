package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class LanguageSettingAdapter extends ArrayAdapter<LanguageSettingListItem> {
    public LanguageSettingAdapter(@NonNull Context context, ArrayList<LanguageSettingListItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LanguageSettingListItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_row, parent, false);
        }
        // Lookup view for data population
        TextView titleTextView = convertView.findViewById(R.id.title_text_view);
        Switch isLearningSwitch = convertView.findViewById(R.id.language_switch);

        // Populate the data into the template view using the data object
        titleTextView.setText(item.getName());
        isLearningSwitch.setChecked(item.getIsLearning());


        // Return the completed view to render on screen
        return convertView;

//        return super.getView(position, convertView, parent);
    }
}
