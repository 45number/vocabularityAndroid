package com.example.android.pets;

import android.app.ActionBar;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

    Button cancelButton;
    Button okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        ActionBar actionBar = getActionBar();
//        actionBar.hide();

        setContentView(R.layout.activity_editor_deck);

        getSupportActionBar().hide();

//        wordListView = findViewById(R.id.list);
//        mCursorAdapter = new WordCursorAdapter(this, null);
//        wordListView.setAdapter(mCursorAdapter);

        cancelButton = findViewById(R.id.cancel_button);
        okButton = findViewById(R.id.ok_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });


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
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
//        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        for(int counter = 0; counter< cursor.getCount(); counter++) {
            int wordIdColumnIndex = cursor.getColumnIndex(WordEntry._ID);
            int wordColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_WORD);
            int translationColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_TRANSLATION);
            int toRepeatColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_REPEAT_MEM);
            int toRepeatSpellColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_REPEAT_SPELL);
            int learningLanguageColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_LANGUAGE_LEARNING);

            Integer wordId = cursor.getInt(wordIdColumnIndex);
            String word = cursor.getString(wordColumnIndex);
            String translation = cursor.getString(translationColumnIndex);
            Integer toRepeat = cursor.getInt(toRepeatColumnIndex);
            Integer toRepeatSpell = cursor.getInt(toRepeatSpellColumnIndex);

//            Integer counterPlusOne = counter + 1;



//            mLearningLanguage = cursor.getInt(learningLanguageColumnIndex);

//            mCursorData.add(new Word(wordId, word, translation, toRepeat, toRepeatSpell));
            drawTableRow(counter + 1, new Word(wordId, word, translation, toRepeat, toRepeatSpell));
            cursor.moveToNext();
        }
//        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mCursorAdapter.swapCursor(null);
    }

    public void drawTableRow(Integer counter, Word word) {
        /* Find Tablelayout defined in main.xml */
        TableLayout tl = findViewById(R.id.table);
        /* Create a new row to be added. */
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tr.setBackgroundResource(R.drawable.row_border);

        /* Create a Button to be the row-content. */
//        Button b = new Button(this);
//        b.setText("Dynamic Button");
//        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView counterView = new TextView(this);
        counterView.setText(counter.toString());
        TableRow.LayoutParams counterParams = new TableRow.LayoutParams(
                60,
                TableRow.LayoutParams.MATCH_PARENT
        );
//        counterParams.setMargins(0, 10, 0, 0);
        counterView.setLayoutParams(counterParams);
//        counterView.setPadding(5, 20, 0, 0);
        counterView.setGravity(Gravity.CENTER);
//        counterView.setBackgroundColor(Color.parseColor("#000000"));
        counterView.setBackgroundResource(R.drawable.row_counter_border);
        counterView.setTextColor(Color.parseColor("#FFFFFF"));


        TextView idTextView = new TextView(this);
        Integer wordId = word.getId();
        idTextView.setText(wordId.toString());
        idTextView.setLayoutParams(new TableRow.LayoutParams(0,0));
        idTextView.setVisibility(View.INVISIBLE);


//        TableRow.LayoutParams wrapperParams = new TableRow.LayoutParams(
//                TableRow.LayoutParams.MATCH_PARENT,
//                TableRow.LayoutParams.WRAP_CONTENT
//        );
//        wrapperParams.setMargins(60, 0, 0, 0);
//        LinearLayout wrapper = new LinearLayout(this);
//        wrapper.setLayoutParams(wrapperParams);



        EditText wordEditText = new EditText(this);
        wordEditText.setText(word.getWord());
        wordEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, .5f));
        wordEditText.setBackgroundDrawable(null);
        wordEditText.setPadding(30,10, 30, 20);

        View separator = new View(this);
        separator.setBackgroundColor(Color.parseColor("#CCCCCC"));
        separator.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));

        EditText translateEditText = new EditText(this);
        translateEditText.setText(word.getTranslation());
        translateEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, .5f));
        translateEditText.setBackgroundDrawable(null);
        translateEditText.setPadding(30,10, 30, 20);

        /* Add Button to row. */
        /*tr.addView(wordEditText);
        tr.addView(separator);
        tr.addView(translateEditText);*/
//        wrapper.addView(wordEditText);
//        wrapper.addView(separator);
//        wrapper.addView(translateEditText);

//        tr.addView(counterView);
        tr.addView(counterView);
        tr.addView(idTextView);
        tr.addView(wordEditText);
        tr.addView(separator);
        tr.addView(translateEditText);




//        View separator_horizontal = new View(this);
//        separator_horizontal.setBackgroundColor(Color.parseColor("#000000"));
//        separator_horizontal.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));

        /* Add row to TableLayout. */
//tr.setBackgroundResource(R.drawable.sf_gradient_03);
        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
//        tl.addView(tr, separator_horizontal);
    }

    private void onSaveButtonClicked() {
        TableLayout table = findViewById(R.id.table);
        for (int counter = 0; counter < table.getChildCount(); counter++) {
            TableRow row = (TableRow) table.getChildAt(counter);

//            TextView cTextView = (TextView) row.getVirtualChildAt(0);
//            String counterString = cTextView.getText().toString();
//            int c = Integer.parseInt(counterString);

            TextView idTextView = (TextView) row.getVirtualChildAt(1);
            String idString = idTextView.getText().toString();
            int id = Integer.parseInt(idString);

            EditText wordEditText = (EditText) row.getVirtualChildAt(2);
            String word = wordEditText.getText().toString();

            EditText translationEditText = (EditText) row.getVirtualChildAt(4);
            String translation = translationEditText.getText().toString();

            Uri currentWordUri = ContentUris.withAppendedId(WordEntry.CONTENT_URI, id);
            ContentValues values = new ContentValues();
            values.put(WordEntry.COLUMN_WORD, word);
            values.put(WordEntry.COLUMN_TRANSLATION, translation);
            int rowsAffected = getContentResolver().update(currentWordUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(EditorDeckActivity.this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }

//            Log.e("Child", c + " : " + id + " / " + word + " / " + translation);
        }

    }


}
