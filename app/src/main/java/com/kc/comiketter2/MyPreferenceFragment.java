package com.kc.comiketter2;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

/**
 * Created by HIDE on 2018/03/03.
 */

public class MyPreferenceFragment extends PreferenceFragment {
    public static final int FILTER_COUNT = 5;

    public static PreferenceFragment newInstance(){
        PreferenceFragment fragment = new MyPreferenceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}