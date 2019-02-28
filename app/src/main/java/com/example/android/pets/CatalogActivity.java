package com.example.android.pets;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.android.pets.data.SettingsContract;
import com.example.android.pets.data.pathItem;

import java.util.ArrayList;

public class CatalogActivity extends AppCompatActivity implements
        FragmentChangeListener {

    ViewPager viewPager;

    SharedPreferences mSettings;

//    boolean mHasVisited;

    private ArrayList<Integer> mTabsTitles = new ArrayList<>();

    private boolean mIsEnglishStudying;
    private boolean mIsRussianStudying;
    private boolean mIsArabicStudying;

    FoldersPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

        mIsEnglishStudying = mSettings.getBoolean(SettingsContract.IS_ENG_STUDYING, false);
        mIsRussianStudying = mSettings.getBoolean(SettingsContract.IS_RU_STUDYING, false);
        mIsArabicStudying = mSettings.getBoolean(SettingsContract.IS_AR_STUDYING, false);

//        mHasVisited = mSettings.getBoolean(SettingsContract.HAS_VISITED, false);

        if (!mIsEnglishStudying && !mIsRussianStudying && !mIsArabicStudying) {
            setContentView(R.layout.activity_startup);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new StartupLanguagesFragment())
                    .commit();

        } else {

            setContentView(R.layout.activity_folders);

            viewPager = findViewById(R.id.viewpager);

            setTabs();

            mAdapter = new FoldersPagerAdapter(this, getSupportFragmentManager(), mTabsTitles);
            viewPager.setAdapter(mAdapter);

        }

    }

    public void setTabs() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        mTabsTitles.clear();

        if(mSettings.contains(SettingsContract.IS_ENG_STUDYING)) {
            mIsEnglishStudying = mSettings.getBoolean(SettingsContract.IS_ENG_STUDYING, false);
            if (mIsEnglishStudying)
                mTabsTitles.add(1);
        }

        if(mSettings.contains(SettingsContract.IS_RU_STUDYING)) {
            mIsRussianStudying = mSettings.getBoolean(SettingsContract.IS_RU_STUDYING, false);
            if (mIsRussianStudying)
                mTabsTitles.add(2);
        }

        if(mSettings.contains(SettingsContract.IS_AR_STUDYING)) {
            mIsArabicStudying = mSettings.getBoolean(SettingsContract.IS_AR_STUDYING, false);
            if (mIsArabicStudying)
                mTabsTitles.add(3);
        }


        if (mTabsTitles.size() > 1) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    public void updateFolderPageAdapter() {
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (!mIsEnglishStudying && !mIsRussianStudying && !mIsArabicStudying) {
            super.onBackPressed();
        } else {
            Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
            ((FoldersFragment)f).onBackPressed();
            return;
        }
    }

    public void refreshDecks() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        ((FoldersFragment)f).refreshDecks();
        return;
    }

    public void refreshMemWords() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        ((FoldersFragment)f).refreshMemWords();
        return;
    }

    public pathItem getCurrentFolder() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        return ((FoldersFragment)f).getCurrentFolder();
    }

    /*public ArrayList<pathItem> getFoldersPath() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        return ((FoldersFragment)f).getFoldersPath();
    }*/

    public void updatePathTextView() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        ((FoldersFragment)f).updatePathTextView();
    }

    public String getPath() {
        Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        return  ((FoldersFragment)f).getPath();
    }

    @Override
    public void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment, fragment.toString());
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }
}