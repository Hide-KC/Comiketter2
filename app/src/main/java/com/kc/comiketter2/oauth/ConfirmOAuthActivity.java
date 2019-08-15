package com.kc.comiketter2.oauth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.kc.comiketter2.dialog.ExplainDialogFragment;
import com.kc.comiketter2.R;

/**
 * OAuth認証開始の確認画面。
 */
public class ConfirmOAuthActivity extends AppCompatActivity {
    private TwitterOAuth mOAuth;
    public static final Integer REQUEST_CODE = 3000;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("msg", "finish");
        setResult(RESULT_CANCELED, intent);
        finish();
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
        setResult(RESULT_OK, intent);
        finish();
    }
}
