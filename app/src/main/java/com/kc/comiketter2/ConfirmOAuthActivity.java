package com.kc.comiketter2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * OAuth認証開始の確認画面。
 */
public class ConfirmOAuthActivity extends AppCompatActivity {
    private TwitterOAuth mOAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_oauth);

        mOAuth = new TwitterOAuth(this);

        Button btnYes = (Button)findViewById(R.id.btnYes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOAuth.startAuthorize();
            }
        });


        Button btnNo = (Button)findViewById(R.id.btnNo);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmOAuthActivity.this.finish();
            }
        });
    }

    //Webページでアプリを承認するとここに戻ってくる。
    @Override
    protected void onNewIntent(Intent intent) {
        mOAuth.OAuthApproval(this, intent);
        finish();
    }
}
