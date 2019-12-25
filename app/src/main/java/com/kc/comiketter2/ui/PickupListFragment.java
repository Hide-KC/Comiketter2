package com.kc.comiketter2.ui;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.kc.comiketter2.IUpdater;
import com.kc.comiketter2.R;
import com.kc.comiketter2.UserDTO;
import com.kc.comiketter2.main.MainActivity;
import com.kc.comiketter2.model.data.database_helper.DatabaseHelper;
import com.kc.comiketter2.pickup.PickUpDTOAdapter;
import com.kc.comiketter2.ui.dialog.OptionalInfoDialogFragment;
import com.kc.comiketter2.util.StringMatcher;

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
    View view = inflater.inflate(R.layout.fragment_sticky_pickuplist, container, false);

    //StickyListビューを取得
    final StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      sticky.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
    }

    //Adapterをいったんお掃除
    ArrayAdapter<UserDTO> instantAdapter = (ArrayAdapter<UserDTO>) sticky.getAdapter();
    if (instantAdapter != null) {
      instantAdapter.clear();
    }

    //空ビューを表示
    sticky.setEmptyView(view.findViewById(R.id.empty_text));

    Bundle args = getArguments();

    DatabaseHelper helper = DatabaseHelper.getInstance(getActivity());
    final PickUpDTOAdapter adapter = new PickUpDTOAdapter(getActivity());
    if (args.getString(PARAM).equals(PICKUP_LIST)) {
      //PickupListアダプターの実装
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
      long myID = preferences.getLong(MainActivity.MY_ID, 0);
      long selectedListID = preferences.getLong(MainActivity.SELECTED_LIST_ID, 0);
      List<UserDTO> users = helper.getUserList(myID, selectedListID);

      this.filterUsers(users);

      for (UserDTO user : users) {
        if (user.pickup == 1) {
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
        if (view instanceof ImageButton) {
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

        for (UserDTO user : users) {
          if (user.pickup == 1) {
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
        if (args != null) {
          Integer position = args.getInt("position");
          Integer fromTop = args.getInt("fromTop");

          if (position > 0) {
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
    //コミケ専用フィルタとカスタムフィルタによるフィルタリング
    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    boolean visibleAllUser = defaultSharedPreferences.getBoolean("visible_all_user", false);
    if (visibleAllUser) return;

    StringBuilder name = new StringBuilder();
    for (int user_i = users.size() - 1; user_i >= 0; user_i--) {
      name.append(users.get(user_i).name);
      //フィルタを実施。配置表示必須。
      String eventName = StringMatcher.getEventName(name.toString(), true, getActivity());
      if (eventName == null || eventName.equals("")) {
        users.remove(user_i);
      }
      name.setLength(0);
    }
  }

  @Override
  public void selectionToTop() {
    View view = getView();
    if (view != null) {
      StickyListHeadersListView sticky = view.findViewById(R.id.sticky_list);
      sticky.setSelection(0);

      Bundle args = getArguments();
      if (args != null) {
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
      if (args != null) {
        args.putInt("position", sticky.getFirstVisiblePosition());
        args.putInt("fromTop", sticky.getChildAt(0).getTop());
      }
    }
  }
}
