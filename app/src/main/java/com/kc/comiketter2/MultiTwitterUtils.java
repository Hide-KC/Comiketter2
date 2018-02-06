package com.kc.comiketter2;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by HIDE on 2018/01/31.
 */

public class MultiTwitterUtils {
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";

    private MultiTwitterUtils(){

    }

    static public Twitter getTwitter(Context context, long myID) throws Resources.NotFoundException{
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(context.getString(R.string.consumer_key), context.getString(R.string.consumer_secret));

        AccessToken token = loadAccessToken(context, myID);
        if (token != null){
            twitter.setOAuthAccessToken(token);
        } else {
            throw new Resources.NotFoundException("AccessTokenが保存されていません");
        }

        return twitter;
    }

    static public AccessToken loadAccessToken(Context context, long myID){
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        String[] array = helper.getAccessTokenArray(myID);
        if (array != null){
            return new AccessToken(array[0], array[1]);
        } else {
            return null;
        }
    }

    static public void storeAccessToken(Context context, long myID, AccessToken token){
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        SQLiteDatabase writable = helper.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put("_id", myID);
        args.put("token", token.getToken());
        args.put("token_secret", token.getTokenSecret());

        int id = (int) writable.insertWithOnConflict(helper.MULTI_ACCOUNTS, null, args, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1){
            String filter = "_id = " + myID;
            writable.update(helper.MULTI_ACCOUNTS, args, filter, null);
        }
    }

    static public void deleteAccessToken(Context context, long myID){

    }
}
