package com.kc.comiketter2.model.data.database_helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;

import com.kc.comiketter2.R;
import com.kc.comiketter2.UserDTO;
import com.kc.comiketter2.drawer.ListDTO;
import com.kc.comiketter2.main.MainActivity;
import com.kc.comiketter2.prefs.EditAndCheckablePreference;
import com.kc.comiketter2.ui.MyPreferenceFragment;
import com.kc.comiketter2.util.StringMatcher;

import java.util.ArrayList;
import java.util.List;

import twitter4j.auth.AccessToken;

/**
 * Created by HIDE on 2017/11/12.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
  public final String USER_INFO = "user_info";
  public final String OPTIONAL_INFO = "optional_info";
  public final String MULTI_ACCOUNTS = "multi_accounts";
  public final String LIST_INFO = "list_info";
  public final String RELATION_INFO = "relation_info";

  //ユーザ情報テーブルの作成クエリ
  private final String USER_QUERY = "create table " + USER_INFO + " ("
      + "_id integer primary key not null, "
      + "name text not null, "
      + "screen_name text not null, "
      + "profile_image_url text, "
      + "profile_description text )";

  //付加情報テーブルの作成クエリ
  //auto_day : StringMatcherで自動判別した日付
  //manual_day : クライアントが手動で入力した日付
  //自動判別が間違える可能性があるため。手動日が入力されていればそちらを優先して使用する。
  private final String OPTIONAL_QUERY = "create table " + OPTIONAL_INFO + " ("
      + "_id integer primary key not null, "
      + "auto_day integer not null, "
      + "manual_day integer, "
      + "circle_name text, "
      + "circle_space text, "
      + "target integer, "
      + "busuu integer, "
      + "yosan integer, "
      + "memo text, "
      + "pickup integer, "
      + "hasgot integer )";

  //_id：my_id
  //storeAccessTokenの関係で_id以外はNULL制約かけられない
  private final String MULTI_ACCOUNT_QUERY = "create table " + MULTI_ACCOUNTS + " ("
      + "_id integer primary key not null, "
      + "name text, "
      + "screen_name text, "
      + "profile_image_url text, "
      + "token text, "
      + "token_secret text, "
      + "unique (token, token_secret))";

  //_id：list_id
  //subscribed：リスト自体はDatabaseに登録しておき、購読メソッド実行で１を代入
  private final String LIST_INFO_QUERY = "create table " + LIST_INFO + " ("
      + "_id integer primary key not null, "
      + "name string not null, "
      + "my_id integer not null, "
      + "subscribed integer not null )";

  //relation_id=0でフォロー、=list_idでリストID
  //フォローやリスト１件ごとに登録。
  private final String RELATION_INFO_QUERY = "create table " + RELATION_INFO + " ("
      + "_id integer primary key autoincrement, "
      + "my_id integer not null, "
      + "relation_id integer not null, "
      + "user_id integer not null )";


  private static DatabaseHelper helper;
  private static final Integer DB_VERSION = 3; //DBスキーマを変更した場合、これをインクリメントする

  private DatabaseHelper(Context context) {
    super(context, "usersDB", null, DB_VERSION);
  }

  public static DatabaseHelper getInstance(Context context) {
    if (helper == null) {
      helper = new DatabaseHelper(context);
    }
    return helper;
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(USER_QUERY);
    database.execSQL(OPTIONAL_QUERY);
    database.execSQL(MULTI_ACCOUNT_QUERY);
    database.execSQL(LIST_INFO_QUERY);
    database.execSQL(RELATION_INFO_QUERY);
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    // DBバージョンアップ時のデータ移行を実装
    if (oldVersion < newVersion) {
      if (oldVersion == 1 && newVersion == 2) {
        //OPTIONAL_INFOテーブルへのhole_id列追加、HOLE_NAMESテーブルの追加
        //→対応終了
      } else if (oldVersion == 2 && newVersion == 3) {
        //複アカ、リスト対応
        try {
          database.execSQL(MULTI_ACCOUNT_QUERY);
        } catch (SQLiteException ex) {
          ex.printStackTrace();
        }

        try {
          database.execSQL(LIST_INFO_QUERY);
        } catch (SQLiteException ex) {
          ex.printStackTrace();
        }

        try {
          database.execSQL(RELATION_INFO_QUERY);
        } catch (SQLiteException ex) {
          ex.printStackTrace();
        }
      } else if (oldVersion == 3 && newVersion == 4) {
        //ダイアログ表示して再構築してよいか伺う。
        //全テーブルのドロップ、再構築

      }
    }
  }

  /**
   * relationID=0:フォロー、relationID=listID:リスト入り
   *
   * @param relationID
   * @param users
   */
  public void updateUserInfo(long relationID, List<UserDTO> users, Context context) {
    UserDTO myAccount = users.get(0);

    //登録済みID一覧を取得。Relation_Infoテーブルと結合
    SQLiteDatabase readable = getReadableDatabase();
    StringBuilder query1 = new StringBuilder();
    StringBuilder query2 = new StringBuilder();
    query1.append("select * from ").append(OPTIONAL_INFO).append(" inner join ").append(RELATION_INFO).append(" on ").append(OPTIONAL_INFO).append("._id = ").append(RELATION_INFO).append(".user_id");
    query2.append("select * from ( ").append(query1).append(" ) u where relation_id = ").append(relationID).append(";");
    Cursor cursor = readable.rawQuery(query2.toString(), null);

    List<Long> registeredIDs = new ArrayList<>();

    boolean eol = cursor.moveToFirst();
    while (eol) {
      registeredIDs.add(cursor.getLong(cursor.getColumnIndex("_id")));
      eol = cursor.moveToNext();
    }

    cursor.close();
    readable.close();

    SQLiteDatabase writable = getWritableDatabase();
    writable.beginTransaction();
    {
      try {
        //自アカ情報の更新
        ContentValues args = new ContentValues();
        args.put("_id", myAccount.user_id);
        args.put("name", myAccount.name);
        args.put("screen_name", myAccount.screen_name);
        args.put("profile_image_url", myAccount.profile_image_url);
        args.put("token", myAccount.token);
        args.put("token_secret", myAccount.token_secret);
        int conflictMYID = (int) writable.insertWithOnConflict(MULTI_ACCOUNTS, null, args, SQLiteDatabase.CONFLICT_IGNORE);
        if (conflictMYID != -1) {
          //競合発生
          String filter = "_id = " + myAccount.user_id;
          ContentValues args2 = new ContentValues();
          args2.put("name", myAccount.name);
          args2.put("screen_name", myAccount.screen_name);
          args2.put("profile_image_url", myAccount.profile_image_url);
          writable.update(MULTI_ACCOUNTS, args2, filter, null);
        }

        //更新前準備。スペース、日付をリセットする。サークル名はリセットしない。当落発表時くらいしかキャッチするタイミングがないため。
        for (int user_i = 0; user_i < registeredIDs.size(); user_i++) {
          String filter = "_id = " + registeredIDs.get(user_i);
          ContentValues cv = new ContentValues();
          cv.put("auto_day", 99);
          cv.put("circle_space", "");
          writable.update(OPTIONAL_INFO, cv, filter, null);
        }

        //RELATION_INFO query
        //RELATION_INFOテーブルより、一度削除
        String relationDelete = "my_id = ? and relation_id = ?";
        String[] deleteArgs = new String[]{String.valueOf(myAccount.user_id), String.valueOf(relationID)};
        writable.delete(RELATION_INFO, relationDelete, deleteArgs);

        for (int user_i = 1; user_i < users.size(); user_i++) {
          UserDTO user = users.get(user_i);

          //USER_INFO query
          String replaceQuery = "Insert or Replace into " + USER_INFO + " ("
              + "_id, "
              + "name, "
              + "screen_name, "
              + "profile_image_url, "
              + "profile_description) "
              + "Values (?, ?, ?, ?, ?)";

          SQLiteStatement replaceUserInfo = writable.compileStatement(replaceQuery);
          replaceUserInfo.bindLong(1, user.user_id);
          replaceUserInfo.bindString(2, user.name);
          replaceUserInfo.bindString(3, user.screen_name);
          replaceUserInfo.bindString(4, user.profile_image_url);
          replaceUserInfo.bindString(5, user.profile_description);

          replaceUserInfo.execute();

          //OPTIONAL_INFO query
          Integer autoDay = StringMatcher.getParticipateDay(user.name);
          String space = StringMatcher.getSpace(user.name);

          ContentValues instantValues = new ContentValues();
          instantValues.put("_id", user.user_id);
          instantValues.put("auto_day", autoDay);
          instantValues.put("circle_space", space);
          if (user.circle_name != null) {
            instantValues.put("circle_name", user.circle_name);
          } else {
            instantValues.put("circle_name", "");
            user.circle_name = "";
          }

          int conflictOPID = (int) writable.insertWithOnConflict(OPTIONAL_INFO, null, instantValues, SQLiteDatabase.CONFLICT_IGNORE);
          if (conflictOPID == -1) {
            String filter = "_id = " + user.user_id;
            ContentValues instantValues2 = new ContentValues();
            instantValues2.put("auto_day", autoDay);
//                        Log.d(this.getClass().getSimpleName(), user.name);
            //当落発表ツイが流れてしまうとnullになってしまうためnullチェック
            if (!user.circle_name.equals("")) {
              instantValues2.put("circle_name", user.circle_name);
            }

            instantValues2.put("circle_space", space);
            writable.update(OPTIONAL_INFO, instantValues2, filter, null);
          }

          //RELATION_INFO Insert
          ContentValues relationCV = new ContentValues();
          relationCV.put("my_id", myAccount.user_id);
          relationCV.put("user_id", user.user_id);
          relationCV.put("relation_id", relationID);
          writable.insert(RELATION_INFO, null, relationCV);
        }

        writable.setTransactionSuccessful();
      } catch (SQLiteException e) {
        e.printStackTrace();
      } finally {
        writable.endTransaction();
        writable.close();
      }
    }
  }

  //複数アカウント用、リスト対応用
  //listID = 0ならば、そのmyIDのすべての参加サークルを返す
  public List<UserDTO> getUserList(long myID, long listID) {
    List<UserDTO> users = new ArrayList<>();
    SQLiteDatabase readable = getReadableDatabase();

    StringBuilder queryBuilder1 = new StringBuilder();
    StringBuilder queryBuilder2 = new StringBuilder();
    StringBuilder queryBuilder3 = new StringBuilder();

    //query1
    queryBuilder1.append("select * from ").append(USER_INFO).append(" inner join ").append(OPTIONAL_INFO).append(" on ").append(USER_INFO).append("._id = ").append(OPTIONAL_INFO).append("._id");
    //query2
    queryBuilder2.append("select * from ( ").append(queryBuilder1).append(" ) u inner join ").append(RELATION_INFO).append(" on u._id = ").append(RELATION_INFO).append(".user_id");
    //query3
    queryBuilder3.append("select * from ( ").append(queryBuilder2).append(" ) v where v.my_id = ").append(myID).append(" and v.relation_id = ").append(listID);
    queryBuilder3.append(" order by v.auto_day ASC, v.circle_space ASC;");

//        Log.d("Query", queryBuilder3.toString());

    Cursor cursor = readable.rawQuery(queryBuilder3.toString(), null);

    boolean eol = cursor.moveToFirst();
    while (eol) {
      UserDTO user = new UserDTO();
      user.user_id = cursor.getLong(cursor.getColumnIndex("_id"));
      user.name = cursor.getString(cursor.getColumnIndex("name"));
      user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
      user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url")).replaceAll("':", ":");
      user.profile_description = cursor.getString(cursor.getColumnIndex("profile_description")).replaceAll("':", ":");
      user.auto_day = cursor.getInt(cursor.getColumnIndex("auto_day"));
      user.manual_day = cursor.getInt(cursor.getColumnIndex("manual_day"));
      user.circle_space = cursor.getString(cursor.getColumnIndex("circle_space"));
      user.circle_name = cursor.getString(cursor.getColumnIndex("circle_name"));
      user.target = cursor.getInt(cursor.getColumnIndex("target"));
      user.busuu = cursor.getInt(cursor.getColumnIndex("busuu"));
      user.yosan = cursor.getInt(cursor.getColumnIndex("yosan"));
      user.memo = cursor.getString(cursor.getColumnIndex("memo"));
      user.pickup = cursor.getInt(cursor.getColumnIndex("pickup"));
      user.hasgot = cursor.getInt(cursor.getColumnIndex("hasgot"));

      users.add(user);
      eol = cursor.moveToNext();
    }

    cursor.close();


    return users;
  }

  public UserDTO getUser(long userID) {
    UserDTO user = new UserDTO();
    SQLiteDatabase database = getReadableDatabase();

    String query1 = "select * from " + USER_INFO + " inner join " + OPTIONAL_INFO + " on " + USER_INFO + "._id = " + OPTIONAL_INFO + "._id";
    String query2 = "select * from ( " + query1 + " ) u where u._id = " + userID + ";";

    Cursor cursor = database.rawQuery(query2, null);
    cursor.moveToFirst();

    user.user_id = cursor.getLong(cursor.getColumnIndex("_id"));
    user.name = cursor.getString(cursor.getColumnIndex("name"));
    user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
    user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url")).replaceAll("':", ":");
    user.profile_description = cursor.getString(cursor.getColumnIndex("profile_description")).replaceAll("':", ":");
    user.auto_day = cursor.getInt(cursor.getColumnIndex("auto_day"));
    user.manual_day = cursor.getInt(cursor.getColumnIndex("manual_day"));
    user.circle_space = cursor.getString(cursor.getColumnIndex("circle_space"));
    user.circle_name = cursor.getString(cursor.getColumnIndex("circle_name"));
    user.target = cursor.getInt(cursor.getColumnIndex("target"));
    user.busuu = cursor.getInt(cursor.getColumnIndex("busuu"));
    user.yosan = cursor.getInt(cursor.getColumnIndex("yosan"));
    user.memo = cursor.getString(cursor.getColumnIndex("memo"));
    user.pickup = cursor.getInt(cursor.getColumnIndex("pickup"));
    user.hasgot = cursor.getInt(cursor.getColumnIndex("hasgot"));

    cursor.close();

    return user;
  }

  @Deprecated
  public void setValue(String tableName, Long userID, String columnName, Integer value) {
    setValue(tableName, userID, columnName, String.valueOf(value));
  }

  @Deprecated
  public void setValue(String tableName, Long userID, String columnName, String value) {
    SQLiteDatabase database = getWritableDatabase();

    //OPTIONAL_INFO query
    String filter = "_id = " + userID;
    ContentValues args = new ContentValues();
    args.put(columnName, value);
    database.update(tableName, args, filter, null);
    database.close();

//        Log.d("UserID", "_id = " + userID + " , column = " + columnName);
  }

  public void clearOptionalInfo() {
    //userID一覧とサイズの取得
    SQLiteDatabase readable = getReadableDatabase();
    String query = "select _id from " + OPTIONAL_INFO + ";";
    Cursor cursor = readable.rawQuery(query, null);

    List<Long> userIDs = new ArrayList<>();

    boolean eol = cursor.moveToFirst();
    while (eol) {
      Long userID = cursor.getLong(0);
      userIDs.add(userID);
      eol = cursor.moveToNext();
    }
    cursor.close();

    int user_count = userIDs.size();

    //DataBaseへの書き込み
    SQLiteDatabase writable = getWritableDatabase();
    writable.beginTransaction();
    {
      try {
        for (Integer user_i = 0; user_i < user_count; user_i++) {
          String filter = "_id = " + userIDs.get(user_i);
          ContentValues cv = new ContentValues();
          cv.put("target", 0);
          cv.put("busuu", 0);
          cv.put("yosan", 0);
          cv.put("memo", "");
          cv.put("pickup", 0);
          cv.put("hasgot", 0);

          writable.update(OPTIONAL_INFO, cv, filter, null);
        }
        writable.setTransactionSuccessful();
      } catch (SQLException ex) {
        ex.printStackTrace();
      } finally {
        writable.endTransaction();
        writable.close();
      }
    }

  }

  public boolean clearData() {
    //自身のアカウント以外の全消去
    boolean ret = false;
    SQLiteDatabase writable = getWritableDatabase();
    String sql = "delete from ";
    writable.beginTransaction();
    {
      try {
        writable.execSQL(sql + USER_INFO);
        writable.execSQL(sql + OPTIONAL_INFO);
//                writable.execSQL(sql + LIST_INFO);
        writable.execSQL(sql + RELATION_INFO);
        ret = true;
        writable.setTransactionSuccessful();
      } catch (SQLiteException e) {
        e.printStackTrace();
      } finally {
        writable.endTransaction();
        writable.execSQL("vacuum");
        writable.close();
      }
    }
    return ret;
  }

  public List<UserDTO> search(String word) {
    //name,screenName,description,circleNameから検索
    //正規表現的なマッチングにしたい
    List<UserDTO> users = new ArrayList<>();
    if (word.equals("")) {
      return users;
    } else {
      word = word.replace("'", "''");
    }

    StringBuilder builder = new StringBuilder();
    builder.append("'%").append(word).append("%'");

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select * from ( ")
        .append("select * from ").append(USER_INFO).append(" inner join ").append(OPTIONAL_INFO).append(" on ").append(USER_INFO).append("._id = ").append(OPTIONAL_INFO).append("._id") //内部結合
        .append(" ) u where u.name like ").append(builder.toString())
        .append(" or ")
        .append("u.screen_name like ").append(builder.toString())
        .append(" or ")
        .append("u.circle_name like ").append(builder.toString()) //orで条件増やす場合はここ！
        .append(";");

    SQLiteDatabase readable = getReadableDatabase();
    Cursor cursor = readable.rawQuery(queryBuilder.toString(), null);

    boolean eol = cursor.moveToFirst();
    while (eol) {
      UserDTO user = new UserDTO();
      user.user_id = cursor.getLong(cursor.getColumnIndex("_id"));
      user.name = cursor.getString(cursor.getColumnIndex("name"));
      user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
      user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url"));
      user.pickup = cursor.getInt(cursor.getColumnIndex("pickup"));
      user.circle_space = cursor.getString(cursor.getColumnIndex("circle_space"));
      user.circle_name = cursor.getString(cursor.getColumnIndex("circle_name"));
      users.add(user);
      eol = cursor.moveToNext();
    }

    cursor.close();
    return users;
  }

  public Boolean isUserExisted(Long userID) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select _id from ").append(USER_INFO).append(" where _id = ").append(userID).append(";");

    SQLiteDatabase readable = getReadableDatabase();
    Cursor cursor = readable.rawQuery(queryBuilder.toString(), null);

    Boolean ret;
    if (cursor.getCount() == 0) {
      ret = false;
    } else {
      ret = true;
    }

    cursor.close();
    return ret;

  }

  //リストID、名前一覧の取得。０の場合も有り
  public List<ListDTO> getLists(Context context, long myID) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select * from ").append(LIST_INFO).append(" where my_id = ").append(myID).append(" order by _id DESC;");

    SQLiteDatabase readable = getReadableDatabase();
    Cursor cursor = readable.rawQuery(queryBuilder.toString(), null);

    List<ListDTO> list = new ArrayList<>();
    //フォロー一覧
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    long selectedListID = preferences.getLong(MainActivity.SELECTED_LIST_ID, 0);
    ListDTO followDTO = new ListDTO();
    followDTO.name = context.getString(R.string.all_follow);
    if (selectedListID == 0) {
      followDTO.subscribed = true;
    }
    list.add(followDTO);

    boolean eol = cursor.moveToFirst();
    while (eol) {
      ListDTO listDTO = new ListDTO();
      listDTO.listID = cursor.getLong(cursor.getColumnIndex("_id"));
      listDTO.name = cursor.getString(cursor.getColumnIndex("name"));
      if (cursor.getInt(cursor.getColumnIndex("subscribed")) == 0) {
        listDTO.subscribed = false;
      } else {
        listDTO.subscribed = true;
      }
      list.add(listDTO);
      eol = cursor.moveToNext();
    }

    cursor.close();
    return list;
  }

  public ListDTO getListDTO(Context context, long listID) {
    ListDTO listDTO = new ListDTO();
    if (listID == 0) {
      //フォロー一覧
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      long selectedListID = preferences.getLong(MainActivity.SELECTED_LIST_ID, 0);
      listDTO.name = context.getString(R.string.all_follow);
      if (selectedListID == 0) {
        listDTO.subscribed = true;
      }
    } else {
      SQLiteDatabase readable = getReadableDatabase();
      String query = "select * from " + LIST_INFO + " where _id = " + listID + ";";
      Cursor cursor = readable.rawQuery(query, null);
      cursor.moveToFirst();
      if (cursor.getCount() > 0) {
        listDTO.listID = cursor.getLong(cursor.getColumnIndex("_id"));
        listDTO.name = cursor.getString(cursor.getColumnIndex("name"));
        int subscribed = cursor.getInt(cursor.getColumnIndex("subscribed"));
        if (subscribed == 1) {
          listDTO.subscribed = true;
        }
      }
    }
    return listDTO;
  }

  //リストの購読
  public void subscribeList(Integer listID) {
    ContentValues args = new ContentValues();
    args.put("subscribed", 1);
    String filter = String.valueOf(listID);

    SQLiteDatabase writable = getWritableDatabase();
    try {
      writable.update(LIST_INFO, args, filter, null);
    } catch (SQLiteException e) {
      e.printStackTrace();
    } finally {
      writable.close();
    }
  }

  //リスト一覧の更新。リストの登録・解除を含む
  public void updateLists(Long myID, List<ListDTO> listDTOs) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select _id, name, subscribed from ").append(LIST_INFO).append(" where my_id = ").append(myID).append(";");

    SQLiteDatabase readable = getReadableDatabase();
    Cursor cursor = readable.rawQuery(queryBuilder.toString(), null);
    Boolean eol = cursor.moveToFirst();

    while (eol) {
      //購読中list_idと一致するものがlistDTOs中にある場合、購読中に設定しておく
      int subscribed = cursor.getInt(cursor.getColumnIndex("subscribed"));
      if (subscribed == 1) {
        long listID = cursor.getLong(cursor.getColumnIndex("_id"));
        for (ListDTO listDTO : listDTOs) {
          if (listDTO.listID == listID) {
            listDTO.subscribed = true;
            break;
          }
        }
      }
      eol = cursor.moveToNext();
    }

    cursor.close();

    SQLiteDatabase writable = getWritableDatabase();
    try {
      writable.beginTransaction();
      {
        //一旦LIST_INFOから対象アカのリストを削除
        queryBuilder.setLength(0);
        queryBuilder.append("delete from ").append(LIST_INFO).append(" where my_id = ").append(myID).append(";");
        writable.execSQL(queryBuilder.toString());

        //再度追加
        for (ListDTO listDTO : listDTOs) {
          ContentValues args = new ContentValues();
          args.put("_id", listDTO.listID);
          args.put("name", listDTO.name);
          args.put("my_id", myID);
          if (listDTO.subscribed) {
            args.put("subscribed", 1);
          } else {
            args.put("subscribed", 0);
          }
          writable.insert(LIST_INFO, null, args);
        }
      }
      writable.setTransactionSuccessful();
    } catch (SQLiteException e) {
      e.printStackTrace();
    } finally {
      writable.endTransaction();
      writable.close();
    }

  }

  public AccessToken getAccessToken(long myID) {
    SQLiteDatabase readable = getReadableDatabase();
    String query = "select token, token_secret from " + MULTI_ACCOUNTS + " where _id = " + myID + ";";
    Cursor cursor = readable.rawQuery(query, null);

    if (cursor.getCount() == 0) {
      cursor.close();
      return null;
    } else {
      String[] array = new String[2];
      array[0] = cursor.getString(0);
      array[1] = cursor.getString(1);
      cursor.close();
      return new AccessToken(array[0], array[1]);
    }
  }

  public void storeMyAccount(UserDTO myAccount) {
    SQLiteDatabase writable = getWritableDatabase();
    ContentValues args = new ContentValues();
    args.put("_id", myAccount.user_id);
    args.put("name", myAccount.name);
    args.put("screen_name", myAccount.screen_name);
    args.put("profile_image_url", myAccount.profile_image_url);
    args.put("token", myAccount.token);
    args.put("token_secret", myAccount.token_secret);
    writable.insertWithOnConflict(MULTI_ACCOUNTS, null, args, SQLiteDatabase.CONFLICT_REPLACE);
  }

  public void setListsSubscribe(List<ListDTO> listDTOs) {
    SQLiteDatabase writable = getWritableDatabase();
    for (ListDTO listDTO : listDTOs) {
      String filter = "_id = " + listDTO.listID;
      ContentValues args = new ContentValues();
      int subscribed_i = 0;
      if (listDTO.subscribed) {
        subscribed_i = 1;
      }
      args.put("subscribed", subscribed_i);
      writable.update(LIST_INFO, args, filter, null);
    }
  }

  public int getTotalYosan(long myID, long listID, Context context) {
    List<UserDTO> users = new ArrayList<>();
    SQLiteDatabase readable = getReadableDatabase();

    StringBuilder queryBuilder1 = new StringBuilder();
    StringBuilder queryBuilder2 = new StringBuilder();
    StringBuilder queryBuilder3 = new StringBuilder();

    //query1
    queryBuilder1.append("select * from ").append(USER_INFO).append(" inner join ").append(OPTIONAL_INFO).append(" on ").append(USER_INFO).append("._id = ").append(OPTIONAL_INFO).append("._id");
    //query2
    queryBuilder2.append("select * from ( ").append(queryBuilder1).append(" ) u inner join ").append(RELATION_INFO).append(" on u._id = ").append(RELATION_INFO).append(".user_id");
    //query3
    queryBuilder3.append("select name, yosan from ( ").append(queryBuilder2).append(" ) v where v.my_id = ").append(myID).append(" and v.relation_id = ").append(listID).append(";");

//        Log.d("Query", queryBuilder3.toString());

    boolean isFiltered = false;
    for (int filter_i = 0; filter_i < MyPreferenceFragment.FILTER_COUNT; filter_i++) {
      SharedPreferences preferences = context.getSharedPreferences("filter" + filter_i, Context.MODE_PRIVATE);
      if (preferences.getBoolean(EditAndCheckablePreference.CHECKED, false)) {
        isFiltered = true;
        break;
      }
    }

    Cursor cursor = readable.rawQuery(queryBuilder3.toString(), null);
    int yosan = 0;
    boolean eol = cursor.moveToFirst();
    if (isFiltered) {
      StringBuilder builder = new StringBuilder();
      while (eol) {
        builder.append(cursor.getString(cursor.getColumnIndex("name")));
        if (StringMatcher.getEventName(builder.toString(), false, context) != null) {
          yosan += cursor.getInt(cursor.getColumnIndex("yosan"));
        }
        builder.setLength(0);
        eol = cursor.moveToNext();
      }
    } else {
      while (eol) {
        yosan += cursor.getInt(cursor.getColumnIndex("yosan"));
        eol = cursor.moveToNext();
      }
    }

    cursor.close();
    return yosan;
  }
}
