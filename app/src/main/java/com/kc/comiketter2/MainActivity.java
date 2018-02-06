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
import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
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
        ClearDialogFragment.Callback,
        IObserver{
    //画面回転時、 task == null（onCreate） となってしまうので、
    //とりあえず TwitterUtils.task に参照を退避するようにしている。
    //task,dialogで弱参照しているActivity,DialogFragmentが軒並み参照先を失ってしまうので、
    //相互にコールバック持たせてMainで参照を持たせてやってる。
    //→TaskManagerクラスを作成。
    private MyAsyncTask<Void, Integer, List<User>> task = null;
    private Integer taskID = -1;
    private static final String TourakuDay = "2017-11-02";
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SearchUserActivity.REQUEST_CODE && data != null && resultCode == RESULT_OK){
            TabLayout tabLayout = findViewById(R.id.tab_layout);
            onPageSelected(tabLayout.getSelectedTabPosition());
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

        //TwitterUtils.deleteAccessToken(this);

        if (TwitterUtils.loadAccessToken(this) == null){
            //OAuth認証画面
            startActivity(new Intent(this, com.kc.comiketter2.ConfirmOAuthActivity.class));
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
        final ListView leftDrawer = includeDrawer.findViewById(R.id.left_drawer);
        Button subscribeList = includeDrawer.findViewById(R.id.subscribeList);
        subscribeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //購読リスト選択ダイアログを表示
            }
        });

        SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
        long myID = prefMyself.getLong("my_id", 0);
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

        ListDTOAdapter listDTOAdapter = new ListDTOAdapter(this);
        //フォロー一覧
        ListDTO followDTO = new ListDTO();
        followDTO.name = getString(R.string.all_follow);
        followDTO.subscribed = true;
        listDTOAdapter.add(followDTO);

        final DatabaseHelper helper = DatabaseHelper.getInstance(this);
        List<ListDTO> listDTOs = helper.getLists(myID);
        if (listDTOs != null){
            for (int list_i = 0; list_i < listDTOs.size(); list_i++){
                listDTOAdapter.add(listDTOs.get(list_i));
//                if (listDTOs.get(list_i).subscribed){
//                    listDTOAdapter.add(listDTOs.get(list_i));
//                }
            }
        }
        leftDrawer.setAdapter(listDTOAdapter);
        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //リストクリック時の処理
                ListDTOAdapter adapter = (ListDTOAdapter) leftDrawer.getAdapter();

                ListDTO listDTO = adapter.getItem(i);
                listDTO.subscribed = true;

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("selected_list_id", listDTO.listID);
                editor.apply();

                contentsUpdate();
                MainActivity.this.onBackPressed();
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
                setTitle("test open");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("DrawerToggle", "Closed");
                setTitle(R.string.app_name);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);

        //TabLayoutの設定
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

//        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.toolbar_search).getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                FragmentManager manager = getSupportFragmentManager();
//                Fragment fragment = manager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + tabLayout.getSelectedTabPosition());
//                StickyListHeadersListView sticky = null;
//                if (tabLayout.getSelectedTabPosition() <= 1){
//                    sticky = fragment.getView().findViewById(R.id.sticky_list);
//                }
//
//                if (sticky == null){
//                    Log.d("Comiketter", "sticky == null");
//                } else {
//                    if (newText.equals("")){
//
//                    } else {
//
//                    }
//                }
//                return false;
//            }
//        });

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
        toolbar.inflateMenu(R.menu.menu_main);
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
                } else {

                }
                return true;
            }
        });
