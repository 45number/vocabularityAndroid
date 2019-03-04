package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.vocabularity.android.vocabularity.data.FolderContract.FolderEntry;

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


        int numberColumnIndex = cursor.getColumnIndex(FolderEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_FOLDER_NAME);
        int breedColumnIndex = cursor.getColumnIndex(FolderEntry.COLUMN_IMAGE);

        String deckNumber = cursor.getString(numberColumnIndex);
        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);

        numberTextView.setText(deckNumber);
        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
    }
}
