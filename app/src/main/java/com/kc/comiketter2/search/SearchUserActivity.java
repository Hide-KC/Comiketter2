package com.kc.comiketter2.search;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.kc.comiketter2.IUpdater;
import com.kc.comiketter2.dialog.OptionalInfoDialogFragment;
import com.kc.comiketter2.R;
import com.kc.comiketter2.UserDTO;
import com.kc.comiketter2.data.DatabaseHelper;

import java.util.List;

public class SearchUserActivity extends AppCompatActivity implements IUpdater {

    public static final Integer REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        Toolbar toolbar = findViewById(R.id.toolbar_search_activity);

        toolbar.setTitle(getString(R.string.search_activity));
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

        toolbar.inflateMenu(R.menu.search_activity);

        final ListView listView = findViewById(R.id.list_result);
        //リストアイテムにクリックイベントを付与
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i) instanceof UserDTO){
                    UserDTO user = (UserDTO)adapterView.getItemAtPosition(i);
                    DialogFragment dialog = OptionalInfoDialogFragment.newInstance(user.user_id);
                    dialog.show(SearchUserActivity.this.getSupportFragmentManager(), "optional_info");
                    Log.d("Comiketter", "" + user.user_id);
                }
            }
        });

        //リストにアダプタをセット
        final ArrayAdapter<UserDTO> adapter;
        if (listView.getAdapter() != null && listView.getAdapter() instanceof ArrayAdapter){
            adapter = (ArrayAdapter<UserDTO>)listView.getAdapter();
            adapter.clear();
        } else {
            adapter = new SearchResultAdapter(SearchUserActivity.this);
            listView.setAdapter(adapter);
        }

        EditText editText = findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String word = editable.toString();

                //DatabaseHelper#searchを呼ぶ
                DatabaseHelper helper = DatabaseHelper.getInstance(SearchUserActivity.this);
                List<UserDTO> users = helper.search(word);

                Log.d("Search", "======================================");
                Log.d("Search", "word :" + word);
                for(UserDTO user : users){
                    Log.d("Search", user.name + " @" + user.screen_name);
                }

                adapter.clear();
                for(Integer user_i = 0; user_i < users.size(); user_i++){
                    adapter.add(users.get(user_i));
                }
            }
        });

        if (savedInstanceState != null){
            editText.setText(savedInstanceState.getString("searchText"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText editText = findViewById(R.id.search_text);
        outState.putString("searchText", editText.getText().toString());
    }


    @Override
    public void update() {
        EditText editText = findViewById(R.id.search_text);
        String word = editText.getText().toString();

        //DatabaseHelper#searchを呼ぶ
        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        List<UserDTO> users = helper.search(word);

        Log.d("Search", "======================================");
        Log.d("Search", "word :" + word);
        for(UserDTO user : users){
            Log.d("Search", user.name + " @" + user.screen_name);
        }

        ListView results = findViewById(R.id.list_result);
        ArrayAdapter<UserDTO> adapter = (ArrayAdapter<UserDTO>) results.getAdapter();
        adapter.clear();
        for(Integer user_i = 0; user_i < users.size(); user_i++){
            adapter.add(users.get(user_i));
        }
    }
}
