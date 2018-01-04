package com.kc.comiketter2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by HIDE on 2017/11/12.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public final String USER_INFO = "user_info";
    public final String OPTIONAL_INFO = "optional_info";
    public final String HOLE_NAMES = "hole_names";

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
            + "hole_id integer, "
            + "target integer, "
            + "busuu integer, "
            + "yosan integer, "
            + "memo text, "
            + "pickup integer, "
            + "hasgot integer )";

    private final String HOLE_NAME_QUERY = "create table " + HOLE_NAMES + " ("
            + "hole_id integer primary key not null, "
            + "name text not null )";

    private static DatabaseHelper helper;
    private static final Integer DB_VERSION = 2; //DBスキーマを変更した場合、これをインクリメントする

    private DatabaseHelper(Context context){
        super(context, "usersDB", null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context){
        if (helper == null){
            helper = new DatabaseHelper(context);
        }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(USER_QUERY);
        sqLiteDatabase.execSQL(OPTIONAL_QUERY);
        sqLiteDatabase.execSQL(HOLE_NAME_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // DBバージョンアップ時のデータ移行を実装
        if (oldVersion == 1 && newVersion == 2){
            //OPTIONAL_INFOテーブルへのhole_id列追加、HOLE_NAMESテーブルの追加
            try {
                database.execSQL(HOLE_NAME_QUERY);
                database.execSQL(
                        "alter table " + OPTIONAL_INFO + " add hole_id text"
                );
            } catch (SQLiteException ex){
                ex.printStackTrace();
            }

            for (Integer key:StringMatcher.holeHashMap.keySet()){
                ContentValues cv = new ContentValues();
                cv.put("hole_id", key);
                cv.put("name", StringMatcher.holeHashMap.get(key));
                database.insert(HOLE_NAMES, null, cv);
            }
        }
    }

    public void updateUserInfo(List<UserDTO> users){
        //登録済みID一覧を取得
        SQLiteDatabase readable = getReadableDatabase();
        String q = "select _id from " + OPTIONAL_INFO + ";";
        Cursor cursor = readable.rawQuery(q, null);

        List<Long> registeredIDs = new ArrayList<>();

        boolean eol = cursor.moveToFirst();
        while (eol){
            registeredIDs.add(cursor.getLong(cursor.getColumnIndex("_id")));
            eol = cursor.moveToNext();
        }

        cursor.close();

        //値のUpdate
        SQLiteDatabase writable = getWritableDatabase();
        writable.beginTransaction();
        {
            try{
                for (Integer user_i = 0; user_i < registeredIDs.size(); user_i++){
                    //更新前準備。スペース、日付をリセットする。サークル名はリセットしなくていいかな。
                    //当落発表時くらいしかキャッチするタイミングがないため。
                    String filter = "_id = " + registeredIDs.get(user_i);
                    ContentValues cv = new ContentValues();
                    cv.put("auto_day", 0);
                    cv.put("circle_name", "");
                    cv.put("circle_space", "");
                    writable.update(OPTIONAL_INFO, cv, filter, null);
                }

                for (Integer user_i = 0; user_i < users.size(); user_i++){
                    UserDTO user = users.get(user_i);

                    //USER_INFO query
                    String replaceQuery = "Insert or Replace into " + USER_INFO + " ("
                            + "_id, "
                            + "name, "
                            + "screen_name, "
                            + "profile_image_url, "
                            + "profile_description) "
                            + "Values (?, ?, ?, ?, ?)";

                    SQLiteStatement replaceUserinfo = writable.compileStatement(replaceQuery);
                    replaceUserinfo.bindLong(1, user.user_id);
                    replaceUserinfo.bindString(2, user.name);
                    replaceUserinfo.bindString(3, user.screen_name);
                    replaceUserinfo.bindString(4, user.profile_image_url);
                    replaceUserinfo.bindString(5, user.profile_description);

                    replaceUserinfo.execute();

                    //OPTIONAL_INFO query
                    //user_i == 0　は自身のアカウントのため、１からスタート
                    if (user_i > 0){
                        Integer autoDay = StringMatcher.getParticipateDay(user.name);
                        String space = StringMatcher.getSpace(user.name);
                        Integer holeID = 0;

                        ContentValues instantValues = new ContentValues();
                        instantValues.put("_id", user.user_id);
                        if (space != null){
                            holeID = StringMatcher.getHoleID(space);
                            instantValues.put("auto_day", autoDay);
                            instantValues.put("circle_space", space);
                        } else {
                            instantValues.put("auto_day", 0);
                            instantValues.put("circle_space", "");
                        }

                        if (user.circle_name != null){
                            instantValues.put("circle_name", user.circle_name);
                        } else {
                            instantValues.put("circle_name", "");
                        }

                        int id = (int) writable.insertWithOnConflict(OPTIONAL_INFO, null, instantValues, SQLiteDatabase.CONFLICT_IGNORE);
                        if (id == -1){
                            String filter = "_id = " + user.user_id;
                            ContentValues args = new ContentValues();
                            args.put("auto_day", autoDay);
                            //当落発表ツイが流れてしまうとnullになってしまうためnullチェック
                            if (user.circle_name != null){
                                args.put("circle_name", user.circle_name);
                            }
                            args.put("hole_id", holeID);
                            args.put("circle_space", space);
                            writable.update(OPTIONAL_INFO, args, filter, null);
                        }
                    }
                }

                writable.setTransactionSuccessful();
            } catch (SQLiteException ex){
                ex.printStackTrace();
            } finally {
                writable.endTransaction();
                writable.close();
            }
        }
    }

    public List<UserDTO> getUserList(){
        List<UserDTO> users = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        String query1 = "select * from " + USER_INFO + " inner join " + OPTIONAL_INFO + " on " + USER_INFO + "._id = " + OPTIONAL_INFO + "._id";
        String query2 = "select * from ( " + query1 + " ) u order by u.auto_day ASC, u.manual_day ASC, u.circle_space ASC;";

        Cursor cursor = database.rawQuery(query2, null);

        boolean eol = cursor.moveToFirst();
        while (eol){
            UserDTO user = new UserDTO();
            user.user_id = cursor.getLong(cursor.getColumnIndex("_id"));
            user.name = cursor.getString(cursor.getColumnIndex("name")).replaceAll("':", ":");
            user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
            user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url")).replaceAll("':", ":");
            user.profile_description = cursor.getString(cursor.getColumnIndex("profile_description")).replaceAll("':", ":");
            user.auto_day = cursor.getInt(cursor.getColumnIndex("auto_day"));
            user.manual_day = cursor.getInt(cursor.getColumnIndex("manual_day"));
            user.circle_space = cursor.getString(cursor.getColumnIndex("circle_space"));
            user.circle_name = cursor.getString(cursor.getColumnIndex("circle_name"));
            user.hole_id = cursor.getInt(cursor.getColumnIndex("hole_id"));
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

    public UserDTO getUser(long userID){
        UserDTO user = new UserDTO();
        SQLiteDatabase database = getReadableDatabase();

        String query1 = "select * from " + USER_INFO + " inner join " + OPTIONAL_INFO + " on " + USER_INFO + "._id = " + OPTIONAL_INFO + "._id";
        String query2 = "select * from ( " + query1 + " ) u where u._id = " + userID + ";";

        Cursor cursor = database.rawQuery(query2, null);
        cursor.moveToFirst();

        user.user_id = cursor.getLong(cursor.getColumnIndex("_id"));
        user.name = cursor.getString(cursor.getColumnIndex("name")).replaceAll("':", ":");
        user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
        user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url")).replaceAll("':", ":");
        user.profile_description = cursor.getString(cursor.getColumnIndex("profile_description")).replaceAll("':", ":");
        user.auto_day = cursor.getInt(cursor.getColumnIndex("auto_day"));
        user.manual_day = cursor.getInt(cursor.getColumnIndex("manual_day"));
        user.circle_space = cursor.getString(cursor.getColumnIndex("circle_space"));
        user.hole_id = cursor.getInt(cursor.getColumnIndex("hole_id"));
        user.target = cursor.getInt(cursor.getColumnIndex("target"));
        user.busuu = cursor.getInt(cursor.getColumnIndex("busuu"));
        user.yosan = cursor.getInt(cursor.getColumnIndex("yosan"));
        user.memo = cursor.getString(cursor.getColumnIndex("memo"));
        user.pickup = cursor.getInt(cursor.getColumnIndex("pickup"));
        user.hasgot = cursor.getInt(cursor.getColumnIndex("hasgot"));

        cursor.close();

        return user;
    }

    public void setValue(String tableName, Long userID, String columnName, Integer value){
        setValue(tableName, userID, columnName, String.valueOf(value));
    }

    public void setValue(String tableName, Long userID, String columnName, String value){
        SQLiteDatabase database = getWritableDatabase();

        //OPTIONAL_INFO query
        String filter = "_id = " + userID;
        ContentValues args = new ContentValues();
        args.put(columnName, value);
        database.update(tableName, args, filter, null);
        database.close();

        Log.d("UserID", "_id = " + userID + " , column = " + columnName);
    }

    public void clearOptionalInfo(){
        //userID一覧とサイズの取得
        SQLiteDatabase readable = getReadableDatabase();
        String query = "select _id from " + OPTIONAL_INFO + ";";
        Cursor cursor = readable.rawQuery(query, null);

        List<Long> userIDs = new ArrayList<>();

        boolean eol = cursor.moveToFirst();
        while (eol){
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
            try{
                for (Integer user_i = 0; user_i < user_count; user_i++){
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
            } catch (SQLException ex){
                ex.printStackTrace();
            } finally {
                writable.endTransaction();
                writable.close();
            }
        }

    }

}
