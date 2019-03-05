package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils;

import com.vocabularity.android.vocabularity.data.FolderContract;

import java.io.File;

public class FolderCursorAdapter extends CursorAdapter {



    public FolderCursorAdapter(Context context, Cursor c) {
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

        int nameColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_FOLDER_NAME);
        int imageColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_IMAGE);
        int summaryColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_STATISTICS);
        int markedColumnIndex = cursor.getColumnIndex(FolderContract.FolderEntry.COLUMN_MARKED);

        String folderName = cursor.getString(nameColumnIndex);
        String folderImage = cursor.getString(imageColumnIndex);
        String folderSummary = cursor.getString(summaryColumnIndex);
        int folderMarked = cursor.getInt(markedColumnIndex);

//        String lastCharacters = folderImage.charAt(folderImage.length() - 4);
//        Integer symbolsInImageName = folderImage.length();
//        Log.e("last symbols are ", ": " + symbolsInImageName);


        String lastCharacters = getLastCharacters(folderImage, 4);
//        Log.e("last symbols are ", "--" + lastCharacters + "--");

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.

        /*if ( !".png".equals(lastCharacters) ) {

            pictureImageView.setImageResource(R.drawable.ic_deck);

            String deckString = view.getContext().getString(R.string.deck);//((TextView) view.findViewById(R.id.name)).getText().toString();
            deckString += " " + folderName;
            nameTextView.setText(deckString);

            String cardsInDeckString = view.getContext().getString(R.string.cards_in_deck);
            cardsInDeckString += " " + folderSummary;
            summaryTextView.setText(cardsInDeckString);

        } else {
            if (TextUtils.isEmpty(folderImage)) {
                pictureImageView.setImageResource(R.drawable.ic_add_folder_image);
            } else {
                try {
                    ContextWrapper cw = new ContextWrapper(context);
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    File mypath=new File(directory,folderImage);
                    pictureImageView.setImageDrawable(Drawable.createFromPath(mypath.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String fs;
            if ("Empty folder".equals(folderSummary)) {
                fs = view.getContext().getString(R.string.empty_folder);
            } else {
                String[] split = folderSummary.split("\\s+");
                String foldersStr = view.getContext().getString(R.string.folders);
                String decksStr = view.getContext().getString(R.string.decks);
                String cardsStr = view.getContext().getString(R.string.cards);
                fs = foldersStr + ": " + split[0] + " :: " + decksStr + ": " + split[1] + " :: " + cardsStr + ": " + split[2];
            }
            nameTextView.setText(folderName);
            summaryTextView.setText(fs);

        }*/


        if (TextUtils.isEmpty(folderImage)) {
//            folderImage = context.getString(R.string.unknown_breed);
            pictureImageView.setImageResource(R.drawable.ic_add_folder_image);

            String fs;
            if ("Empty folder".equals(folderSummary)) {
                fs = view.getContext().getString(R.string.empty_folder);
            } else {
                String[] split = folderSummary.split("\\s+");
                String foldersStr = view.getContext().getString(R.string.folders);
                String decksStr = view.getContext().getString(R.string.decks);
                String cardsStr = view.getContext().getString(R.string.cards);
                fs = foldersStr + ": " + split[0] + " :: " + decksStr + ": " + split[1] + " :: " + cardsStr + ": " + split[2];
            }
            nameTextView.setText(folderName);
            summaryTextView.setText(fs);

        } else if ( !".png".equals(lastCharacters) ) {
            pictureImageView.setImageResource(R.drawable.ic_deck);

            String deckString = view.getContext().getString(R.string.deck);//((TextView) view.findViewById(R.id.name)).getText().toString();
            deckString += " " + folderName;
            nameTextView.setText(deckString);

            String cardsInDeckString = view.getContext().getString(R.string.cards_in_deck);
            cardsInDeckString += " " + folderSummary;
            summaryTextView.setText(cardsInDeckString);

        } else {
            try {
                ContextWrapper cw = new ContextWrapper(context);
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File mypath=new File(directory, folderImage);
                pictureImageView.setImageDrawable(Drawable.createFromPath(mypath.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }




            String fs;
            if ("Empty folder".equals(folderSummary)) {
                fs = view.getContext().getString(R.string.empty_folder);
            } else {
                String[] split = folderSummary.split("\\s+");
                String foldersStr = view.getContext().getString(R.string.folders);
                String decksStr = view.getContext().getString(R.string.decks);
                String cardsStr = view.getContext().getString(R.string.cards);
                fs = foldersStr + ": " + split[0] + " :: " + decksStr + ": " + split[1] + " :: " + cardsStr + ": " + split[2];
            }
            nameTextView.setText(folderName);
            summaryTextView.setText(fs);


        }


        if (folderMarked == 1) {
            markedBadge.setVisibility(View.VISIBLE);
        }


//        nameTextView.setText(folderName);
//        summaryTextView.setText(folderSummary);
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