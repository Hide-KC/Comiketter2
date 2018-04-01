package com.kc.comiketter2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Created by HIDE on 2017/12/13.
 */

public abstract class StickyListFragment extends Fragment {
    public static final String FOLLOW_LIST = "follow_list";
    public static final String PICKUP_LIST = "pickup_list";
    public static final String PARAM = "param";

    public static StickyListFragment newInstance(String param){
        StickyListFragment fragment = null;
        Bundle args = new Bundle();
        if(param.equals(FOLLOW_LIST)){
            args.putString(PARAM, FOLLOW_LIST);
            fragment = new FollowListFragment();
        } else if (param.equals(PICKUP_LIST)){
            args.putString(PARAM, PICKUP_LIST);
            fragment = new PickupListFragment();
        } else {
            throw new IllegalArgumentException("セットした引数は無効です");
        }

        fragment.setArguments(args);
        return fragment;
    }

    protected abstract void filterUsers(List<UserDTO> users);
    public abstract void selectionToTop();
    public abstract void saveScrollY();
}
