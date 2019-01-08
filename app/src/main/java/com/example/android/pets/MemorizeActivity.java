package com.example.android.pets;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.android.pets.data.SettingsContract;
import com.example.android.pets.data.WordContract.WordEntry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class MemorizeActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DECK_LOADER = 0;


    List<Word> mCursorData = new ArrayList<>();


    TextView wordTextView;
    TextView translationTextView;
    CardView cardView;
    ToggleButton directionToggle;
    ToggleButton mToRepeatToggle;
    TextView counterTextView;
    ToggleButton mLoopToggle;
    ToggleButton mShuffleToggle;
    ImageButton mSpeakButton;


    private int mLearningLanguage;


    SharedPreferences mSettings;
    private Integer mSettingWordsAtTime = 0;


    TextToSpeech tts;


    private Integer mWordId;
    int mWordsInDeck;
    int mInitCounterValue = 0;

    private boolean mIsLooped = false;
    private boolean mIsShuffled = true;

    private boolean mCardSwitcher = false;
    private boolean mIsFirstStart = true;

    private boolean mIsDirectionReversed = false;


    private String mWord;
    private String mTranslation;
    private int mToRepeat;

    private Cursor cursor;

    private int s;


    static final String AUDIO_PATH =
            "https://translate.google.com/translate_tts?ie=UTF-8&tl=ar-AR&client=tw-ob&q=";
    private MediaPlayer mediaPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_memorize);

        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

        if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
            mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        }

        if(mSettings.contains(SettingsContract.IS_SHUFFLED)) {
            mIsShuffled = mSettings.getBoolean(SettingsContract.IS_SHUFFLED, true);
        }

        if(mSettings.contains(SettingsContract.IS_LOOPED)) {
            mIsLooped = mSettings.getBoolean(SettingsContract.IS_LOOPED, true);
        }

        if(mSettings.contains(SettingsContract.IS_DIRECTION_REVERSED)) {
            mIsDirectionReversed = mSettings.getBoolean(SettingsContract.IS_DIRECTION_REVERSED, false);
        }




        wordTextView = findViewById(R.id.word);
        translationTextView = findViewById(R.id.translation);

        cardView = findViewById(R.id.cardView);

        counterTextView = findViewById(R.id.counter);

        mToRepeatToggle = findViewById(R.id.bookmarkToggle);

        mLoopToggle = findViewById(R.id.repeatToggle);

        mShuffleToggle = findViewById(R.id.shuffleToggle);

        if (mIsShuffled)
            mShuffleToggle.setChecked(true);
        else
            mShuffleToggle.setChecked(false);

        if (mIsLooped)
            mLoopToggle.setChecked(true);
        else
            mLoopToggle.setChecked(false);


        mSpeakButton = findViewById(R.id.speakButton);

        /**
         * Set activity background"
         */
        View someView = findViewById(R.id.cardView);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        /**
         * End of setting background
         */

        directionToggle = findViewById(R.id.directionToggle);

        directionToggle.setText("Ru - En");
        // Sets the text for when the button is first created.

        directionToggle.setTextOff("Ru - En");
        // Sets the text for when the button is not in the checked state.
        directionToggle.setTextOn("En - Ru");

        if (mIsDirectionReversed) {
            directionToggle.setChecked(true);
        }


        directionToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((ToggleButton) v).isChecked()) {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_DIRECTION_REVERSED, true);
                    editor.apply();

                    mIsDirectionReversed = true;
                    assignValues1(mInitCounterValue);
                }
                else {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_DIRECTION_REVERSED, false);
                    editor.apply();

                    mIsDirectionReversed = false;
                    assignValues1(mInitCounterValue);
                }
            }
        });

        tts=new TextToSpeech(MemorizeActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                s = status;
                new Thread(new Runnable() {
                    public void run() {
                        if(s != TextToSpeech.ERROR) {
                            tts.setPitch(1.1f);
                            if (mLearningLanguage==1)
                                tts.setLanguage(Locale.UK);
                            else if (mLearningLanguage==2)
                                tts.setLanguage(new Locale("ru"));
                        }
                    }
                }).start();
            }
        });

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/open_sans_light.ttf");
        wordTextView.setTypeface(typeface);
        translationTextView.setTypeface(typeface);

        CardView card = findViewById(R.id.cardView);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCardClick();
            }
        });


        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToNext();
            }
        });

        ImageButton previousButton = findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToPrevious();
            }
        });

        mToRepeatToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    markWordToRepeat(1);
                }
                else {
                    markWordToRepeat(0);
                }
            }
        });


        mLoopToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_LOOPED, true);
                    editor.apply();

                    mIsLooped = true;
                }
                else {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_LOOPED, false);
                    editor.apply();

                    mIsLooped = false;
                }
            }
        });

        mShuffleToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_SHUFFLED, true);
                    editor.apply();

                    Collections.shuffle(mCursorData);
                    mInitCounterValue = 0;
                    assignValues1(mInitCounterValue);
                }
                else {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(SettingsContract.IS_SHUFFLED, false);
                    editor.apply();

                    Collections.sort(mCursorData);
                    mInitCounterValue = 0;
                    assignValues1(mInitCounterValue);
                }
            }
        });


        mSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
                    playAudio(AUDIO_PATH + mWord);
//                    String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
//                    new DownloadFileAsync().execute(AUDIO_PATH + mWord);
                /*} catch (Exception e) {
                    e.printStackTrace();
                }*/
