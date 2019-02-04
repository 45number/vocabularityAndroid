package com.example.android.pets;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.widget.ListView;

import com.example.android.pets.data.SettingsContract;
import com.example.android.pets.data.WordContract.WordEntry;

import java.util.Collections;

public class EditorDeckActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    SharedPreferences mSettings;
    WordCursorAdapter mCursorAdapter;
    private Integer mSettingWordsAtTime = 0;
    private Cursor cursor;
    int mWordsInDeck;
    ListView wordListView;
    private static final int WORD_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_deck);

        wordListView = findViewById(R.id.list);
        mCursorAdapter = new WordCursorAdapter(this, null);
        wordListView.setAdapter(mCursorAdapter);


        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);
        if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
            mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        }

        Long folderIdLong = getIntent().getLongExtra("folder", -1L);
        Long deckIdLong = getIntent().getLongExtra("deck", -1L);

        Bundle args=new Bundle();
        args.putString("selection", WordEntry.COLUMN_FOLDER + " = ?");
        String folder = folderIdLong.toString();
        String[] selectionArgs = {folder};
        args.putStringArray("selectionArgs", selectionArgs);
        Long skipLong = deckIdLong * mSettingWordsAtTime;
        String sortOrder = WordEntry._ID + " LIMIT " + skipLong + "," + mSettingWordsAtTime;
        args.putString("sortOrder", sortOrder);
        getLoaderManager().initLoader(WORD_LOADER, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                WordEntry._ID,
                WordEntry.COLUMN_WORD,
                WordEntry.COLUMN_TRANSLATION,
                WordEntry.COLUMN_LANGUAGE_LEARNING,
                WordEntry.COLUMN_REPEAT_MEM,
                WordEntry.COLUMN_REPEAT_SPELL
        };

        String selection = bundle.getString("selection");
        String[] selectionArgs = bundle.getStringArray("selectionArgs");
        String sortOrder = bundle.getString("sortOrder");

        return new CursorLoader(
                this,
                WordEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

//        return new CursorLoader(this,
//                WordEntry.CONTENT_URI,
//                projection,
//                null,
//                null,
//                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
