package com.vocabularity.android.vocabularity;


//import android.app.Activity;
import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import com.vocabularity.android.vocabularity.data.SettingsContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartupQuantityFragment extends Fragment {


    SharedPreferences mSettings;


    public StartupQuantityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_startup_quantity, container, false);

        mSettings = this.getActivity().getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

        final NumberPicker np = rootView.findViewById(R.id.numberPicker1);
        np.setMaxValue(100);
        np.setMinValue(5);
        np.setValue(25);
        np.setWrapSelectorWheel(false);
//        np.setOnValueChangedListener(this);

        final Button startButton = rootView.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer wordsAtTime = np.getValue();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(SettingsContract.WORDS_AT_TIME, wordsAtTime);
                editor.putBoolean(SettingsContract.HAS_VISITED, true);
                editor.apply();


                reloadUI();

            }
        });


        return rootView;
    }

    public void reloadUI() {
//        Intent intent = new Intent(this.getContext(), CatalogActivity.class);
//        intent.setFlag(Intent.CLEAR_TASK);
//        startActivity(intent);

        this.getActivity().recreate();
    }

}