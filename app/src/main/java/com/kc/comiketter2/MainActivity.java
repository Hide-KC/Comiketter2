package com.kc.comiketter2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

public class MainActivity extends AppCompatActivity
        implements MyAsyncTask.IAsyncTaskCallback,
        UserLoadDialogFragment.IDialogControl,
        ViewPager.OnPageChangeListener,
        ClearDialogFragment.ICallback {
    //画面回転時、 task == null（onCreate） となってしまうので、
    //とりあえず TwitterUtils.task に参照を退避するようにしている。
    //task,dialogで弱参照しているActivity,DialogFragmentが軒並み参照先を失ってしまうので、
    //相互にコールバック持たせてMainで参照を持たせてやってる。
    //→TaskManagerクラスを作成。
    private MyAsyncTask<Void, Integer, List<User>> task = null;
    private Integer taskID = -1;
    private static final String TourakuDay = "2017-11-02";
    private ActionBarDrawerToggle drawerToggle;
    public static final String MY_ID = "my_id";
    public static final String SELECTED_LIST_ID = "selected_list_id";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SearchUserActivity.REQUEST_CODE && data != null && resultCode == RESULT_OK){
            //Pickup等、DBに変更があった場合に備えてページ更新
            TabLayout tabLayout = findViewById(R.id.tab_layout);
            onPageSelected(tabLayout.getSelectedTabPosition());
        } else if (requestCode == MyPreferenceActivity.REQUEST_CODE){
            //カスタムフィルタの状態を読込み、ページ更新
            Log.d("Preference", "onActivityResult");
            Toolbar toolbar = findViewById(R.id.toolbar);
            ImageView filterView = toolbar.findViewById(R.id.toolbar_constraint).findViewById(R.id.filter_image);

            //フィルタワードのチェック状態で判別
            if(isFiltered()){
                filterView.setVisibility(View.VISIBLE);
            } else {
                filterView.setVisibility(View.INVISIBLE);
            }

            TabLayout tabLayout = findViewById(R.id.tab_layout);
            onPageSelected(tabLayout.getSelectedTabPosition());
        } else if (requestCode == ConfirmOAuthActivity.REQUEST_CODE && resultCode == RESULT_CANCELED){
            //認証失敗、終了
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            //drawer is opened
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DatabaseHelper helper = DatabaseHelper.getInstance(this);

        if (TwitterUtils.loadAccessToken(this) == null){
            //OAuth認証画面
            startOAuthActivity();
        } else {
            //認証済みの場合
            if (savedInstanceState == null){
                Log.d("Comiketter","AccessToken有り");
                Toast.makeText(this, "Twitter連携済",Toast.LENGTH_SHORT).show();
            } else {
                taskID = savedInstanceState.getInt("task_id");
                Log.d("Comiketter","taskID = " + taskID);
            }
        }

        //DrawerLayoutの設定
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ConstraintLayout includeDrawer = drawerLayout.findViewById(R.id.include_drawer);
        final ListView leftDrawer = includeDrawer.findViewById(R.id.event_list);
        Button subscribeList = includeDrawer.findViewById(R.id.subscribeList);
        subscribeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //購読リスト選択ダイアログを表示
            }
        });

        SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
        final long myID = prefMyself.getLong("my_id", 0);
        if (myID != 0){
            //アイコンの設定
            ConstraintLayout headerLayout = includeDrawer.findViewById(R.id.header_layout);
            ImageView icon = headerLayout.findViewById(R.id.my_icon);
            Glide.with(this).load(prefMyself.getString("profile_image_url", null)).into(icon);

            //アカウント名の設定
            TextView name = headerLayout.findViewById(R.id.my_name);
            name.setText(prefMyself.getString("name", null));

            //スピナーの設定
            Spinner spinner = headerLayout.findViewById(R.id.account_spinner);

            //総予算の設定

        }

        //表示する一覧
        final ListDTOAdapter listDTOAdapter = new ListDTOAdapter(this);
        List<ListDTO> listDTOs = helper.getLists(this, myID);
        for (int list_i = 0; list_i < listDTOs.size(); list_i++){
            listDTOAdapter.add(listDTOs.get(list_i));
        }
        leftDrawer.setAdapter(listDTOAdapter);
        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //リストクリック時の処理
                //単垢対応モード。複アカになったらリファクタ必要かも。
                ListDTOAdapter adapter = (ListDTOAdapter) leftDrawer.getAdapter();
                for (int list_i = 0; list_i < adapter.getCount(); list_i++){
                    adapter.getItem(list_i).subscribed = false;
                }

                //adapterをArrayListに変換
                ListDTO listDTO = adapter.getItem(i);
                listDTO.subscribed = true;
                List<ListDTO> listDTOList = adapter.toArrayList();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(SELECTED_LIST_ID, listDTO.listID);
                editor.apply();

                //表示差し替え
                updateStickyList();
                MainActivity.this.onBackPressed();
                MainActivity.this.updateToolbar();

                //DBに保存
                helper.setListsSubscribe(listDTOList);
                Log.d("ListItemClicked", listDTO.listID + " " + listDTO.name);
            }
        });

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                null,
                R.string.busuu_hint,
                R.string.yosan_hint
        ){
            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("DrawerToggle", "Opened");
                ListView listView = drawerView.findViewById(R.id.event_list);
                ListDTOAdapter adapter = (ListDTOAdapter)listView.getAdapter();
                adapter.clear();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                List<ListDTO> listDTOs = helper.getLists(MainActivity.this, preferences.getLong(MY_ID, 0));
                for (ListDTO listDTO:listDTOs){
                    adapter.add(listDTO);
                }

                MainActivity.this.updateTotalYosan();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("DrawerToggle", "Closed");
                ListView listView = drawerView.findViewById(R.id.event_list);
                ListDTOAdapter adapter = (ListDTOAdapter)listView.getAdapter();
                adapter.clear();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                List<ListDTO> listDTOs = helper.getLists(MainActivity.this, preferences.getLong(MY_ID, 0));
                for (ListDTO listDTO:listDTOs){
                    adapter.add(listDTO);
                }

                MainActivity.this.updateTotalYosan();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);

        //TabLayoutの設定
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        FragmentPagerAdapter adapter = new MyFragmentPagerAdapter(this, getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        //オートマチック方式。これだけで両方syncする。
        tabLayout.setupWithViewPager(viewPager);

        //タブ選択時に初期位置まで戻すためのリスナー
        TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                FragmentManager manager = getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + tab.getPosition());
                if (fragment instanceof StickyListFragment){
                    if (fragment instanceof PickupListFragment){
                        Log.d("FragmentName", "PickupListFragment");
                    } else if (fragment instanceof FollowListFragment){
                        Log.d("FragmentName", "FollowListFragment");
                    }

                    ((StickyListFragment) fragment).saveScrollY();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //選択済みタブの再選択
                FragmentManager manager = getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + tab.getPosition());
                if (fragment instanceof StickyListFragment){
                    ((StickyListFragment) fragment).selectionToTop();
                }
            }
        };

        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        //ToolBarの設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        ConstraintLayout constraintLayout = toolbar.findViewById(R.id.toolbar_constraint);
        TextView selectedListName = constraintLayout.findViewById(R.id.selected_list_name);
        ImageView filterView = constraintLayout.findViewById(R.id.filter_image);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //フィルタワードのチェック状態で判別
        if(isFiltered()){
            filterView.setVisibility(View.VISIBLE);
        } else {
            filterView.setVisibility(View.INVISIBLE);
        }

        long listID = preferences.getLong(SELECTED_LIST_ID, 0);
        selectedListName.setText(helper.getListDTO(this, listID).name);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.toolbar_reload){
                    if (TwitterUtils.loadAccessToken(MainActivity.this) == null){
                        Toast.makeText(MainActivity.this, "Twitterと連携できません。", Toast.LENGTH_SHORT).show();
                    } else {
                        getFriendIDs();
                    }
                } else if (id == R.id.toolbar_clear){
                    clearOptionalInfo();
                } else if (id == R.id.toolbar_search_sub) {
                    startSearchActivity();
                } else if (id == R.id.toolbar_preferences) {
                    startPreferenceActivity();
                }
                return true;
            }
        });

        ImageButton btn = findViewById(R.id.navigation_icon);
        if (myID != 0){
            Glide.with(this).load(prefMyself.getString("profile_image_url", null)).into(btn);
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Toolbar", "NavigationIcon Clicked");
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        helper.close();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        taskID = savedInstanceState.getInt("task_id");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("task_id", taskID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Comiketter2", "onPause");
        TaskManager manager = TaskManager.getInstance();
        if (!manager.hasTaskID(taskID)){
            taskID = manager.put(task);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("Comiketter2", "onResume");
        updateTotalYosan();
        TaskManager manager = TaskManager.getInstance();
        MyAsyncTask<Void, Integer, List<User>> task = (MyAsyncTask<Void, Integer, List<User>>)manager.get(taskID);

        if (task != null){
            Log.d("Comiketter2", task.getStatus().toString());
            task.weakActivity = new WeakReference<Activity>(this);
        }
    }

    private void getFriendIDs(){
        final Context context = this;

        MyAsyncTask<Long, Integer, List<UserDTO>> task = new MyAsyncTask<Long,Integer,List<UserDTO>>(this){
            @Override
            protected void onPreExecute() {
                //ダイアログを表示
                UserLoadDialogFragment dialog = UserLoadDialogFragment.newInstance(getString(R.string.loading));

                dialog.show(getSupportFragmentManager(), "user_load");
                weakDialog = new WeakReference<>(dialog);
            }

            @Override
            protected List<UserDTO> doInBackground(Long... params) {
                targetID = params[0];
                Twitter twitter = TwitterUtils.getTwitter(context);

                List<UserDTO> users = new ArrayList<>();
                List<Long> idsList = new ArrayList<>();
                IDs ids = null;
                DatabaseHelper helper = DatabaseHelper.getInstance(context);

                try {
                    //自身のプロフィールを取得
                    User myself = twitter.verifyCredentials();
                    myID = myself.getId();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong(MY_ID, myID);
                    editor.apply();
                    UserDTO myselfDTO = new UserDTO(myself);
                    myselfDTO.token = twitter.getOAuthAccessToken().getToken();
                    myselfDTO.token_secret = twitter.getOAuthAccessToken().getTokenSecret();
                    users.add(myselfDTO);

                    //リスト一覧の取得
                    List<UserList> userLists = twitter.getUserLists(myID);
                    List<ListDTO> listDTOs = new ArrayList<>();
                    for (UserList userList:userLists){
                        ListDTO listDTO = new ListDTO();
                        listDTO.listID = userList.getId();
                        listDTO.slug = userList.getSlug();
                        listDTO.name = userList.getName();
                        listDTOs.add(listDTO);
                    }
                    helper.updateLists(myID, listDTOs);

                    //targetID=0ならフォロー一覧を、!=0ならリストメンバー一覧を取得
                    if (targetID == 0){
                        Long cursor = -1L;
                        do {
                            ids = twitter.getFriendsIDs(myID, cursor);

                            if (ids != null){
                                for (Long id:ids.getIDs()){
                                    idsList.add(id);
                                }

                                RateLimitStatus rateLimit = ids.getRateLimitStatus();
//                                Log.d("Comiketter2", "残りAPI＝" + rateLimit.getRemaining());

                                if (rateLimit.getRemaining() <= 0){
                                    Log.d("Comiketter2", "API切れです。");
                                    break;
                                }

                            } else {
                                Log.d("Comiketter2", "フォローID一覧が取得できませんでした。");
                                return null;
                            }
                            cursor = ids.getNextCursor();
                        } while (ids.hasNext());

                        //ユーザIDが０個だったら抜ける
                        if (idsList.size() <= 0) {
                            return null;
                        } else {
                            publishProgress(0, idsList.size());
                        }

                        if (this.isCancelled()) return null;

                        //Userオブジェクトを取得
                        Integer max = idsList.size() / 100;
                        for (Integer i = 0; i < max + 1 ; i++){
                            Integer j = 0;
                            long[] array = new long[100];

                            while (true){
                                //arrayが100埋まるまでidsListからユーザIDをコピー
                                if (i * 100 + j < idsList.size()){
                                    array[j] = idsList.get(i * 100 + j);
                                } else break;

                                j++;
                                //j == 99で0~99までインデックス埋まる
                                if (j >= 100) break;
                            }

                            ResponseList<User> userResponseList = twitter.lookupUsers(array);

                            for (User user:userResponseList){
                                //正規表現に一致するユーザのみ抽出。
                                if (StringMatcher.getComiketName(user.getName()) != null
                                        || StringMatcher.getEventName(user.getName(), true, context) != null
                                        || helper.isUserExisted(user.getId())){
//                                    Log.d("Comiketter", user.getName());
                                    UserDTO userDTO = new UserDTO(user);
                                    users.add(userDTO);
                                }
                            }

                            publishProgress(1, i * 100 + j);

                            try {
                                RateLimitStatus rateLimit = userResponseList.getRateLimitStatus();
//                                Log.d("Comiketter2", "残りAPI＝" + rateLimit.getRemaining());

                                if (rateLimit.getRemaining() <= 0){
                                    Log.d("Comiketter2", "API切れです。");
                                }
                            } catch (NullPointerException e){
                                return users;
                            }

                            if (this.isCancelled()) return users;
                        }

                        //ここの時点でusersリストはサークル名以外ある程度できている。
                        //サークル名を取得するため、screenNameをOrで繋げて一気に検索をかける
                        if (users.size() > 2){
                            //検索クエリをユーザ全員分生成する
                            List<String> queries = new ArrayList<>();
                            //検索クエリの生成
                            StringBuilder builder = new StringBuilder("");
                            String optionalQuery = "exclude:retweets since:" + TourakuDay + " ◎貴サークル";

                            //０は自分のアカウントなので注意
                            for (Integer user_i = 1; user_i < users.size(); user_i++){
                                //文字数は500文字以内にする。超えると拒否される
                                //スクリーン名の制限は2018-01-01現在15文字＋from等加算で２１文字余裕を見る
                                if (StringMatcher.getComiketName(users.get(user_i).name) != null
                                        || StringMatcher.getEventName(users.get(user_i).name,true, context) != null){
                                    builder.append("from:" + users.get(user_i).screen_name + " OR ");
                                }

                                if (builder.length() + optionalQuery.length() > 479 || user_i == users.size() - 1){
                                    queries.add(builder.substring(0, builder.length() - 3) + optionalQuery);
                                    builder.setLength(0);
                                }
                            }

                            if (queries.size() > 175){
                                Toast.makeText(weakActivity.get(), "サークル名取得対象が多すぎるので、全て取得はできません……", Toast.LENGTH_SHORT).show();
                            }

                            //検索APIのRateLimitが180しかない……
                            Integer searchRateLimit = 180;

                            for (Integer q_i = 0; q_i < queries.size(); q_i++){
//                                Log.d("CircleName", "Query:" + queries.get(q_i));
                                twitter4j.Query q = new Query(queries.get(q_i));
                                twitter4j.QueryResult result = twitter.search(q);
//                                Log.d("CircleName", "残りAPI: " + result.getRateLimitStatus().getRemaining());
//                                Log.d("CircleName", "ツイート数: " + result.getTweets().size());

                                searchRateLimit = result.getRateLimitStatus().getRemaining();

                                if (searchRateLimit < 1) {
                                    break;
                                } else if (result.getTweets().size() == 0) {
                                    continue;
                                } else {
                                    List<twitter4j.Status> statuses = result.getTweets();
                                    String circleName;

                                    for (twitter4j.Status status:statuses){
                                        Long userID = status.getUser().getId();
                                        circleName = StringMatcher.getCircleName(status);

                                        for (UserDTO user:users){
                                            if (user.user_id == userID){
                                                user.circle_name = circleName;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //リストの更新の場合
                        //メンバーカウントを取得
                        UserList userList = twitter.showUserList(targetID);
                        publishProgress(0, userList.getMemberCount());

                        PagableResponseList<User> members;
                        long cursor = -1L;
                        int count = 0;
                        do {
                            members = twitter.getUserListMembers(targetID, cursor);

                            if (members != null){
                                for (User member:members){
                                    //正規表現に一致するユーザのみ抽出。
                                    if (StringMatcher.getComiketName(member.getName()) != null
                                            || StringMatcher.getEventName(member.getName(),true, context) != null
                                            || helper.isUserExisted(member.getId())){
//                                        Log.d("Comiketter", member.getName());
                                        UserDTO userDTO = new UserDTO(member);
                                        users.add(userDTO);
                                    }
                                }

                                try {
                                    RateLimitStatus rateLimit = members.getRateLimitStatus();
//                                    Log.d("Comiketter2", "残りAPI＝" + rateLimit.getRemaining());

                                    if (rateLimit.getRemaining() <= 0){
                                        Log.d("Comiketter2", "API切れです。");
                                        break;
                                    }
                                } catch (NullPointerException e){
                                    break;
                                }
                            } else {
                                Log.d("Comiketter2", "フォローID一覧が取得できませんでした。");
                                return null;
                            }
                            publishProgress(1, members.size() + 20 * count);
                            cursor = members.getNextCursor();
                            count++;
                        } while (members.hasNext());

                        //リストメンバーが０個だったら抜ける
                        if (users.size() <= 0) {
                            return null;
                        }

                        if (this.isCancelled()) return null;
                    }
                } catch (TwitterException ex){
                    ex.printStackTrace();
                    idsList.clear();
                    return null;
                } finally {
                    idsList.clear();
                }
                return users;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
//                Toast.makeText(context, "残りAPI＝" + values[0], Toast.LENGTH_SHORT).show();
                UserLoadDialogFragment dialog = null;

                if (weakDialog.get() == null){
                    return;
                } else {
                    dialog = weakDialog.get();
                }

                if (values[0] == 0){
                    dialog.setUserCount(values[1]);
                } else {
                    dialog.updateNowCount(values[1]);
                }

            }

            @Override
            protected void onCancelled(List<UserDTO> users) {
                this.onPostExecute(users);
            }

            @Override
            protected void onPostExecute(List<UserDTO> users) {
                //ダイアログを閉じる
                //これはダメ実装だ～～～～
                super.onPostExecute(users);
                try{
                    weakDialog.get().dismiss();
                } catch (NullPointerException e){
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        };

        //TaskManagerにタスクを登録
        TaskManager manager = TaskManager.getInstance();
        int taskID = manager.put(task);
        //フィールドにtaskIDを保持
        this.taskID = taskID;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        long listID = preferences.getLong(SELECTED_LIST_ID, 0);
        task.execute(listID);
    }

    private void clearOptionalInfo(){
        //DBのオプションテーブルの入力可能部分を消去
        DialogFragment dialog = ClearDialogFragment.newInstance(getString(R.string.clear_message));
        dialog.show(getSupportFragmentManager(), "clear_confirmation");
    }

    private void startSearchActivity(){
        //サーチ画面を展開
        Intent intent = new Intent(this, com.kc.comiketter2.SearchUserActivity.class);
        startActivityForResult(intent, SearchUserActivity.REQUEST_CODE);
    }

    private void startPreferenceActivity(){
        Intent intent = new Intent(this, MyPreferenceActivity.class);
        startActivityForResult(intent, MyPreferenceActivity.REQUEST_CODE);
    }

    private void startOAuthActivity(){
        //認証画面を展開
        Intent intent = new Intent(this, com.kc.comiketter2.ConfirmOAuthActivity.class);
        startActivityForResult(intent, ConfirmOAuthActivity.REQUEST_CODE);
    }

    @Override
    public void onComplete(Object result) {
        //AsyncTask完了後のコールバック
        if (result instanceof List){
            List<UserDTO> users = (List<UserDTO>)result;

            Log.d("Comiketter2", "処理終了！");
            Log.d("Comiketter2", "users.size() = " + users.size());

            if (users.size() > 0){
                DatabaseHelper helper = DatabaseHelper.getInstance(this);
                //データの更新
                UserDTO myself = users.get(0);
                SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefMyself.edit();
                editor.putLong("my_id", myself.user_id);
                editor.putString("name", myself.name);
                editor.putString("screen_name", myself.screen_name);
                editor.putString("profile_image_url", myself.profile_image_url);
                editor.apply();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                long listID = preferences.getLong(SELECTED_LIST_ID, 0);
                helper.updateUserInfo(listID, users, this);

                ImageButton btn = findViewById(R.id.navigation_icon);
                Glide.with(this).load(myself.profile_image_url).into(btn);
                TabLayout tabLayout = findViewById(R.id.tab_layout);
                updateDrawer(); //リスト一覧の表示更新
                onPageSelected(tabLayout.getSelectedTabPosition());

                AsyncTask<List<UserDTO>, Void, Integer> task = new AsyncTask<List<UserDTO>, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(List<UserDTO>... params) {
                        List<UserDTO> users = params[0];
                        int filteredUsers = 0;
                        for (int user_i = 0; user_i < users.size(); user_i++){
                            String eventName = StringMatcher.getEventName(users.get(user_i).name, true, MainActivity.this);
                            if (!(eventName == null || eventName.equals(""))){
                                filteredUsers++;
                            }
                        }
                        return filteredUsers;
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);
                        if (result == 0){
                            Toast.makeText(MainActivity.this, getString(R.string.no_users), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, String.valueOf(result) + "人見つかりました！", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                task.execute(users);

            } else {

            }
        } else {

        }

    }

    @Override
    public void onDialogCreated(UserLoadDialogFragment dialog) {
        //画面再生成時、新しいDialogをTaskに設定する。
        Log.d("Comiketter2", "onDialogCreated, taskID = " + String.valueOf(taskID));
        TaskManager manager = TaskManager.getInstance();
        MyAsyncTask<Void, Integer, List<User>> task = (MyAsyncTask<Void, Integer, List<User>>)manager.get(taskID);

        if (task != null){
            task.weakDialog = new WeakReference<>(dialog);
        }
    }

    @Override
    public void onDialogCancelled() {
        Log.d("Comiketter2", "onDialogCancelled, taskID = " + String.valueOf(taskID));
        TaskManager manager = TaskManager.getInstance();
        MyAsyncTask<Void, Integer, List<User>> task = (MyAsyncTask<Void, Integer, List<User>>)manager.get(taskID);

        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING){
            task.cancel(false);
            Toast.makeText(this, R.string.cancel_now, Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + position);

        if (fragment instanceof IUpdater){
            ((IUpdater)fragment).update();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPositiveButtonClicked() {
        //表示中フラグメントのupdate()をたたく
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        int position = tabLayout.getSelectedTabPosition();
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + position);

        if (fragment instanceof StickyListFragment && fragment instanceof IUpdater){
            //DataBaseHelper#clearOptionalInfoをたたく
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            //一応clearする前に位置を保存
            ((StickyListFragment)fragment).saveScrollY();
            helper.clearOptionalInfo();
            ((IUpdater)fragment).update();
        }
        updateTotalYosan();
    }

    public void updateStickyList(){
        //表示中フラグメントのupdate()をたたく
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        int position = tabLayout.getSelectedTabPosition();
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + position);

        if (fragment instanceof StickyListFragment && fragment instanceof IUpdater){
            //一応clearする前に位置を保存
            ((StickyListFragment)fragment).saveScrollY();
            ((IUpdater)fragment).update();
        }
    }

    public void updateToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView selectedListName = toolbar.findViewById(R.id.toolbar_constraint).findViewById(R.id.selected_list_name);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long listID = preferences.getLong(SELECTED_LIST_ID, 0);
        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        ListDTO listDTO = helper.getListDTO(this, listID);
        selectedListName.setText(listDTO.name);
    }

    public void updateDrawer(){
        //Drawer表示項目を更新
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ConstraintLayout includeDrawer = drawerLayout.findViewById(R.id.include_drawer);
        ConstraintLayout headerLayout = includeDrawer.findViewById(R.id.header_layout);
        ImageView icon = headerLayout.findViewById(R.id.my_icon);
        TextView myName = headerLayout.findViewById(R.id.my_name);

        SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
        Glide.with(this).load(prefMyself.getString("profile_image_url", "")).into(icon);
        myName.setText(prefMyself.getString("name", ""));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long myID = preferences.getLong(MY_ID, 0);
        ListView listView = includeDrawer.findViewById(R.id.event_list);
        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        List<ListDTO> listDTOs = helper.getLists(this, myID);
        ListDTOAdapter adapter = (ListDTOAdapter) listView.getAdapter();
        adapter.clear();
        for (ListDTO listDTO:listDTOs){
            adapter.add(listDTO);
        }
    }

    public void updateTotalYosan(){
        Log.d("Yosan", "updateTotalYosan");
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ConstraintLayout includeDrawer = drawerLayout.findViewById(R.id.include_drawer);
        ConstraintLayout headerLayout = includeDrawer.findViewById(R.id.header_layout);
        TextView totalYosan = headerLayout.findViewById(R.id.total_yosan);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long myID = preferences.getLong(MY_ID, 0);
        long listID = preferences.getLong(SELECTED_LIST_ID, 0);

        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        int yosan = helper.getTotalYosan(myID, listID, this);
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        totalYosan.setText(numberFormat.format(yosan));
        totalYosan.append(getString(R.string.yen));
    }

    private boolean isFiltered(){
        for (int filter_i = 0; filter_i < MyPreferenceFragment.FILTER_COUNT ; filter_i++){
            SharedPreferences preferences = getSharedPreferences("filter" + filter_i, Context.MODE_PRIVATE);
            if (preferences.getBoolean(EditAndCheckablePreference.CHECKED, false)){
                return true;
            }
        }
        return false;
    }
}
