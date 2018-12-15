package com.kc.comiketter2.data;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.kc.comiketter2.R;
import com.kc.comiketter2.UserDTO;

/**
 * Created by HIDE on 2017/11/29.
 */

public abstract class DTOAdapter extends ArrayAdapter<UserDTO> {

    public DTOAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    protected int getRowBackColor(Integer day){
        Resources res = getContext().getResources();
        int color = 0;
        if (day == 1){
            color = res.getColor(R.color.first_row_back_1);
        } else if (day == 2){
            color = res.getColor(R.color.second_row_back_1);
        } else if (day == 3) {
            color = res.getColor(R.color.third_row_back_1);
        } else {
            color = res.getColor(R.color.unknown_row_back_1);
        }

        return color;
    }

    protected int getHeaderBackColor(Integer day){
        Resources res = getContext().getResources();
        int color = 0;
        if (day == 1){
            color = res.getColor(R.color.first_header_back);
        } else if (day == 2){
            color = res.getColor(R.color.second_header_back);
        } else if (day == 3) {
            color = res.getColor(R.color.third_header_back);
        } else {
            color = res.getColor(R.color.unknown_header_back);
        }

        return color;
    }
}