//        SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
//        String profile_image_url = prefMyself.getString("profile_image_url", null);
        if (myID != 0){
            ImageButton btn = findViewById(R.id.navigation_icon);
            Glide.with(this).load(prefMyself.getString("profile_image_url", null)).into(btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Toolbar", "NavigationIcon Clicked");
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            });
        }

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
        TaskManager manager = TaskManager.getInstance();
        MyAsyncTask<Void, Integer, List<User>> task = (MyAsyncTask<Void, Integer, List<User>>)manager.get(taskID);

        if (task != null){
            Log.d("Comiketter2", task.getStatus().toString());
            task.weakActivity = new WeakReference<Activity>(this);
        }
    }

    private void getFriendIDs(){
        final Context context = this;

        MyAsyncTask<Void, Integer, List<UserDTO>> task = new MyAsyncTask<Void,Integer,List<UserDTO>>(this){
            @Override
            protected void onPreExecute() {
                //ダイアログを表示
                UserLoadDialogFragment dialog = UserLoadDialogFragment.newInstance(getString(R.string.loading));

                dialog.show(getSupportFragmentManager(), "user_load");
                weakDialog = new WeakReference<>(dialog);
            }

            @Override
            protected List<UserDTO> doInBackground(Void... params) {
                Twitter twitter = TwitterUtils.getTwitter(context);

                List<UserDTO> users = new ArrayList<>();
                List<Long> idsList = new ArrayList<>();
                IDs ids = null;
                long myID = TwitterUtils.loadAccessToken(context).getUserId();
                DatabaseHelper helper = DatabaseHelper.getInstance(context);

                try {
                    //自身のプロフィールを取得
                    User myself = twitter.verifyCredentials();
                    UserDTO myselfDTO = new UserDTO(myself);
                    users.add(myselfDTO);

                    //リスト一覧の取得
                    List<UserList> userLists = twitter.getUserLists(myID);
                    List<ListDTO> listDTOs = new ArrayList<>();
                    for (UserList userList:userLists){
                        ListDTO listDTO = new ListDTO();
                        listDTO.listID = userList.getId();
                        listDTO.name = userList.getName();
                        listDTOs.add(listDTO);
                    }
                    helper.updateLists(myID, listDTOs);

                    //フォローユーザID一覧を取得
                    Long cursor = -1L;
                    do {
                        ids = twitter.getFriendsIDs(myID, cursor);

                        if (ids != null){
                            for (Long id:ids.getIDs()){
                                idsList.add(id);
                            }

                            RateLimitStatus rateLimit = ids.getRateLimitStatus();
                            Log.d("Comiketter2", "残りAPI＝" + rateLimit.getRemaining());

                            if (rateLimit.getRemaining() <= 0){
                                Log.d("Comiketter2", "API切れです。");
                                break;
                            }

                        } else {
                            Log.d("Comiketter2", "フォローID一覧が取得できませんでした。");
                            return null;
                        }
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
                        ResponseList<User> userResponseList = null;
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

                        userResponseList = TwitterUtils.getTwitter(context).lookupUsers(array);

                        for (User user:userResponseList){
                            //正規表現に一致するユーザのみ抽出。
                            if (StringMatcher.getEventName(user.getName()) != null || helper.isExisted(user.getId())){
                                Log.d("Comiketter", user.getName());
                                UserDTO userDTO = new UserDTO(user);
                                users.add(userDTO);
                            }
                        }

                        publishProgress(1, i * 100 + j);

                        RateLimitStatus rateLimit = userResponseList.getRateLimitStatus();
                        Log.d("Comiketter2", "残りAPI＝" + rateLimit.getRemaining());

                        if (rateLimit.getRemaining() <= 0){
                            Log.d("Comiketter2", "API切れです。");
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
                            if (StringMatcher.getEventName(users.get(user_i).name) != null){
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
                            Log.d("CircleName", "Query:" + queries.get(q_i));
                            twitter4j.Query q = new Query(queries.get(q_i));
                            twitter4j.QueryResult result = twitter.search(q);
                            Log.d("CircleName", "残りAPI: " + result.getRateLimitStatus().getRemaining());
                            Log.d("CircleName", "ツイート数: " + result.getTweets().size());

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
                super.onPostExecute(users);
                try{
                    weakDialog.get().dismiss();
                } catch (NullPointerException e){

                }
            }
        };

        //TaskManagerにタスクを登録
        TaskManager manager = TaskManager.getInstance();
        Integer taskID = manager.put(task);
        //フィールドにtaskIDを保持
        this.taskID = taskID;
        task.execute();
    }

    private void clearOptionalInfo(){
        //DBのオプションテーブルの入力可能部分を消去
        ClearDialogFragment dialog = ClearDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), "clear_confirmation");
    }

    private void startSearchActivity(){
        //サーチ画面を展開
        Intent intent = new Intent(this, com.kc.comiketter2.SearchUserActivity.class);

        startActivityForResult(intent, SearchUserActivity.REQUEST_CODE);
    }

    @Override
    public void onComplete(Object result) {
        //AsyncTask完了後のコールバック
        if (result instanceof List){
            if (((List) result).get(0) instanceof UserDTO){
                //result == List<UserDTO>
                List<UserDTO> users = (List<UserDTO>)result;

                if (users.size() > 0){
                    Toast.makeText(this, "Finish!", Toast.LENGTH_SHORT).show();
                    Log.d("Comiketter2", "処理終了！");
                    Log.d("Comiketter2", "users.size() = " + users.size());

                    UserDTO myself = users.get(0);
                    SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefMyself.edit();
                    editor.putLong("my_id", myself.user_id);
                    editor.putString("name", myself.name);
                    editor.putString("screen_name", myself.screen_name);
                    editor.putString("profile_image_url", myself.profile_image_url);
                    editor.apply();

                    DatabaseHelper helper = DatabaseHelper.getInstance(this);
                    helper.updateUserInfo(users);

                    ImageButton btn = findViewById(R.id.navigation_icon);
                    Glide.with(this).load(myself.profile_image_url).into(btn);

                    TabLayout tabLayout = findViewById(R.id.tab_layout);
                    onPageSelected(tabLayout.getSelectedTabPosition());
                } else {
                    return;
                }
            } else {
                //result == List<?>
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

        if (fragment instanceof IObserver){
            IObserver observer = (IObserver) fragment;
            observer.update();
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

        if (fragment instanceof StickyListFragment && fragment instanceof IObserver){
            //DataBaseHelper#clearOptionalInfoをたたく
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            //一応clearする前に位置を保存
            ((StickyListFragment)fragment).saveScrollY();
            helper.clearOptionalInfo();
            ((IObserver)fragment).update();
        }
    }

    @Override
    public void update() {
        //DrawerLayout, Toolbar, ViewPagerの表示更新
    }

    public void contentsUpdate(){
        //ViewPager内コンテンツの更新
    }
}
