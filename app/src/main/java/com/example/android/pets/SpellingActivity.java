package com.example.android.pets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.pets.data.SettingsContract;
import com.example.android.pets.data.WordContract.WordEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class SpellingActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DECK_LOADER = 0;


    List<Word> mCursorData = new ArrayList<>();

    TextView wordTextView;
    TextView translationTextView;
    CardView cardView;
//    ToggleButton directionToggle;
    ToggleButton mToRepeatToggle;
    TextView counterTextView;
    ToggleButton mLoopToggle;
    ToggleButton mShuffleToggle;
    ImageButton mSpeakButton;
    EditText mWordEditText;

    ImageButton mEditButton;
    EditText mWordEdit;
    EditText mTranslationEdit;
    LinearLayout mEditActions;
    Button mDeleteButton;
    Button mCancelButton;
    Button mSaveButton;



    TextToSpeech tts;

    private Integer mWordId;
    int mWordsInDeck;
    int mInitCounterValue = 0;

    private boolean mIsLooped = false;
    private boolean mIsShuffled = true;

//    private boolean mCardSwitcher = false;
    private boolean mIsFirstStart = true;

//    private boolean mIsDirectionReversed = false;


    private String mWord;
    private String mTranslation;
    private int mToRepeat;

    private Cursor cursor;

    private int s;

    private boolean mIsCorrectAnswer = false;

    private int mLearningLanguage;


    private int mWrongWordCursorPosition;

    static final String AUDIO_PATH =
            "https://translate.google.com/translate_tts?ie=UTF-8&tl=ar-AR&client=tw-ob&q=";
    private MediaPlayer mediaPlayer;



    SharedPreferences mSettings;
    private Integer mSettingWordsAtTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_spelling);

        wordTextView = findViewById(R.id.word);
        translationTextView = findViewById(R.id.translation);

        cardView = findViewById(R.id.cardView);

        counterTextView = findViewById(R.id.counter);

        mToRepeatToggle = findViewById(R.id.bookmarkToggle);

        mLoopToggle = findViewById(R.id.repeatToggle);

        mShuffleToggle = findViewById(R.id.shuffleToggle);
        mShuffleToggle.setChecked(true);


        mSpeakButton = findViewById(R.id.speakButton);


        mWordEditText = findViewById(R.id.verifiable_word);
        mWordEditText.setGravity(Gravity.CENTER_HORIZONTAL);


        mEditButton = findViewById(R.id.editButton);
        mWordEdit = findViewById(R.id.wordEdit);
        mTranslationEdit = findViewById(R.id.translationEdit);
        mWordEdit.setVisibility(View.GONE);
        mTranslationEdit.setVisibility(View.GONE);
        mEditActions = findViewById(R.id.editActions);
        mEditActions.setVisibility(View.GONE);
        mDeleteButton = findViewById(R.id.deleteButton);
        mCancelButton = findViewById(R.id.cancelButton);
        mSaveButton = findViewById(R.id.saveButton);




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



        if (mIsShuffled)
            mShuffleToggle.setChecked(true);
        else
            mShuffleToggle.setChecked(false);

        if (mIsLooped)
            mLoopToggle.setChecked(true);
        else
            mLoopToggle.setChecked(false);



        /*mWordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode==KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onEnterPressed();
                    Log.e("opa", "opa");
                    return true;
                }

                if (keyCode==KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    translationTextView.setText("");
                    return SpellingActivity.super.onKeyDown(keyCode, event);
                }
                return false;
            }
        });*/

        mWordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mWordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    onEnterPressed();
                    return true;
                }
                else{
                    return false;
                }
            }
        });





        /**
         * Set activity background"
         */
        View someView = findViewById(R.id.cardView);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        /**
         * End of setting background
         */

//        directionToggle = findViewById(R.id.directionToggle);
//        directionToggle.setText("Ru - En");
//        directionToggle.setTextOff("Ru - En");
//        directionToggle.setTextOn("En - Ru");
//        directionToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((ToggleButton) v).isChecked()) {
//                    mIsDirectionReversed = true;
//                    assignValues1(mInitCounterValue);
//                }
//                else {
//                    mIsDirectionReversed = false;
//                    assignValues1(mInitCounterValue);
//                }
//            }
//        });

        /*tts=new TextToSpeech(SpellingActivity.this, new TextToSpeech.OnInitListener() {
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
        });*/

        ttsStart();

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
                //is chkIos checked?
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
                //is chkIos checked?
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
                //is chkIos checked?
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
                playAudio(AUDIO_PATH + mWord);
