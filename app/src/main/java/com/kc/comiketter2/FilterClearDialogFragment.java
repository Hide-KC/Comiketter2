package com.kc.comiketter2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by HIDE on 2018/03/03.
 */

public class FilterClearDialogFragment extends DialogFragment {
    public static DialogFragment newInstance(){
        DialogFragment dialog = new FilterClearDialogFragment();
        Bundle args = new Bundle();
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("確認")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //OK
                        for (int filter_i = 0; filter_i < MyPreferenceFragment.FILTER_COUNT; filter_i++){
                            SharedPreferences preferences = getActivity().getSharedPreferences("filter" + filter_i, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //キャンセル
                        //何もしないで閉じる
                    }
                })
                .setMessage(getString(R.string.clear_filter_message));

        return dialogBuilder.create();
    }
}
