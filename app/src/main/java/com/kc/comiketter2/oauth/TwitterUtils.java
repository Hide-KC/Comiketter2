package com.kc.comiketter2.oauth;

/**
 * Created by HIDE on 2017/11/05.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kc.comiketter2.R;
import com.kc.comiketter2.UserDTO;
import com.kc.comiketter2.data.DatabaseHelper;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Twitterオブジェクト生成、AccessTokenのセットなど。
 */
public class TwitterUtils {
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    public static User myself = null;

    /**
     * Twitterオブジェクトの生成はこちらから。
     * ConsumerKey, ConsumerSecretをセットして返します。
     * @param context
     * @return
     */
    public static Twitter getTwitter(Context context){
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(context.getString(R.string.consumer_key), context.getString(R.string.consumer_secret));

        AccessToken accessToken = loadAccessToken(context);
        if (accessToken != null){
            twitter.setOAuthAccessToken(accessToken);
        } else {
            Log.d("Comiketter", "AccessTokenが保存されていません。");
        }

        return twitter;
    }

    /**
     * AccessTokenをプリファレンスに保存。
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        editor.apply();
    }

    /**
     * AccessTokenを獲得できたAccountをDBに保存。
     * @param context
     * @param accessToken
     */
    public static void storeMyAccount(final Context context, AccessToken accessToken){
        AsyncTask<AccessToken, Void, Void> task = new AsyncTask<AccessToken, Void, Void>() {
            @Override
            protected Void doInBackground(AccessToken... accessTokens) {
                AccessToken token = accessTokens[0];
                Twitter twitter = TwitterUtils.getTwitter(context);
                try {
                    //自身のプロフィールを取得
                    User myAccount = twitter.verifyCredentials();
                    UserDTO myAccountDTO = new UserDTO(myAccount);
                    myAccountDTO.token = token.getToken();
                    myAccountDTO.token_secret = token.getTokenSecret();
                    DatabaseHelper helper = DatabaseHelper.getInstance(context);
                    helper.storeMyAccount(myAccountDTO);
                } catch (TwitterException e){
                    e.printStackTrace();
                }
                return null;
            }
        };

        task.execute(accessToken);
    }

    //選択しているアカウントのAccessTokenを返す
    public static AccessToken loadAccessToken(Context context, long myID){
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String token = preferences.getString()
        return null;
    }

    /**
     * AccessTokenをプリファレンスから読込み。
     * 保存されていない場合はnullを返す。
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String token = preferences.getString(TOKEN, null);
        String token_secret = preferences.getString(TOKEN_SECRET, null);

        if (token != null && token_secret != null){
            return new AccessToken(token, token_secret);
        } else {
            return null;
        }
    }

    /**
     * AccessTokenをプリファレンスから削除。
     * @param context
     */
    public static void deleteAccessToken(Context context) throws NullPointerException{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, null);
        editor.putString(TOKEN_SECRET, null);
        editor.apply();
    }


}


