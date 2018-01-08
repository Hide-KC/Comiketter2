package com.kc.comiketter2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by HIDE on 2017/12/25.
 */

public class ClearDialogFragment extends DialogFragment {
    public interface Callback{
        void onPositiveButtonClicked();
//        void onNegativeButtonClicked();
//        void onCancelled();
    }

    public static ClearDialogFragment newInstance(){
        ClearDialogFragment dialog = new ClearDialogFragment();
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
                        if (getActivity() instanceof Callback){
                            ((Callback) getActivity()).onPositiveButtonClicked();
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
                .setMessage(getString(R.string.clear_message));

        return dialogBuilder.create();
    }
}
