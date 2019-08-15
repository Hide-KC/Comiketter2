package com.kc.comiketter2.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.kc.comiketter2.R;

/**
 * Created by HIDE on 2017/11/18.
 */

public class UserLoadDialogFragment extends DialogFragment {
    private View view = null;
    private Integer user_count = 0;
    private Integer now_count = 0;

    public interface IDialogControl{
        void onDialogCreated(UserLoadDialogFragment dialog);
        void onDialogCancelled();
    }

    public static UserLoadDialogFragment newInstance(String title){
        UserLoadDialogFragment dialog = new UserLoadDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.dialog_task_progress, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState != null){
            TextView textView = view.findViewById(R.id.user_count);
            textView.setText(savedInstanceState.getInt("now_count") + " / " + savedInstanceState.getInt("user_count"));
            ProgressBar progressBar = view.findViewById(R.id.progress);
            progressBar.setProgress(savedInstanceState.getInt("now_count"));
            progressBar.setMax(savedInstanceState.getInt("user_count"));
        }

        builder.setView(view)
                .setTitle(title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        if (getActivity() instanceof IDialogControl){
            ((IDialogControl)getActivity()).onDialogCreated(this);
        }

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getActivity() instanceof IDialogControl){
            ((IDialogControl)getActivity()).onDialogCancelled();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            this.user_count = savedInstanceState.getInt("user_count");
            this.now_count = savedInstanceState.getInt("now_count");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("user_count", user_count);
        outState.putInt("now_count", now_count);
    }

    public void setUserCount(Integer count){
        this.user_count = count;
        TextView textView = view.findViewById(R.id.user_count);
        textView.setText("0 / " + String.valueOf(this.user_count));

        ProgressBar progressBar = view.findViewById(R.id.progress);
        progressBar.setMax(this.user_count);
    }

    public void updateNowCount(Integer count){
        this.now_count = count;
        TextView textView = view.findViewById(R.id.user_count);
        textView.setText(String.valueOf(this.now_count) + " / " + String.valueOf(this.user_count));

        ProgressBar progress = view.findViewById(R.id.progress);

        progress.setProgress(this.now_count);

    }


}
