package com.kc.comiketter2.prefs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kc.comiketter2.dialog.ClearDialogFragment;
import com.kc.comiketter2.dialog.ExplainDialogFragment;
import com.kc.comiketter2.dialog.FilterDialogFragment;
import com.kc.comiketter2.R;

/**
 * Created by HIDE on 2018/03/03.
 */

public class MyPreferenceFragment extends PreferenceFragment implements FilterDialogFragment.IDialogCallback {
    //もしフィルターの数を増やす場合はここを増やすこと！！
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preference clearDataPreference = findPreference("clear_data");
        clearDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialog = ClearDialogFragment.newInstance(getString(R.string.clear_got_data));
                dialog.show(((AppCompatActivity)(MyPreferenceFragment.this.getActivity())).getSupportFragmentManager(), "clear_all");
                return false;
            }
        });

        Preference howToPreference = findPreference("how_to");
        howToPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MyPreferenceFragment.this.getActivity(), com.kc.comiketter2.HowToActivity.class);
                startActivity(intent);
                return false;
            }
        });

        Preference privacyPolicyPreference = findPreference("privacy_policy");
        privacyPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialog = ExplainDialogFragment.newInstance();
                dialog.show(((AppCompatActivity)(MyPreferenceFragment.this.getActivity())).getSupportFragmentManager(), "explain");
                return false;
            }
        });
    }

    @Override
    public void dialogResult(String key, int resultCode) {
        //値の更新
        if (resultCode == Activity.RESULT_OK){
            Preference preference = findPreference(key);
            if (preference instanceof EditAndCheckablePreference){
                ((EditAndCheckablePreference) preference).updatePreferenceValue();
            }
        }
    }
}