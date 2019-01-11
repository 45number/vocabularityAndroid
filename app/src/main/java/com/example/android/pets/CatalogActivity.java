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

import com.example.android.pets.data.SettingsContract;

import java.util.ArrayList;

public class CatalogActivity extends AppCompatActivity implements
        FragmentChangeListener {

    ViewPager viewPager;
//    FrameLayout frameLayout;

    SharedPreferences mSettings;

    boolean mHasVisited;

    private ArrayList<Integer> mTabsTitles = new ArrayList<>();


    private boolean mIsEnglishStudying;
    private boolean mIsRussianStudying;
    private boolean mIsArabicStudying;

    FoldersPagerAdapter mAdapter;

//    public FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(SettingsContract.APP_PREFERENCES, Context.MODE_PRIVATE);

//        fab = findViewById(R.id.fab);

        mHasVisited = mSettings.getBoolean(SettingsContract.HAS_VISITED, false);
        if (!mHasVisited) {
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


//    public FloatingActionButton getFab() {
//        fab = findViewById(R.id.fab);
//        return fab;
//    }

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
        if (!mHasVisited) {
            super.onBackPressed();
        } else {
//            Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());


            Fragment f = mAdapter.getRegisteredFragment(viewPager.getCurrentItem());

//            int position = viewPager.getCurrentItem();
//            Fragment f =(FoldersFragment)FoldersPagerAdapter.getRegisteredFragment(position);

            ((FoldersFragment)f).onBackPressed();
            return;
        }
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment, fragment.toString());
//        Log.e("barbara straizand", fragment.toString());
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

/*    public int returnCurrentFragment() {
//        mAdapter.DetailOnPageChangeListener().get
//       return FoldersPagerAdapter.DetailOnPageChangeListener.getCurrentPage();
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + ViewPager.getCurrentItem());
    }*/

//    public int getCurrentFragment() {
////        Fragment hosted = mAdapter.getItem(viewPager.getCurrentItem());
//        int ii = viewPager.getCurrentItem();
//        return ii;
//
//
//    }

//    public void opa() {
//        Fragment fragment = ((FragmentPagerAdapter)viewPager.getAdapter()).getFragment();
//    }

}