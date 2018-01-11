package com.kc.comiketter2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

/**
 * Created by HIDE on 2018/01/10.
 */

public class SearchResultAdapter extends ArrayAdapter<UserDTO> {
    private Context context;
    private LayoutInflater mInflater;

    public SearchResultAdapter(Context context){
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ItemViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_search_result, null);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder)convertView.getTag();
        }

        //Adapterに渡された各値を格納します。
        //Userオブジェクトの取得
        final UserDTO user = getItem(position);

        //アイコン画像の設定
        Glide.with(context).load(user.profile_image_url).into(holder.profile_image);

        //名前の設定
        holder.name.setText(user.name);

        //スクリーンネームの設定
        holder.screenName.setText("@" + user.screen_name);

        //ホールの設定
        String holeName = StringMatcher.getHoleName(user.hole_id);
        if (holeName != null){
            holder.hole_name.setText(holeName);
        } else {
            holder.hole_name.setText("");
        }

        //サークルスペースの設定
        holder.circle_space.setText(user.circle_space);

        //サークル名の設定
        if (user.circle_name != null){
            holder.circle_name.setText(user.circle_name);
        }

        //ふぁぼの設定
        if (user.pickup == 0){
            holder.pickup_view.setImageResource(R.drawable.pickup_off);
        } else {
            holder.pickup_view.setImageResource(R.drawable.pickup_on);
        }

        holder.pickup_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper helper = DatabaseHelper.getInstance(context);
                if (user.pickup == 0){
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "pickup", 1);
                    holder.pickup_view.setImageResource(R.drawable.pickup_on);
                    user.pickup = 1;
                } else {
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "pickup", 0);
                    holder.pickup_view.setImageResource(R.drawable.pickup_off);
                    user.pickup = 0;
                }
            }
        });

        return convertView;
    }

    public static class ItemViewHolder{
        ImageButton profile_image;
        TextView name;
        TextView screenName;
        ImageView pickup_view;
        TextView circle_space;
        TextView circle_name;
        TextView hole_name;

        // コンストラクタ内でidバインドを行なうとスッキリします
        public ItemViewHolder(View view) {
            profile_image = view.findViewById(R.id.profile_image);
            name = view.findViewById(R.id.name);
            screenName = view.findViewById(R.id.screen_name);
            pickup_view = view.findViewById(R.id.pickup_view);
            circle_space = view.findViewById(R.id.circle_space);
            circle_name = view.findViewById(R.id.circle_name);
            hole_name = view.findViewById(R.id.hole_name);
        }
    }
}
