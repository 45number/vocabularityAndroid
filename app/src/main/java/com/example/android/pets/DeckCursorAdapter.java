package com.example.android.pets;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.TextUtils;
import com.example.android.pets.data.PetContract.PetEntry;


import com.example.android.pets.data.PetContract;

import java.io.File;

public class DeckCursorAdapter extends CursorAdapter {

    public DeckCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView numberTextView = view.findViewById(R.id.number);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);


        int numberColumnIndex = cursor.getColumnIndex(PetEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_FOLDER_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_IMAGE);

        String deckNumber = cursor.getString(numberColumnIndex);
        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);

        numberTextView.setText(deckNumber);
        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
    }
}
