package com.example.android.pets;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import com.example.android.pets.data.WordContract;

public class WordCursorAdapter extends CursorAdapter{

    public WordCursorAdapter(Context context, Cursor c) { super(context, c, 0 ); }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.word_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        EditText wordEditText = view.findViewById(R.id.word);
        EditText translationEditText = view.findViewById(R.id.translation);

        int wordColumnIndex = cursor.getColumnIndex(WordContract.WordEntry.COLUMN_WORD);
        int translationColumnIndex = cursor.getColumnIndex(WordContract.WordEntry.COLUMN_TRANSLATION);

        String word = cursor.getString(wordColumnIndex);
        String translation = cursor.getString(translationColumnIndex);

        wordEditText.setText(word);
        translationEditText.setText(translation);
    }
}