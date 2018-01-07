package com.kc.comiketter2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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

//import android.support.v4.app.Fragment;

public class MainActivity extends AppCompatActivity implements MyAsyncTask.IAsyncTaskCallback, UserLoadDialogFragment.IDialogControl, ViewPager.OnPageChangeListener, ClearDialogFragment.Callback{
    //画面回転時、 task == null（onCreate） となってしまうので、
    //とりあえず TwitterUtils.task に参照を退避するようにしている。
    //task,dialogで弱参照しているActivity,DialogFragmentが軒並み参照先を失ってしまうので、
    //相互にコールバック持たせてMainで参照を持たせてやってる。スパゲッティ。
    //やばみ。
    //→TaskManagerクラスを作成。
    private MyAsyncTask<Void, Integer, List<User>> task = null;
    private Integer taskID = -1;
    private SearchView searchView;
    private static final String TourakuDay = "2017-11-02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                } else {

                }
                return true;
            }
        });

        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.toolbar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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


        SharedPreferences prefMyself = getSharedPreferences("myself", Context.MODE_PRIVATE);
        String profile_image_url = prefMyself.getString("profile_image_url", null);
        if (profile_image_url != null){
            ImageButton btn = findViewById(R.id.navigation_icon);
            Glide.with(this).load(profile_image_url).into(btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Toolbar", "NavigationIcon Clicked");
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
                Long myUserID = TwitterUtils.loadAccessToken(context).getUserId();

                try {
                    //自身のプロフィールを取得
                    User myself = twitter.verifyCredentials();
                    UserDTO myselfDTO = new UserDTO(myself);
                    users.add(myselfDTO);

                    //フォローユーザID一覧を取得
                    Long cursor = -1L;

                    do {
                        ids = twitter.getFriendsIDs(myUserID, cursor);

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
                            if (StringMatcher.getEventName(user.getName()) != null){
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
                            builder.append("from:" + users.get(user_i).screen_name + " OR ");
                            if (builder.length() + optionalQuery.length() > 479 || user_i == users.size() - 1){
                                queries.add(builder.substring(0, builder.length() - 3) + optionalQuery);
                                builder.setLength(0);
                            }
                        }

                        //検索APIのRateLimitが180しかない……
                        Integer searchRateLimit = 180;

                        for (String query:queries){
                            Log.d("CircleName", "Query:" + query);
                            twitter4j.Query q = new Query(query);
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
                    editor.putLong("_id", myself.user_id);
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
}
