package com.example.android.pets;

import android.content.Context;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
//import android.util.Log;
//
//import com.example.android.pets.data.SettingsContract;

import java.util.ArrayList;

public class FoldersPagerAdapter extends FragmentStatePagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<>();

//    SharedPreferences mSettings;
//    private boolean mIsEngStudying = false;
//    private boolean mIsRuStudying = false;
//    private boolean mIsArStudying = false;
//    private int mCounterOfTabs;

//    private ArrayList<Integer> mTabsTitles = new ArrayList<>();
    ArrayList<Integer> mTabsArray;

    private Context mContext;

    public FoldersPagerAdapter(Context context, FragmentManager fm, ArrayList<Integer> tabsArray) {
        super(fm);
        mContext = context;
        mTabsArray = tabsArray;
    }

    @Override
    public Fragment getItem(int position) {

//        Log.e("FoldersPagerAdapter: ", "" + mCounterOfTabs);
//        Log.e("FoldersPagerAdapter: ", mTabsTitles.toString());

        FoldersFragment f = new FoldersFragment();

        Bundle args=new Bundle();
        args.putInt("language_learning", mTabsArray.get(position));
//        args.putInt("jopa", position);
        f.setArguments(args);

        return f;


//        if (position == 0) {
//            FoldersFragment f = new FoldersFragment();
//            return f;
//        } else if (position == 1){
//            BlankFragment f = new BlankFragment();
//            return f;
//        } else if (position == 2) {
//            ColorsFragment f = new ColorsFragment();
//            return f;
//        } else {
//            BlankFragment f = new BlankFragment();
//            return f;
//        }
    }


    @Override
    public CharSequence getPageTitle(int position) {

        for (int counter = 0; counter < mTabsArray.size(); counter++) {
            if (position == counter) {
                switch (mTabsArray.get(counter)) {
                    case 1:
                        return mContext.getString(R.string.english);
                    case 2:
                        return mContext.getString(R.string.russian);
                    case 3:
                        return mContext.getString(R.string.arabic);
                }
            }
        }

        return mContext.getString(R.string.english);
    }

    @Override
    public int getCount() {
        return mTabsArray.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
//        return super.getItemPosition(object);
        return POSITION_NONE;
//        FoldersFragment fragment = (FoldersFragment)item;
//        String title = fragment.getTitle();
//        int position = titles.indexOf(title);
//
//        if (position >= 0) {
//            return position;
//        } else {
//            return POSITION_NONE;
//        }
    }




    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }





//    public class DetailOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
//
//        private int currentPage;
//
//        @Override
//        public void onPageSelected(int position) {
//            currentPage = position;
//        }
//
//        public final int getCurrentPage() {
//            return currentPage;
//        }
//    }




}
