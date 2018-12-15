package com.kc.comiketter2.dialog;

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
    public interface ICallback {
        void onPositiveButtonClicked();
    }

    public static DialogFragment newInstance(String message){
        DialogFragment dialog = new ClearDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String message = args.getString("message", "");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("確認")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //OK
                        if (getActivity() instanceof ICallback){
                            ((ICallback) getActivity()).onPositiveButtonClicked();
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
                .setMessage(message);

        return dialogBuilder.create();
    }
}
