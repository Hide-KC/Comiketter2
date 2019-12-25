package com.kc.comiketter2.howto;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kc.comiketter2.R;

public class HowToActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_how_to);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    TextView tv = findViewById(R.id.how_to_text);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      tv.setText(Html.fromHtml(getString(R.string.how_to_text), Html.FROM_HTML_MODE_LEGACY));
    } else {
      tv.setText(Html.fromHtml(getString(R.string.how_to_text)));
    }
  }
}
