package com.kc.comiketter2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by HIDE on 2018/02/24.
 */

public class FilterDialogFragment extends DialogFragment {
    private static final String KEY = "key";
    private static final String TITLE = "title";
    private static final String FILTER = "filter";
    private View view = null;

    public static DialogFragment newInstance(String key){
        DialogFragment dialogFragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY, key);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String key = args.getString(KEY);

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
            name = preferences.getString(TITLE, "");
            filter = preferences.getString(FILTER, "");
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
                    }
                });

        return builder.create();
    }
}