//                ConvertTextToSpeech();
            }
        });


        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWordEdit.setVisibility(View.VISIBLE);
                mTranslationEdit.setVisibility(View.VISIBLE);
//                mEditActions.setVisibility(View.VISIBLE);


//                setViewVisibility(mWordEdit, 1);
//                setViewVisibility(mTranslationEdit, 1);
                setViewVisibility(mEditActions, 1);



                wordTextView.setVisibility(View.GONE);
                translationTextView.setVisibility(View.GONE);
                mEditButton.setVisibility(View.GONE);

//                setViewVisibility(wordTextView, 0);
//                setViewVisibility(translationTextView, 0);
//                setViewVisibility(mEditButton, 0);



                mWordEdit.requestFocus();
                mWordEdit.setSelection(mWordEdit.getText().length());


//                mWordEditText.setFocusable(false);
                mWordEditText.setEnabled(false);
                mWordEditText.setText("");
//                setViewVisibility(mWordEditText, 0);
                mWordEditText.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });

//                mWordEditText.setCursorVisible(false);
//                mWordEdit.setKeyListener(null);
//                mWordEdit.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String word = mTranslationEdit.getText().toString().trim();
                String translation = mWordEdit.getText().toString().trim();

                Uri currentWordUri = ContentUris.withAppendedId(WordEntry.CONTENT_URI, mWordId);
                ContentValues values = new ContentValues();
                values.put(WordEntry.COLUMN_WORD, word);
                values.put(WordEntry.COLUMN_TRANSLATION, translation);

                int rowsAffected = getContentResolver().update(currentWordUri, values, null, null);

                if (rowsAffected == 0) {
                    Toast.makeText(SpellingActivity.this, getString(R.string.editor_update_pet_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    mWord = word;
                    mTranslation = translation;

                    wordTextView.setText(mTranslation);
                    mWordEdit.setText(mTranslation);
                    translationTextView.setText("");
                    mTranslationEdit.setText(mWord);

                    Word wordCurrent = mCursorData.get(mInitCounterValue);
                    wordCurrent.setWord(mWord);
                    wordCurrent.setTranslation(mTranslation);


                }
                finishEditing();
            }
        });





        Long folderIdLong = getIntent().getLongExtra("folder", -1L);
        Long deckIdLong = getIntent().getLongExtra("deck", -1L);

        Bundle args=new Bundle();
        if (folderIdLong == -1L || deckIdLong == -1L) {

            Integer langId = getIntent().getIntExtra("lang_learning", 1);
            String langString = langId.toString();

            args.putString("selection", WordEntry.COLUMN_REPEAT_SPELL + " = ? AND " + WordEntry.COLUMN_LANGUAGE_LEARNING + "=?");
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



    public void finishEditing() {
        mWordEdit.setVisibility(View.GONE);
        mTranslationEdit.setVisibility(View.GONE);
//        mEditActions.setVisibility(View.GONE);

//        setViewVisibility(mWordEdit, 0);
//        setViewVisibility(mTranslationEdit, 0);
        setViewVisibility(mEditActions, 0);

        wordTextView.setVisibility(View.VISIBLE);
        translationTextView.setVisibility(View.VISIBLE);
        mEditButton.setVisibility(View.VISIBLE);

//        mWordEditText.setFocusable(true);
//        mWordEditText.setCursorVisible(true);
//        mWordEdit.setKeyListener(null);
//        mWordEdit.setBackgroundColor(Color.TRANSPARENT);

        mWordEditText.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        mWordEditText.setEnabled(true);
        mWordEditText.requestFocus();
    }

    public void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_card_title);
        builder.setMessage(R.string.delete_card_msg);
        builder.setPositiveButton(R.string.ok_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteCard();

            }
        });
        builder.setNegativeButton(R.string.cancel_deleting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteCard() {

        setResult(3);

//        Log.e("mInitCounterValue", mInitCounterValue+"");
//        Log.e("mWordsInDeck - 1", mWordsInDeck - 1+"");
//        Log.e("mWordId", mWordId+"");
//        finish();

        Uri currentWordUri = ContentUris.withAppendedId(WordEntry.CONTENT_URI, mWordId);
        if (currentWordUri != null) {


            Long folderLong = getIntent().getLongExtra("folder", -1L);
            Long deckLong = getIntent().getLongExtra("deck", -1L);
            String[] selectionArgs = new String[] {folderLong.toString(), deckLong.toString()};

//            String[] selectionArgs = null;
            String select = null;
            if (mCursorData.size() == 1) {
//                Long folderLong = getIntent().getLongExtra("folder", -1L);
//                Long deckLong = getIntent().getLongExtra("deck", -1L);
//                selectionArgs = new String[] {folderLong.toString(), deckLong.toString()};
                select = "dummy";
            }

            int rowsDeleted = getContentResolver().delete(currentWordUri, select, selectionArgs);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                mCursorData.remove(mInitCounterValue);
                mWordsInDeck--;

                if (!mCursorData.isEmpty()) {
                    if (mInitCounterValue < mWordsInDeck - 1) {
                        assignValues1(mInitCounterValue);
                    } else {
                        if (mIsLooped) {
                            mInitCounterValue = 0;
                            assignValues1(mInitCounterValue);
                        } else {
                            if (mInitCounterValue > 0)
                                mInitCounterValue--;
                            assignValues1(mInitCounterValue);
                        }
                    }
//                    assignValues1(mInitCounterValue);
                } else {
//                    PetProvider.shiftDecks();
                    setResult(6);
                    finish();
                }

            }
        }
    }



    public void setViewVisibility(final View view, int opacity) {
        if (opacity == 0) {
            view.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.GONE);
                }
            });
        } else if (opacity == 1) {
            view.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    view.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    private void playAudio(final String url) {
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
//    @Override
//    protected void onSaveInstanceState (Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putCharSequence(KEY_TEXT_VALUE, mTextView.getText());
//    }



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


    public void ttsStart() {
        tts=new TextToSpeech(SpellingActivity.this, new TextToSpeech.OnInitListener() {
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

    @Override
    protected void onResume() {
        ttsStart();
        super.onResume();
    }


    private void markWordToRepeat(int value) {
        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_REPEAT_SPELL, value);
        Uri uri = Uri.withAppendedPath(WordEntry.CONTENT_URI, mWordId.toString());
        int rowsAffected = getContentResolver().update(uri, values, null, null);
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.

            Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Word word = mCursorData.get(mInitCounterValue);
            word.setToRepeatMem(value);
            mToRepeat = word.getToRepeatSpell();
        }
    }







    public SpannableStringBuilder setTags(ArrayList<String> tags, ArrayList<Integer> indices) {
//        if (tags == null) {
//            return;
//        }

//        translationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35); // Tricking the text view for getting a bigger line height

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        String between = " ";
        int tagStart = 0;

        float textSize = 13 * getResources().getDisplayMetrics().scaledDensity; // sp to px

        int bgColor = ContextCompat.getColor(this, R.color.correctBackground);
        int textColor = ContextCompat.getColor(this, R.color.white);

        for (String tag : tags) {


            // Append tag and space after
            stringBuilder.append(tag);
            stringBuilder.append(between);

            // Set span for tag

            if (indices.size() != 0) {
                for (Integer index : indices) {
                    if (tags.indexOf(tag) == index) {
                        bgColor = ContextCompat.getColor(this, R.color.magnitude9);
                        RoundedBackgroundSpan tagSpan = new RoundedBackgroundSpan(bgColor, textColor, textSize);
                        stringBuilder.setSpan(tagSpan, tagStart, tagStart + tag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mWrongWordCursorPosition = tagStart + tag.length();
                        break;
                    }
                }
            }


            // Update to next tag start
            tagStart += tag.length() + between.length();

            if (indices.size() == 0)
                mWrongWordCursorPosition = tagStart - between.length();
        }

        if (indices.size() == 0) {
            bgColor = ContextCompat.getColor(this, R.color.correctBackground);
            RoundedBackgroundSpan tagSpan = new RoundedBackgroundSpan(bgColor, textColor, textSize);
            stringBuilder.setSpan(tagSpan, 0, mWrongWordCursorPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


//        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        return stringBuilder;
//        mWordEditText.setText(stringBuilder);
    }


//    private void onCardClick() {
//        if (!mCardSwitcher) {
//
//            ArrayList<String> tags = stringToArrayList(mWord);
//
//            String userWordString = mWordEditText.getText().toString().trim();
//            ArrayList<Integer> indicesOfWrong = checkWord(mWord, userWordString);
//
//            setTags(tags, indicesOfWrong);
//
//            ConvertTextToSpeech();
//        } else {
//            moveToNext();
//        }
//        mCardSwitcher = !mCardSwitcher;
//
//    }

    public void onEnterPressed() {
        if (!mIsCorrectAnswer) {

            String userWordString = mWordEditText.getText().toString().trim();
            if (userWordString != null && !userWordString.isEmpty()) {

//                ConvertTextToSpeech();

                playAudio(AUDIO_PATH + mWord);

                ArrayList<String> tags = stringToArrayList(mWord);
                ArrayList<Integer> indicesOfWrong = checkWord(mWord, userWordString);
                SpannableStringBuilder sourceWord = setTags(tags, indicesOfWrong);
                translationTextView.setText(sourceWord);


                ArrayList<String> tags1 = stringToArrayList(userWordString);
                ArrayList<Integer> indicesOfWrong1 = checkWord(userWordString, mWord);
                SpannableStringBuilder sourceWord1 = setTags(tags1, indicesOfWrong1);
                mWordEditText.setText(sourceWord1);

                mWordEditText.setSelection(mWrongWordCursorPosition);

                if (indicesOfWrong.isEmpty() && indicesOfWrong1.isEmpty()) {
                    mIsCorrectAnswer = true;
                }
            }
        } else {
            mIsCorrectAnswer = false;
            moveToNext();
        }
    }

    private ArrayList<Integer> checkWord(String a, String b) {
        ArrayList<String> aArrayList = stringToArrayList(a);
        ArrayList<String> bArrayList = stringToArrayList(b);

        ArrayList<String> setADifference = difference1(aArrayList, bArrayList);

        ArrayList<Integer> wrongIndices = getWrongWordsIndices(aArrayList, setADifference);
        return wrongIndices;
    }

    private ArrayList<String> stringToArrayList(String string) {
        string = string.toLowerCase();
        String[] stringArray = string.split(" ");
        ArrayList<String> stringArrayList = new ArrayList<>(Arrays.asList(stringArray));
        return stringArrayList;
    }

    public static ArrayList<Integer> getWrongWordsIndices(ArrayList<String> setA, ArrayList<String> setB) {
        ArrayList<Integer> tmp = new ArrayList<>();
        for (String x : setB)
            if (setA.contains(x)) {
                tmp.add(setA.indexOf(x));
            }
        return tmp;
    }

    public static ArrayList<String> difference1(ArrayList<String> setA, ArrayList<String> setB) {
        ArrayList<String> tmp = new ArrayList<>(setA);
        tmp.removeAll(setB);
        return tmp;
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
        mToRepeat = word.getToRepeatSpell();

        mIsCorrectAnswer = false;

        mWordEditText.setText("");

        wordTextView.setText(mTranslation);
        translationTextView.setText("");

        mWordEdit.setText(mTranslation);
        mTranslationEdit.setText(mWord);


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

        if (mIsFirstStart) { // Find the columns of pet attributes that we're interested in
            mWordsInDeck = data.getCount();
            mIsFirstStart = false;
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int wordIdColumnIndex = cursor.getColumnIndex(WordEntry._ID);
                int wordColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_WORD);
                int translationColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_TRANSLATION);
                int toRepeatColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_REPEAT_SPELL);
                int toRepeatMemColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_REPEAT_MEM);
                int learningLanguageColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_LANGUAGE_LEARNING);

                Integer wordId = cursor.getInt(wordIdColumnIndex);
                String word = cursor.getString(wordColumnIndex);
                String translation = cursor.getString(translationColumnIndex);
                Integer toRepeat = cursor.getInt(toRepeatColumnIndex);
                Integer toRepeatMem = cursor.getInt(toRepeatMemColumnIndex);

                mLearningLanguage = cursor.getInt(learningLanguageColumnIndex);

                mCursorData.add(new Word(wordId, word, translation, toRepeatMem, toRepeat));
            }
            if (mIsShuffled)
                Collections.shuffle(mCursorData);

            assignValues1(mInitCounterValue);
            cursor.close();

        } else {
            Log.e("4444", "No here");
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        wordTextView.setText("");
        translationTextView.setText("");
    }
}
