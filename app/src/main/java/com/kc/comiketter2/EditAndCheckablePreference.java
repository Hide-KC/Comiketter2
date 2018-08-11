package com.kc.comiketter2;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by HIDE on 2018/02/20.
 */

public class EditAndCheckablePreference extends Preference {
    public static final String KEY = "key";
    public static final String TITLE = "title";
    public static final String FILTER = "filter";
    public static final String CHECKED = "checked";

    public EditAndCheckablePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditAndCheckablePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.preference_filter, parent,false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ダイアログの表示
                String key = EditAndCheckablePreference.this.getKey();
                //コミケ専用フィルタの場合はクリックイベントなし
                if (key.equals("comike")) return;

                Activity activity = (Activity) getContext();
                DialogFragment dialogFragment = FilterDialogFragment.newInstance(key);
                dialogFragment.show(activity.getFragmentManager(), "filter_info");
            }
        });

        //KeyからSharedPreferencesを取得、初期値に設定
        final SharedPreferences preferences = getContext().getSharedPreferences(getKey(), Context.MODE_PRIVATE);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        TextView titleView = view.findViewById(android.R.id.title);
        TextView summaryView = view.findViewById(android.R.id.summary);

        if (getKey().equals("comike")){
            //コミケ専用フィルタの場合は初期値true
            checkBox.setChecked(preferences.getBoolean(CHECKED, true));
            titleView.setText(preferences.getString(TITLE, getContext().getString(R.string.filter_name_comike)));
            summaryView.setText(preferences.getString(FILTER, getContext().getString(R.string.filter_text_comike)));
        } else {
            //カスタムフィルタの場合は初期値false
            checkBox.setChecked(preferences.getBoolean(CHECKED, false));

            //プリセットフィルタを取得
            String[] presetFilter0 = getContext().getResources().getStringArray(R.array.preset_filter0);
            String[] presetFilter1 = getContext().getResources().getStringArray(R.array.preset_filter1);
            String[] presetFilter2 = getContext().getResources().getStringArray(R.array.preset_filter2);

            switch (getKey()){
                case "filter0":
                    //コミティア
                    titleView.setText(preferences.getString(TITLE, presetFilter0[0]));
                    summaryView.setText(preferences.getString(FILTER, presetFilter0[1]));
                    break;
                case "filter1":
                    //コミック１
                    titleView.setText(preferences.getString(TITLE, presetFilter1[0]));
                    summaryView.setText(preferences.getString(FILTER, presetFilter1[1]));
                    break;
                case "filter2":
                    //スパコミ
                    titleView.setText(preferences.getString(TITLE, presetFilter2[0]));
                    summaryView.setText(preferences.getString(FILTER, presetFilter2[1]));
                    break;
                default:
                    titleView.setText(preferences.getString(TITLE, getContext().getString(R.string.filter_name_title)));
                    summaryView.setText(preferences.getString(FILTER, getContext().getString(R.string.filter_text_title)));
            }
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //有効・無効の切り替え
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(CHECKED, b);
                editor.apply();
            }
        });

        view.setBackground(getBackgroundSelector());
    }

    private Drawable getBackgroundSelector(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return getContext().getDrawable(R.drawable.ripple);
        } else {
            return getContext().getResources().getDrawable(R.drawable.button_overlay);
        }
    }
}
