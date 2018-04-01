package com.kc.comiketter2;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by HIDE on 2017/12/13.
 */

public class PickupListFragment extends StickyListFragment implements IUpdater {
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //layoutファイルからViewオブジェクトを生成
        View view = inflater.inflate(R.layout.fragment_sticky_list, container, false);

        //StickyListビューを取得
        final StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);

        //Adapterをいったんお掃除
        ArrayAdapter<UserDTO> instantAdapter = (ArrayAdapter<UserDTO>) sticky.getAdapter();
        if (instantAdapter != null) {
            instantAdapter.clear();
        }

        Bundle args = getArguments();

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        final PickUpDTOAdapter adapter = new PickUpDTOAdapter(getActivity());
        if (args.getString(PARAM).equals(PICKUP_LIST)){
            //PickupListアダプターの実装
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            long myID = preferences.getLong(MainActivity.MY_ID, 0);
            long selectedListID = preferences.getLong(MainActivity.SELECTED_LIST_ID, 0);
            List<UserDTO> users = helper.getUserList(myID, selectedListID);

            this.filterUsers(users);

            for (UserDTO user : users){
                if (user.pickup == 1){
                    adapter.add(user);
                }
            }

//            if (preferences.getBoolean("filter_switch", false)){
//                for (UserDTO user : users){
//                    if (user.pickup == 1 && StringMatcher.getEventName(user.name,false, getContext()) != null){
//                        adapter.add(user);
//                    }
//                }
//            } else {
//                for (UserDTO user : users){
//                    if (user.pickup == 1){
//                        adapter.add(user);
//                    }
//                }
//            }

            view.setTag(PICKUP_LIST);
        } else {
            throw new IllegalArgumentException("取り出した引数は無効です");
        }

        //タップイベント付加
        sticky.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view instanceof ImageButton){
                    return;
                } else {
                    PickUpDTOAdapter adp = (PickUpDTOAdapter) sticky.getAdapter();
                    UserDTO user = adp.getItem(i);

                    DialogFragment dialog = OptionalInfoDialogFragment.newInstance(PickupListFragment.this, user.user_id);
                    dialog.show(getActivity().getSupportFragmentManager(), "optional_info");
                    Log.d("Comiketter", "" + user.user_id);
                }
            }
        });

        //Adapterセット
        sticky.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScrollY();
    }

    @Override
    public void update() {
        Log.d("Comiketter", "PickupList Update");

        View view = this.getView();
        final StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);
        final ArrayAdapter<UserDTO> adapter = new PickUpDTOAdapter(getActivity());

        AsyncTask<Long, UserDTO, ArrayAdapter<UserDTO>> task = new AsyncTask<Long, UserDTO, ArrayAdapter<UserDTO>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayAdapter<UserDTO> doInBackground(Long... params) {
                long myID = params[0];
                long listID = params[1];

                DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
                //PickupListアダプターの実装
                List<UserDTO> users = helper.getUserList(myID, listID);
                PickupListFragment.this.filterUsers(users);

                for (UserDTO user : users){
                    if (user.pickup == 1){
                        adapter.add(user);
                    }
                }

                return adapter;
            }

            @Override
            protected void onPostExecute(ArrayAdapter<UserDTO> adapter) {
                super.onPostExecute(adapter);
                ArrayAdapter<UserDTO> arrayAdapter = (ArrayAdapter<UserDTO>) sticky.getAdapter();
                arrayAdapter.clear();
                sticky.setAdapter((StickyListHeadersAdapter) adapter);

                Bundle args = getArguments();
                if (args != null){
                    Integer position = args.getInt("position");
                    Integer fromTop = args.getInt("fromTop");

                    if (position > 0){
                        position = position + 1;
                    }
                    sticky.setSelectionFromTop(position, fromTop);
                }
            }
        };

        //myIDとlistIDを持ってくる
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long myID = preferences.getLong(MainActivity.MY_ID, 0);
        long listID = preferences.getLong(MainActivity.SELECTED_LIST_ID, 0);
        task.execute(myID, listID);
    }

    @Override
    protected void filterUsers(List<UserDTO> users) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //フィルタの有効化と全てのユーザの表示
        StringBuilder name = new StringBuilder();
        for (int user_i = users.size() - 1; user_i >= 0; user_i--){
            name.append(users.get(user_i).name);
            if (preferences.getBoolean("filter_switch", false)){
                if (StringMatcher.getEventName(name.toString(), false, getContext()) == null){
                    users.remove(user_i);
                }
            } else if (preferences.getBoolean("visible_all_user", false)){
                if (StringMatcher.getSpace(name.toString()).equals("")){
                    users.remove(user_i);
                }
            }
            name.setLength(0);
        }
    }

    @Override
    public void selectionToTop() {
        View view = getView();
        if (view != null){
            StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);
            sticky.setSelection(0);

            Bundle args = getArguments();
            if (args != null){
                args.putInt("position", 0);
            }
        }
    }

    @Override
    public void saveScrollY() {
        View view = getView();
        if (view != null){
            StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);

            Bundle args = getArguments();
            if (args != null){
                args.putInt("position", sticky.getFirstVisiblePosition());
                args.putInt("fromTop", sticky.getChildAt(0).getTop());
            }
        }
    }
}
