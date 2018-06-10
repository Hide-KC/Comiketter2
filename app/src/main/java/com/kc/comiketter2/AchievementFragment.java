package com.kc.comiketter2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by HIDE on 2017/12/14.
 */

public class AchievementFragment extends Fragment implements IUpdater {
    public static String ACHIEVEMENT = "achievement";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);

        Bundle args = getArguments();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Comiketter", "Achievement OnResume");
    }

    public static AchievementFragment newInstance(){
        AchievementFragment fragment = new AchievementFragment();
        Bundle args = new Bundle();
        args.putString(ACHIEVEMENT, ACHIEVEMENT);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void update() {


    }
}
