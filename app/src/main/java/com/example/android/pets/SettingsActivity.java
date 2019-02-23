package com.example.android.pets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.SettingsContract;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class SettingsActivity extends AppCompatActivity  implements NumberPicker.OnValueChangeListener{

    SharedPreferences mSettings;

    private Integer mSettingWordsAtTime = 0;
    private ArrayList<Integer> mSelectedLanguages;

    private ArrayList<Boolean> mSelectedLanguagesBoolean;

//    CheckedTextView englishCheckedTextView;

    private boolean mSettingIsEngStudying = false;
    private boolean mSettingIsRuStudying = false;
    private boolean mSettingIsArStudying = false;

    ListView wordsAtTimeListView;
    ThreeViewsAdapter adapterWordsAtTime;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

        setTitle(R.string.settings);

        /*englishCheckedTextView = findViewById(R.id.englishCheckedTextView);
        englishCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (englishCheckedTextView.isChecked()) {
                    englishCheckedTextView.setChecked(false);
                } else {
                    englishCheckedTextView.setChecked(true);
                }
            }
        });*/


//        Button b = findViewById(R.id.button11);
//        b.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                showWordsAtTimeDialog();
//            }
//        });

        /*Button bl = findViewById(R.id.buttonLanguages);
        bl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showLanguagesDialog();
            }
        });*/

    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayList<LanguageSettingListItem> arrayOfLanguages = new ArrayList<>();
        LanguageSettingAdapter adapterLanguages = new LanguageSettingAdapter(this, arrayOfLanguages);
        ListView languagesListView = findViewById(R.id.languagesList);
        languagesListView.setAdapter(adapterLanguages);

        addLanguageToAdapter(
                adapterLanguages,
                getString(R.string.english),
                mSettings.getBoolean(SettingsContract.IS_ENG_STUDYING, false)
        );

        addLanguageToAdapter(
                adapterLanguages,
                getString(R.string.russian),
                mSettings.getBoolean(SettingsContract.IS_RU_STUDYING, false)
        );

        addLanguageToAdapter(
                adapterLanguages,
                getString(R.string.arabic),
                mSettings.getBoolean(SettingsContract.IS_AR_STUDYING, false)
        );


        /*ListView languagesListView = findViewById(R.id.languagesList);
        languagesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        String[] languages = {"English","Russian","Arabic"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.settings_row, R.id.title_text_view, languages);
        languagesListView.setAdapter(adapter);*/
        languagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final Switch languageSwitch = view.findViewById(R.id.language_switch);

//                Log.e("int i:", i+"");
//                Log.e("long l:", l+"");

                if (languageSwitch.isChecked()) {


                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    dialogBuilder.setTitle(R.string.are_you_sure);
                    dialogBuilder.setMessage(R.string.delete_language_alert_text);

                    dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            /*SharedPreferences.Editor editor = mSettings.edit();
                            if (i == 0)
                                editor.putBoolean(SettingsContract.IS_ENG_STUDYING, false);

                            if (i == 1)
                                editor.putBoolean(SettingsContract.IS_RU_STUDYING, false);

                            if (i == 2)
                                editor.putBoolean(SettingsContract.IS_AR_STUDYING, false);
                            editor.apply();

                            setResult(4);*/

                            setLanguages(i, false);

                            deleteFolders(i+1);

                            languageSwitch.setChecked(false);

                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();

                } else {
                    setLanguages(i, true);
                    languageSwitch.setChecked(true);
                }


//                CheckedTextView checkedTextView = view.findViewById(R.id.txt_title);
//                checkedTextView.setChecked(true);


            }
        });



        ArrayList<ThreeViewsListItem> arrayOfWordsAtTime = new ArrayList<>();
        adapterWordsAtTime = new ThreeViewsAdapter(this, arrayOfWordsAtTime);
        wordsAtTimeListView = findViewById(R.id.wordsAtTimeList);
        wordsAtTimeListView.setAdapter(adapterWordsAtTime);

        /*Integer wat = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        ThreeViewsListItem wordsAtTimeItem =
                new ThreeViewsListItem(
                        "Words at time",
                        "How many words do you plan to learn every day?",
                        wat.toString());
        adapterWordsAtTime.add(wordsAtTimeItem);*/
        setWordsAtTimeListView();

        wordsAtTimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showWordsAtTimeDialog();
            }
        });

    }


    public void setLanguages(int i, boolean value) {

        SharedPreferences.Editor editor = mSettings.edit();

        if (i == 0)
            editor.putBoolean(SettingsContract.IS_ENG_STUDYING, value);
        if (i == 1)
            editor.putBoolean(SettingsContract.IS_RU_STUDYING, value);
        if (i == 2)
            editor.putBoolean(SettingsContract.IS_AR_STUDYING, value);

        editor.apply();

        setResult(4);

    }



    public void addLanguageToAdapter(LanguageSettingAdapter adapter, String name, Boolean isLearning) {
        LanguageSettingListItem item = new LanguageSettingListItem(name, isLearning);
        adapter.add(item);
    }


    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        Log.i("value is",""+newVal);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showWordsAtTimeDialog() {

        if(mSettings.contains(SettingsContract.WORDS_AT_TIME)) {
            mSettingWordsAtTime = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_words_at_time_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(R.string.set_words_at_time);

        final NumberPicker np = dialogView.findViewById(R.id.numberPicker1);
        np.setMaxValue(100);
        np.setMinValue(5);
        np.setValue(mSettingWordsAtTime);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);

        dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Integer wordsAtTime = np.getValue();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(SettingsContract.WORDS_AT_TIME, wordsAtTime);
                editor.apply();

                adapterWordsAtTime.clear();
                setWordsAtTimeListView();

                setResult(4);
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    public void setWordsAtTimeListView() {
        Integer wat = mSettings.getInt(SettingsContract.WORDS_AT_TIME, 25);
        ThreeViewsListItem wordsAtTimeItem =
                new ThreeViewsListItem(
                        "Words at time",
                        "How many words do you plan to learn every day?",
                        wat.toString());
        adapterWordsAtTime.add(wordsAtTimeItem);
    }

    private void deleteFolders(Integer language) {
        String selection = PetContract.PetEntry.COLUMN_LEARNING_LANGUAGE + " = ? ";
//        String[] languages = new String[language];

        String [] arguments = new String[1];
        arguments[0] = language.toString();

//        Log.e("langs", arguments[0]);

//        int rowsDeleted =
        getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, selection, arguments);

//        Log.e("Settings", rowsDeleted + " rows deleted from pet database");
    }




   /* public void showLanguagesDialog() {

        mSelectedLanguages = new ArrayList();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        inflater.inflate(R.layout.set_words_at_time_dialog, null);

        dialogBuilder.setTitle(R.string.you_are_learning);

        if(mSettings.contains(SettingsContract.IS_ENG_STUDYING)) {
            mSettingIsEngStudying = mSettings.getBoolean(SettingsContract.IS_ENG_STUDYING, true);
        }

        if(mSettings.contains(SettingsContract.IS_RU_STUDYING)) {
            mSettingIsRuStudying = mSettings.getBoolean(SettingsContract.IS_RU_STUDYING, false);
        }

        if(mSettings.contains(SettingsContract.IS_AR_STUDYING)) {
            mSettingIsArStudying = mSettings.getBoolean(SettingsContract.IS_AR_STUDYING, false);
        }

        final boolean[] checkedLanguages = new boolean[]{
                mSettingIsEngStudying,
                mSettingIsRuStudying,
                mSettingIsArStudying
        };



        dialogBuilder.setMultiChoiceItems(R.array.languages, checkedLanguages,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedLanguages.add(which);
                        }
                        else if (mSelectedLanguages.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedLanguages.remove(Integer.valueOf(which));
                        }

                    }
                });

        dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                SharedPreferences.Editor editor = mSettings.edit();

                if (checkedLanguages[0])
                    editor.putBoolean(SettingsContract.IS_ENG_STUDYING, true);
                else
                    editor.putBoolean(SettingsContract.IS_ENG_STUDYING, false);

                if (checkedLanguages[1])
                    editor.putBoolean(SettingsContract.IS_RU_STUDYING, true);
                else
                    editor.putBoolean(SettingsContract.IS_RU_STUDYING, false);

                if (checkedLanguages[2])
                    editor.putBoolean(SettingsContract.IS_AR_STUDYING, true);
                else
                    editor.putBoolean(SettingsContract.IS_AR_STUDYING, false);

                editor.apply();


                setResult(4);


            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }*/


    @Override
    public void onBackPressed() {
        finish();
    }
}