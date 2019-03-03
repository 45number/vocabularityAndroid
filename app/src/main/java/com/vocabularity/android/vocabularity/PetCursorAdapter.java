package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils;

import com.vocabularity.android.vocabularity.data.PetContract;

import java.io.File;

public class PetCursorAdapter extends CursorAdapter {



    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        LinearLayout markedBadge = view.findViewById(R.id.markedBadge);
        markedBadge.setVisibility(View.GONE);


        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.folderImage);

        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_FOLDER_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_IMAGE);
        int summaryColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_STATISTICS);
        int markedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_MARKED);

        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);
        String folderSummary = cursor.getString(summaryColumnIndex);
        int folderMarked = cursor.getInt(markedColumnIndex);

//        String lastCharacters = petBreed.charAt(petBreed.length() - 4);
//        Integer symbolsInImageName = petBreed.length();
//        Log.e("last symbols are ", ": " + symbolsInImageName);


        String lastCharacters = getLastCharacters(petBreed, 4);
//        Log.e("last symbols are ", "--" + lastCharacters + "--");

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(petBreed)) {
            petBreed = context.getString(R.string.unknown_breed);
            pictureImageView.setImageResource(R.drawable.ic_add_folder_image);
        } else if ( !".png".equals(lastCharacters) ) {
            pictureImageView.setImageResource(R.drawable.ic_deck);
        } else {
            try {
                ContextWrapper cw = new ContextWrapper(context);
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File mypath=new File(directory,petBreed);
                pictureImageView.setImageDrawable(Drawable.createFromPath(mypath.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (folderMarked == 1) {
            markedBadge.setVisibility(View.VISIBLE);
        }


        nameTextView.setText(petName);
        summaryTextView.setText(folderSummary);
    }


    private String getLastCharacters(String word, int number) {
        if (word != null) {
            if (word.length() == number) {
                return word;
            } else if (word.length() > number) {
                return word.substring(word.length() - number);
            } else {
                return "no image";
            }
        } else {
            return "image is null";
        }
    }


}