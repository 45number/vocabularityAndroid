package com.vocabularity.android.vocabularity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class StartupPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Integer> mTabsArray;

    private Context mContext;

    public StartupPagerAdapter(Context context, FragmentManager fm, ArrayList<Integer> tabsArray) {
        super(fm);
        mContext = context;
        mTabsArray = tabsArray;
    }

    @Override
    public Fragment getItem(int position) {
        FoldersFragment f = new FoldersFragment();
        return f;
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

}
