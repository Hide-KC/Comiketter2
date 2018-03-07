package com.kc.comiketter2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by HIDE on 2017/12/24.
 */

public class OptionalInfoDialogFragment extends DialogFragment {
    private View view = null;

    public static DialogFragment newInstance(StickyListFragment targetFragment, long userID){
        OptionalInfoDialogFragment dialog = new OptionalInfoDialogFragment();
        Bundle args = new Bundle();
        args.putLong("_id", userID);

        dialog.setArguments(args);
        dialog.setTargetFragment(targetFragment, 0);
        return dialog;
    }

    public static DialogFragment newInstance(long userID){
        OptionalInfoDialogFragment dialog = new OptionalInfoDialogFragment();
        Bundle args = new Bundle();
        args.putLong("_id", userID);

        dialog.setArguments(args);
        dialog.setTargetFragment(null, 0);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long userID = getArguments().getLong("_id");
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null && targetFragment instanceof StickyListFragment){
            ((StickyListFragment)targetFragment).saveScrollY();
        }

        //ユーザ情報の取得
        final DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        UserDTO user = helper.getUser(userID);

        //レイアウト展開
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.dialog_optionalinfo, null);

        //ビューを取得
        ImageView profileImage = view.findViewById(R.id.profile_image);
        TextView name = view.findViewById(R.id.name);
        TextView holeName = view.findViewById(R.id.hole_name);
        final EditText circleName = view.findViewById(R.id.circle_name);
        TextView circleSpace = view.findViewById(R.id.circle_space);
        final Spinner target = view.findViewById(R.id.target_spinner);
        final TextView busuu = view.findViewById(R.id.busuu_value);
        ImageView plus = view.findViewById(R.id.plus);
        ImageView minus = view.findViewById(R.id.minus);
        final EditText yosan = view.findViewById(R.id.yosan_value);
        final EditText memo = view.findViewById(R.id.memo_edit);

        //ビューに値をセット
        Glide.with(this).load(user.profile_image_url).into(profileImage);
        name.setText(user.name);
        holeName.setText(StringMatcher.getHoleName(user.hole_id));
        circleName.setText(user.circle_name);
        circleSpace.setText(user.circle_space);
        if (user.target > 0){
            target.setSelection(user.target);
        } else {
            target.setSelection(0);
        }

        //nullチェックは基本しなくてOK（部数のみカウントアップのため必要）
        if (user.busuu == 0){
            busuu.setText(String.valueOf(0));
        } else {
            busuu.setText(String.valueOf(user.busuu));
        }
        if (user.yosan > 0){
            yosan.setText(String.valueOf(user.yosan));
        }
        memo.setText(user.memo);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer cnt = Integer.valueOf(busuu.getText().toString());
                if (cnt < 0){
                    cnt = 0;
                } else if (cnt >= 10) {
                    cnt = 10;
                } else {
                    cnt = cnt + 1;
                }
                busuu.setText(String.valueOf(cnt));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer cnt = Integer.valueOf(busuu.getText().toString());
                if (cnt <= 0){
                    cnt = 0;
                } else if (cnt > 10) {
                    cnt = 10;
                } else {
                    cnt = cnt - 1;
                }
                busuu.setText(String.valueOf(cnt));
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view)
                .setTitle(getActivity().getString(R.string.dialog_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ユーザ情報の保存
                        helper.setValue(helper.OPTIONAL_INFO, userID, "target", target.getSelectedItemPosition() );
                        helper.setValue(helper.OPTIONAL_INFO, userID, "busuu", Integer.valueOf(busuu.getText().toString()));

                        //予算が数値変換出来ないときはエラーキャッチ
                        try{
                            Integer y = Integer.valueOf(yosan.getText().toString());
                            helper.setValue(helper.OPTIONAL_INFO, userID, "yosan", y);
                        } catch (NumberFormatException ex){
                            ex.printStackTrace();
                            helper.setValue(helper.OPTIONAL_INFO, userID, "yosan", 0);
                        }

                        helper.setValue(helper.OPTIONAL_INFO, userID, "memo", memo.getText().toString());
                        helper.setValue(helper.OPTIONAL_INFO, userID, "circle_name", circleName.getText().toString());
                        Log.d("DialogFragment", circleName.getText().toString());
                        dialogInterface.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof ViewPager.OnPageChangeListener){
            StickyListFragment fragment = (StickyListFragment) getTargetFragment();
            fragment.saveScrollY();
            ((ViewPager.OnPageChangeListener) activity).onPageSelected(1);
            if (activity instanceof MainActivity){
                ((MainActivity) activity).updateTotalYosan();
            }
        } else if (activity instanceof IUpdater){
            ((IUpdater) activity).update();
        }
    }
}
