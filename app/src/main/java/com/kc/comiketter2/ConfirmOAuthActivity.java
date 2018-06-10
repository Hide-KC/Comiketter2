package com.kc.comiketter2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * OAuth認証開始の確認画面。
 */
public class ConfirmOAuthActivity extends AppCompatActivity {
    private TwitterOAuth mOAuth;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ConfirmOAuthActivity.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setContentView(R.layout.activity_confirm_oauth);
        } else {
            setContentView(R.layout.activity_confirm_oauth_low);
        }

        mOAuth = new TwitterOAuth(this);

        Button btnYes = (Button)findViewById(R.id.btnYes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOAuth.startAuthorize();
            }
        });

        Button btnExplain = findViewById(R.id.btn_explain);
        btnExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = ExplainDialogFragment.newInstance();
                fragment.show(ConfirmOAuthActivity.this.getSupportFragmentManager(), ConfirmOAuthActivity.this.getClass().getSimpleName());
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
