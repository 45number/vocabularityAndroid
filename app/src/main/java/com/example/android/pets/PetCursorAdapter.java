package com.example.android.pets;

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
import android.widget.TextView;
import android.text.TextUtils;

import com.example.android.pets.data.PetContract;

import java.io.File;

/**
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PetCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.folderImage);

        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_FOLDER_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_IMAGE);

        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);

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

        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
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