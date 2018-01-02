package com.kc.comiketter2;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by HIDE on 2017/12/09.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    public MyFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            return StickyListFragment.newInstance(StickyListFragment.FOLLOW_LIST);
        } else if (position == 1){
            return StickyListFragment.newInstance(StickyListFragment.PICKUP_LIST);
        } else if (position == 2) {
            return AchievementFragment.newInstance();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        //タブの数
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0){
            return context.getString(R.string.follow);
        } else if (position == 1){
            return context.getString(R.string.pickup);
        } else if (position == 2) {
            return context.getString(R.string.achievement);
        } else {
            return null;
        }
    }
}
