package com.kc.comiketter2;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by HIDE on 2018/02/24.
 */

public class FilterDialogFragment extends DialogFragment {

    public static DialogFragment newInstance(){
        DialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
