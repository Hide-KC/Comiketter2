package com.kc.comiketter2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class PickupListFragment extends StickyListFragment implements IObserver {
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //layoutファイルからViewオブジェクトを生成
        View view = inflater.inflate(R.layout.pickup_list, container, false);

        //StickyListビューを取得
        final StickyListHeadersListView sticky = view.findViewById(R.id.pickup_list);

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
            List<UserDTO> users = helper.getUserList();

            for (UserDTO user : users){
                if (user.auto_day > 0 && user.pickup == 1){
                    adapter.add(user);
                }
            }

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

                    OptionalInfoDialogFragment dialog = OptionalInfoDialogFragment.newInstance(PickupListFragment.this, user.user_id);
                    dialog.show(getFragmentManager(), "optional_info");
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
        final StickyListHeadersListView sticky = view.findViewById(R.id.pickup_list);
        final ArrayAdapter<UserDTO> adapter = new PickUpDTOAdapter(getActivity());

        AsyncTask<Void, UserDTO, ArrayAdapter<UserDTO>> task = new AsyncTask<Void, UserDTO, ArrayAdapter<UserDTO>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayAdapter<UserDTO> doInBackground(Void... voids) {
                DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
                //PickupListアダプターの実装
                List<UserDTO> users = helper.getUserList();

                for (Integer user_i = 0; user_i < users.size(); user_i++){
                    if (users.get(user_i).pickup == 1 && users.get(user_i).auto_day > 0){
                        adapter.add(users.get(user_i));
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

        task.execute();

    }

    @Override
    public void selectionToTop() {
        View view = getView();
        if (view != null){
            StickyListHeadersListView sticky = view.findViewById(R.id.pickup_list);
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
            StickyListHeadersListView sticky = view.findViewById(R.id.pickup_list);

            Bundle args = getArguments();
            if (args != null){
                args.putInt("position", sticky.getFirstVisiblePosition());
                args.putInt("fromTop", sticky.getChildAt(0).getTop());
            }
        }
    }
}
