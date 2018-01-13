package com.kc.comiketter2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by HIDE on 2017/12/13.
 */

public class FollowListFragment extends StickyListFragment implements IObserver  {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //layoutファイルからViewオブジェクトを生成
        View view = inflater.inflate(R.layout.fragment_sticky_list, container, false);

        //StickyListビューを取得
        StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);

        //Adapterをいったんお掃除
        ArrayAdapter<UserDTO> adapter = (ArrayAdapter<UserDTO>) sticky.getAdapter();
        if (adapter != null) {
            adapter.clear();
        }

        Bundle args = getArguments();

        DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
        if (args.getString(PARAM).equals(FOLLOW_LIST)){
            //FollowListアダプターの実装
            adapter = new UserDTOAdapter(getActivity());
            List<UserDTO> users = helper.getUserList();

            for (UserDTO user : users){
//                if (user.auto_day > 0) {
                    adapter.add(user);
//                }
            }
            view.setTag(FOLLOW_LIST);
        } else {
            throw new IllegalArgumentException("取り出した引数は無効です");
        }

        sticky.setAdapter((StickyListHeadersAdapter) adapter);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScrollY();
    }

    @Override
    public void update() {
        Log.d("Comiketter", "FollowList Update");

        View view = this.getView();
        final StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);
        final ArrayAdapter<UserDTO> adapter = new UserDTOAdapter(getActivity());

        AsyncTask<Void, UserDTO, ArrayAdapter<UserDTO>> task = new AsyncTask<Void, UserDTO, ArrayAdapter<UserDTO>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayAdapter<UserDTO> doInBackground(Void... voids) {
                DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
                //UserDTOListアダプターの実装
                List<UserDTO> users = helper.getUserList();

                for (Integer user_i = 0; user_i < users.size(); user_i++){
//                    if (users.get(user_i).auto_day > 0){
                        adapter.add(users.get(user_i));
//                    }
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

        task.execute();
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
        if (view != null) {
            StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);

            Bundle args = getArguments();
            if (args != null){
                args.putInt("position", sticky.getFirstVisiblePosition());
                args.putInt("fromTop", sticky.getChildAt(0).getTop());
            }
        }
    }
}
