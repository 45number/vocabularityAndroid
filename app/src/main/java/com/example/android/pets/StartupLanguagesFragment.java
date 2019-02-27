package com.example.android.pets;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;

import com.example.android.pets.data.SettingsContract;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartupLanguagesFragment extends Fragment {

    SharedPreferences mSettings;

    public StartupLanguagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_startup_languages, container, false);
        mSettings = this.getActivity().getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

        final Button nextButton = rootView.findViewById(R.id.nextButton);

        if (countLanguages() < 1) {
            nextButton.setEnabled(false);
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*SharedPreferences.Editor editor = mSettings.edit();
                if (englishCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_ENG_STUDYING, true);
                if (russianCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_RU_STUDYING, true);
                if (arabicCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_AR_STUDYING, true);
                editor.apply();*/

                if (countLanguages() > 0)
                    showOtherFragment();

            }
        });





        ArrayList<LanguageSettingListItem> arrayOfLanguages = new ArrayList<>();
        LanguageSettingAdapter adapterLanguages = new LanguageSettingAdapter(getActivity(), arrayOfLanguages);
        ListView languagesListView = rootView.findViewById(R.id.languagesList);
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
                    setLanguages(i, false);
                    languageSwitch.setChecked(false);

                    if (countLanguages() < 1) {
                        nextButton.setEnabled(false);
//                        nextButton.setBackgroundColor(getResources().getColor(R.color.grey));
//                        nextButton.setBackground(getResources().getDrawable(R.drawable.cancel_button));
                    }

                } else {
                    setLanguages(i, true);
                    languageSwitch.setChecked(true);

//                    nextButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    nextButton.setEnabled(true);

                }
            }
        });







        /*final CheckBox englishCheckBox = rootView.findViewById(R.id.englishCheckBox);
        final CheckBox russianCheckBox = rootView.findViewById(R.id.russianCheckBox);
        final CheckBox arabicCheckBox = rootView.findViewById(R.id.arabicCheckBox);*/



        return rootView;
    }


    public void addLanguageToAdapter(LanguageSettingAdapter adapter, String name, Boolean isLearning) {
        LanguageSettingListItem item = new LanguageSettingListItem(name, isLearning);
        adapter.add(item);
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
    }

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



    public void showOtherFragment() {
        Fragment fr = new StartupQuantityFragment();
        FragmentChangeListener fc = (FragmentChangeListener)getActivity();
        fc.replaceFragment(fr);
    }

}
