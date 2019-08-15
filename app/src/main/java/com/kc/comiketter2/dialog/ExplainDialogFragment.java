package com.kc.comiketter2.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kc.comiketter2.R;

public class ExplainDialogFragment extends DialogFragment {
    private View view = null;

    public static DialogFragment newInstance(){
        DialogFragment dialogFragment = new ExplainDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        //レイアウト展開
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.dialog_application_explain, null);

        final TextView tv = view.findViewById(R.id.explain_view);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.setMovementMethod(LinkMovementMethod.getInstance());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            tv.setText(Html.fromHtml(getString(R.string.explain_application), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv.setText(Html.fromHtml(getString(R.string.explain_application)));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(getString(R.string.explain_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
