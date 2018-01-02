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

public class AchievementFragment extends Fragment implements IObserver {
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
        Log.d("Comiketter", "Achievement Update");
        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        List<UserDTO> users = helper.getUserList();

        Integer cnt_pickup = 0;
        Integer cnt_hasgot = 0;

        for (Integer user_i = 0; user_i < users.size(); user_i++){
            if (users.get(user_i).pickup == 1){
                cnt_pickup++;
            }

            if (users.get(user_i).hasgot == 1){
                cnt_hasgot++;
            }
        }

        View view = getView();
        ProgressBar progressBar = view.findViewById(R.id.achievement_progress);
        TextView hasgotView = view.findViewById(R.id.hasgot_count);
        TextView pickupView = view.findViewById(R.id.pickup_count);
        progressBar.setMax(cnt_pickup);
        progressBar.setProgress(cnt_hasgot);
        hasgotView.setText(String.valueOf(cnt_hasgot));
        pickupView.setText(String.valueOf(cnt_pickup));

    }
}
