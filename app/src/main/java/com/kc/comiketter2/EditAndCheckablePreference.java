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
                Log.d("key", key);

                Activity activity = (Activity) getContext();
                DialogFragment dialogFragment = FilterDialogFragment.newInstance(key);
                dialogFragment.show(activity.getFragmentManager(), "filter_info");
            }
        });

        //KeyからSharedPreferencesを取得、初期値に設定
        final SharedPreferences preferences = getContext().getSharedPreferences(getKey(), Context.MODE_PRIVATE);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setChecked(preferences.getBoolean(CHECKED, false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //有効・無効の切り替え
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(CHECKED, b);
                editor.apply();
            }
        });

        TextView titleView = view.findViewById(android.R.id.title);
        TextView summaryView = view.findViewById(android.R.id.summary);
        titleView.setText(preferences.getString(TITLE, getContext().getString(R.string.filter_name_title)));
        summaryView.setText(preferences.getString(FILTER, getContext().getString(R.string.filter_text_title)));

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
