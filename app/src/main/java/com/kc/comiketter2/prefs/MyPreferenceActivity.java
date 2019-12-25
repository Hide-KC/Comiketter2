package com.kc.comiketter2.prefs;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kc.comiketter2.R;
import com.kc.comiketter2.model.data.database_helper.DatabaseHelper;
import com.kc.comiketter2.ui.MyPreferenceFragment;
import com.kc.comiketter2.ui.dialog.ClearDialogFragment;

public class MyPreferenceActivity extends AppCompatActivity implements ClearDialogFragment.ICallback {
  public static final Integer REQUEST_CODE = 2000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preferences);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.inflateMenu(R.menu.preference_activity);
    toolbar.setNavigationIcon(R.drawable.ic_action_back);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent();
        intent.putExtra("msg", "update");
        setResult(RESULT_OK, intent);
        finish();
      }
    });

    PreferenceFragment fragment = MyPreferenceFragment.newInstance();
    FragmentManager manager = getFragmentManager();
    manager.beginTransaction()
        .add(R.id.container, fragment, "preference_fragment")
        .commit();
  }

  @Override
  public void onPositiveButtonClicked() {
    DatabaseHelper helper = DatabaseHelper.getInstance(this);
    if (helper.clearData()) {
      Toast.makeText(this, getString(R.string.clear_got_data_success), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, getString(R.string.clear_got_data_miss), Toast.LENGTH_SHORT).show();
    }
  }
}