//                ConvertTextToSpeech();
            }
        });


        Long folderIdLong = getIntent().getLongExtra("folder", -1L);
        Long deckIdLong = getIntent().getLongExtra("deck", -1L);

        Bundle args=new Bundle();
        if (folderIdLong == -1L || deckIdLong == -1L) {

            Integer langId = getIntent().getIntExtra("lang_learning", 1);
            String langString = langId.toString();

            args.putString("selection", WordEntry.COLUMN_REPEAT_MEM + " = ? AND " + WordEntry.COLUMN_LANGUAGE_LEARNING + "=?");
            String[] selectionArgs = {"1", langString};
            args.putStringArray("selectionArgs", selectionArgs);
            String sortOrder = WordEntry._ID;
            args.putString("sortOrder", sortOrder);
        } else {
            args.putString("selection", WordEntry.COLUMN_FOLDER + " = ?");
            String folder = folderIdLong.toString();
            String[] selectionArgs = {folder};
            args.putStringArray("selectionArgs", selectionArgs);
            Long skipLong = deckIdLong * mSettingWordsAtTime;
            String sortOrder = WordEntry._ID + " LIMIT " + skipLong + "," + mSettingWordsAtTime;
            args.putString("sortOrder", sortOrder);
        }

        getLoaderManager().initLoader(DECK_LOADER, args, this);

    }



    private void playAudio(final String url)// throws Exception
    {
        if (mLearningLanguage == 3) {
            new Thread(new Runnable() {
                public void run() {
                    killMediaPlayer();
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                }
            }).start();
        } else {
            ConvertTextToSpeech();
        }
    }

    private void killMediaPlayer() {
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }




    private void ConvertTextToSpeech() {
        new Thread(new Runnable() {
            public void run() {
                if(mWord==null||"".equals(mWord))
                {
                    mWord = "Content not available";
                    tts.speak(mWord, TextToSpeech.QUEUE_FLUSH, null);
                }else
                    tts.speak(mWord, TextToSpeech.QUEUE_FLUSH, null);
            }
        }).start();
    }




    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }


    private void markWordToRepeat(int value) {

        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_REPEAT_MEM, value);
        Uri uri = Uri.withAppendedPath(WordEntry.CONTENT_URI, mWordId.toString());
        int rowsAffected = getContentResolver().update(uri, values, null, null);
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.

            Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Word word = mCursorData.get(mInitCounterValue);
            word.setToRepeatMem(value);
            mToRepeat = word.getToRepeatMem();
            Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void onCardClick() {
        if (!mCardSwitcher) {
            if (mIsDirectionReversed)
                translationTextView.setText(mTranslation);
            else
                translationTextView.setText(mWord);

//            ConvertTextToSpeech();

            try {
                playAudio(AUDIO_PATH + mWord);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            moveToNext();
        }
        mCardSwitcher = !mCardSwitcher;
    }

    private void moveToNext() {
        if (mInitCounterValue < mWordsInDeck - 1) {
            mInitCounterValue++;
            assignValues1(mInitCounterValue);
        } else {
            if (mIsLooped) {
                mInitCounterValue = 0;
                assignValues1(mInitCounterValue);
            } else {
                Toast.makeText(this, "It is the last word",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveToPrevious() {
        if (mInitCounterValue > 0) {
            mInitCounterValue--;
            assignValues1(mInitCounterValue);
        } else {
            if (mIsLooped) {
                mInitCounterValue = mWordsInDeck - 1;
                assignValues1(mInitCounterValue);
            } else {
                Toast.makeText(this, "It is the first word",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void assignValues1(int position) {
        Word word = mCursorData.get(position);

        mWordId = word.getId();
        mWord = word.getWord();
        mTranslation = word.getTranslation();
        mToRepeat = word.getToRepeatMem();

        if (mIsDirectionReversed) {
            wordTextView.setText(mWord);
            translationTextView.setText("");
        } else {
            wordTextView.setText(mTranslation);
            translationTextView.setText("");
        }


        if (mToRepeat == 1) {
            mToRepeatToggle.setChecked(true);
        } else {
            mToRepeatToggle.setChecked(false);
        }
        int counter = mInitCounterValue + 1;
        counterTextView.setText(counter + "/" + mWordsInDeck);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        cursor = data;

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (mIsFirstStart) {

            mWordsInDeck = data.getCount();
            mIsFirstStart = false;
//            Log.e("4444", "I am here");

            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
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

                mLearningLanguage = cursor.getInt(learningLanguageColumnIndex);

                mCursorData.add(new Word(wordId, word, translation, toRepeat, toRepeatSpell));
            }

            if (mIsShuffled)
                Collections.shuffle(mCursorData);

            assignValues1(mInitCounterValue);
            cursor.close();

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        wordTextView.setText("");
        translationTextView.setText("");
    }


    /**
     * Inner class
     */
    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());
//                OutputStream output = new FileOutputStream( Environment.getExternalStorageDirectory().getPath() + "/opa/google_translate_audio.mp3");

                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/opachki");
                dir.mkdirs();
                File file = new File(dir, "opachki.mp3");

                FileOutputStream output = new FileOutputStream(file);


//                ContextWrapper cw = new ContextWrapper(getApplicationContext());
//                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//                File mypath=new File(directory,mImageName);


                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
//            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
//            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }
    /**
     * End of inner class
     */







}