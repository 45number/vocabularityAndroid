package com.example.android.pets;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.android.pets.data.SettingsContract;


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

        final CheckBox englishCheckBox = rootView.findViewById(R.id.englishCheckBox);
        final CheckBox russianCheckBox = rootView.findViewById(R.id.russianCheckBox);
        final CheckBox arabicCheckBox = rootView.findViewById(R.id.arabicCheckBox);

        Button nextButton = rootView.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mSettings.edit();


                if (englishCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_ENG_STUDYING, true);
//                else
//                    editor.putBoolean(SettingsContract.IS_ENG_STUDYING, false);

                if (russianCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_RU_STUDYING, true);
//                else
//                    editor.putBoolean(SettingsContract.IS_RU_STUDYING, false);

                if (arabicCheckBox.isChecked())
                    editor.putBoolean(SettingsContract.IS_AR_STUDYING, true);
//                else
//                    editor.putBoolean(SettingsContract.IS_AR_STUDYING, false);


                editor.apply();

                showOtherFragment();
            }
        });

        return rootView;
    }


    public void showOtherFragment() {
        Fragment fr = new StartupQuantityFragment();
        FragmentChangeListener fc = (FragmentChangeListener)getActivity();
        fc.replaceFragment(fr);
    }

}
