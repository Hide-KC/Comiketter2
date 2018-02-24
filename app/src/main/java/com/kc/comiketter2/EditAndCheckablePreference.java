package com.kc.comiketter2;

import android.content.Context;
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

/**
 * Created by HIDE on 2018/02/20.
 */

public class EditAndCheckablePreference extends Preference {

    public EditAndCheckablePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditAndCheckablePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_filter);
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
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("Comiketter", "check");
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Comiketter", "click");
            }
        });

        view.setBackground(getDrawableResource(R.drawable.ripple));
    }

    private Drawable getDrawableResource(int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return getContext().getDrawable(R.drawable.ripple);
        } else {
            return getContext().getResources().getDrawable(R.drawable.button_overlay);
        }
    }
}
