package com.kc.comiketter2;

import android.support.v4.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MyPreferenceActivity extends AppCompatActivity implements ClearDialogFragment.ICallback {
    public static final Integer REQUEST_CODE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.preference_activity);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.clear_filter){
                    DialogFragment fragment = FilterClearDialogFragment.newInstance();
                    fragment.show(MyPreferenceActivity.this.getSupportFragmentManager(), "clear_filter");
                }
                return false;
            }
        });
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
        if (helper.clearData()){
            Toast.makeText(this, getString(R.string.clear_got_data_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.clear_got_data_miss), Toast.LENGTH_SHORT).show();
        }
    }
}
