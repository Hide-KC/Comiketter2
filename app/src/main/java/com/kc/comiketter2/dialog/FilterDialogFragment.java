package com.kc.comiketter2.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.kc.comiketter2.R;

/**
 * Created by HIDE on 2018/02/24.
 */

public class FilterDialogFragment extends DialogFragment {
    public interface IDialogCallback{
        void dialogResult(String key, int resultCode);
    }

    private static final String KEY = "key";
    private static final String TITLE = "title";
    private static final String FILTER = "filter";
    private View view = null;

    public void dialogResult(String key) {
        Fragment fragment = getTargetFragment();
        if (fragment instanceof IDialogCallback){
            ((IDialogCallback) fragment).dialogResult(key, Activity.RESULT_OK);
        }
    }

    public static DialogFragment newInstance(Fragment target, String key){
        DialogFragment dialogFragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY, key);
        dialogFragment.setTargetFragment(target, 100);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String key = args.getString(KEY);

        //レイアウト展開
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_filter_dialog, null);

        //ビューを取得
        final EditText nameEdit = view.findViewById(R.id.filter_name);
        final EditText filterEdit = view.findViewById(R.id.filter_text);

        //SharedPreferenceから値を取得
        final SharedPreferences preferences = getActivity().getSharedPreferences(key, Context.MODE_PRIVATE);
        String name = "";
        String filter = "";
        if (preferences != null){
            String[] presetFilter = null;
            switch (key){
                case "filter0":
                    presetFilter = getActivity().getResources().getStringArray(R.array.preset_filter0);
                    break;
                case "filter1":
                    presetFilter = getActivity().getResources().getStringArray(R.array.preset_filter1);
                    break;
                case "filter2":
                    presetFilter = getActivity().getResources().getStringArray(R.array.preset_filter2);
                    break;
                default:
                    presetFilter = new String[]{ "" , "" };
            }

            name = preferences.getString(TITLE, presetFilter[0]);
            filter = preferences.getString(FILTER, presetFilter[1]);
        }

        nameEdit.setText(name);
        filterEdit.setText(filter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(getString(R.string.filter_info))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if (!nameEdit.getText().toString().equals("")){
                            editor.putString(TITLE, nameEdit.getText().toString());
                        } else {
                            editor.remove(TITLE);
                        }

                        if (!filterEdit.getText().toString().equals("")){
                            editor.putString(FILTER, filterEdit.getText().toString());
                        } else {
                            editor.remove(FILTER);
                        }
                        editor.apply();

                        dialogInterface.dismiss();
                        dialogResult(key);
                    }
                });

        return builder.create();
    }
}
