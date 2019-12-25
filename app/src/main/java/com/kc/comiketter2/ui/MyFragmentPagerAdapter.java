package com.kc.comiketter2.ui;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kc.comiketter2.R;

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
    if (position == 0) {
      return StickyListFragment.newInstance(StickyListFragment.FOLLOW_LIST);
    } else if (position == 1) {
      return StickyListFragment.newInstance(StickyListFragment.PICKUP_LIST);
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
    if (position == 0) {
      return context.getString(R.string.accounts);
    } else if (position == 1) {
      return context.getString(R.string.pickup);
    } else if (position == 2) {
      return context.getString(R.string.achievement);
    } else {
      return null;
    }
  }
}
