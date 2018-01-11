package com.kc.comiketter2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class SearchUserActivity extends AppCompatActivity {

    public static final Integer REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        Toolbar toolbar = findViewById(R.id.toolbar_search_activity);

        toolbar.setTitle(getString(R.string.search_activity));
        toolbar.setNavigationIcon(R.drawable.arrow_left);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("msg", "update");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        toolbar.inflateMenu(R.menu.menu_search_activity);

        //リストにアダプタをセット
        final ListView list = findViewById(R.id.list_result);
        final ArrayAdapter<UserDTO> adapter;
        if (list.getAdapter() != null && list.getAdapter() instanceof ArrayAdapter){
            adapter = (ArrayAdapter<UserDTO>)list.getAdapter();
            adapter.clear();
        } else {
            adapter = new SearchResultAdapter(SearchUserActivity.this);
            list.setAdapter(adapter);
        }

        EditText editText = findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String word = charSequence.toString();
                if (word.contains("?") || word.contains("？")){
                    Toast.makeText(SearchUserActivity.this, "？は使用できません。", Toast.LENGTH_SHORT).show();
                    word = word.replace("?", "");
                    word = word.replace("？", "");
                }

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

            @Override
            public void afterTextChanged(Editable editable) {

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


}
