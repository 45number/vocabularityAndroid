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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.pets.data.DeckContract;
import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.SettingsContract;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class SettingsActivity extends AppCompatActivity  implements NumberPicker.OnValueChangeListener{

    SharedPreferences mSettings;

    private Integer mSettingWordsAtTime = 0;

    /*private ArrayList<Integer> mSelectedLanguages;
    private ArrayList<Boolean> mSelectedLanguagesBoolean;
    private boolean mSettingIsEngStudying = false;
    private boolean mSettingIsRuStudying = false;
    private boolean mSettingIsArStudying = false;*/

    ListView wordsAtTimeListView;
    ThreeViewsAdapter adapterWordsAtTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);
        setTitle(R.string.settings);
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

        languagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final Switch languageSwitch = view.findViewById(R.id.language_switch);

                if (languageSwitch.isChecked()) {

                    if (countLanguages() > 1) {

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                        dialogBuilder.setTitle(R.string.are_you_sure);
                        dialogBuilder.setMessage(R.string.delete_language_alert_text);

                        dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
                        showOopsDialog();
                    }

                } else {
                    setLanguages(i, true);
                    languageSwitch.setChecked(true);
                }
            }
        });

        ArrayList<ThreeViewsListItem> arrayOfWordsAtTime = new ArrayList<>();
        adapterWordsAtTime = new ThreeViewsAdapter(this, arrayOfWordsAtTime);
        wordsAtTimeListView = findViewById(R.id.wordsAtTimeList);
        wordsAtTimeListView.setAdapter(adapterWordsAtTime);
        setWordsAtTimeListView();

        wordsAtTimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showWordsAtTimeDialog();
            }
        });


//        setListViewHeightBasedOnChildren(languagesListView);

    }

    /*public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }*/

    public int countLanguages() {
        int counter = 0;

        if (mSettings.getBoolean(SettingsContract.IS_ENG_STUDYING, false))
            counter++;
        if (mSettings.getBoolean(SettingsContract.IS_RU_STUDYING, false))
            counter++;
        if (mSettings.getBoolean(SettingsContract.IS_AR_STUDYING, false))
            counter++;

        return counter;
    }

    public void showOopsDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        dialogBuilder.setTitle(R.string.oops);
        dialogBuilder.setMessage(R.string.you_should_learn_one_language);
        dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
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

//                String selection = DeckContract.DeckEntry.COLUMN_LEARNING_LANGUAGE + " = ? ";
//                String [] arguments = new String[1];
//                arguments[0] = language.toString();
//                  int rowsDeleted =
                getContentResolver().delete(DeckContract.DeckEntry.CONTENT_URI, null, null);

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
        String [] arguments = new String[1];
        arguments[0] = language.toString();
//        int rowsDeleted =
        getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, selection, arguments);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}