package com.kc.comiketter2.oauth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kc.comiketter2.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by HIDE on 2017/11/05.
 */

public class TwitterOAuth {
    private Context context = null;
    private Twitter mTwitter;
    private RequestToken requestToken = null;

    public TwitterOAuth(Context context){
        this.context = context;
        mTwitter = TwitterUtils.getTwitter(context);
    }

    public void startAuthorize(){
        final String callbackUrl = context.getString(R.string.callback_url);

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    requestToken = mTwitter.getOAuthRequestToken(strings[0]);
                    return requestToken.getAuthenticationURL();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (TwitterException ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    context.startActivity(intent);
                } else {
                    Log.d("Comiketter", "Authorize失敗");
                }
            }
        };
        task.execute(callbackUrl);
    }

    public void OAuthApproval(final Context context, Intent intent){
        if (intent == null
                || intent.getData() == null
                || !intent.getData().toString().startsWith(context.getString(R.string.callback_url))){

            return;
        }

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... strings) {
                try {
                    return mTwitter.getOAuthAccessToken(requestToken, strings[0]);
                } catch (TwitterException ex){
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null){
                    //認証成功。AccessTokenを保存して終了。
                    TwitterUtils.storeAccessToken(context, accessToken);
                    Log.d("Comiketter", "認証成功！");
                    Toast.makeText(context, context.getString(R.string.accesstoken_success), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Comiketter", "認証失敗。");
                    Toast.makeText(context, context.getString(R.string.accesstoken_error), Toast.LENGTH_SHORT).show();
                }
            }
        };

        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        task.execute(verifier);
    }
}
