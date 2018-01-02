package com.kc.comiketter2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by HIDE on 2017/11/26.
 */

public class UserDTOAdapter extends ArrayAdapter<UserDTO> implements StickyListHeadersAdapter {
    private Context context;
    private LayoutInflater mInflater;

    public UserDTOAdapter(Context context){
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(R.layout.sticky_header_row, null);
            holder = new HeaderViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (UserDTOAdapter.HeaderViewHolder)convertView.getTag();
        }

        if (getHeaderId(position) == 9){
            holder.textView.setText(context.getString(R.string.unknown));
        } else {
            holder.textView.setText(getHeaderId(position) + context.getString(R.string.nichime));
        }

        int day = (int)getHeaderId(position);
        convertView.setBackgroundColor(getHeaderBackColor(day));

        return convertView;
    }

    // 【重要】Headerグループ毎に同じ値を返すようにしましょう
    // getHeaderItemを参考にしましょう
    @Override
    public long getHeaderId(int position) {
        return getHeaderItem(position);
    }

    // getItemにならい、Header版も作る優しさが持てるといいですね
    // getHeaderIdを参考にしましょう
    public int getHeaderItem(int position){
        UserDTO user = getItem(position);
        if (user.manual_day != 0){
            return user.manual_day;
        } else {
            return user.auto_day;
        }

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ItemViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.sticky_item_row_user, null);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder)convertView.getTag();
        }

        //Adapterに渡された各値を格納します。
        //Userオブジェクトの取得
        final UserDTO user = getItem(position);

        //アイコン画像の設定
        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Comiketter2", "アイコンタップ");
                String screenName = user.screen_name;
                String profileUrl = context.getString(R.string.profile_url) + screenName;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl));
                try {
                    view.getContext().startActivity(intent);
                } catch (ActivityNotFoundException ex){
                    ex.printStackTrace();
                    Log.d("Comiketter2", "Twitterクライアントが開けませんでした。");
                    Toast.makeText(getContext(), context.getString(R.string.no_client), Toast.LENGTH_SHORT).show();
                }
            }
        });
        Glide.with(context).load(user.profile_image_url).into(holder.profile_image);

        //名前の設定
        holder.name.setText(user.name);

        //サークルスペースの設定
        holder.circle_space.setText(user.circle_space);

        //サークル名の設定
        if (user.circle_name != null){
            holder.circle_name.setText(user.circle_name);
        }

        //自己紹介の設定
        holder.profile_description.setText(user.profile_description);

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

        //1列の高さの指定。不規則に高さがばらついてしまうため指定している
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, holder.profile_image.getHeight());
//        convertView.setLayoutParams(params);
//        Log.d("Comiketter2", String.valueOf(convertView.getHeight()));


        int day = 0;
        if (user.manual_day == 0){
            day = user.auto_day;
        } else {
            day = user.manual_day;
        }
        convertView.setBackgroundColor(getRowBackColor(day, position));

        return convertView;
    }

    public static class HeaderViewHolder{
        TextView textView;

        public HeaderViewHolder(View view){
            textView = view.findViewById(R.id.header_text);
        }
    }

    public static class ItemViewHolder{
        ImageButton profile_image;
        TextView name;
        TextView profile_description;
        ImageView pickup_view;
        TextView circle_space;
        TextView circle_name;

        // コンストラクタ内でidバインドを行なうとスッキリします
        public ItemViewHolder(View view) {
            profile_image = view.findViewById(R.id.profile_image);
            name = view.findViewById(R.id.name);
            profile_description = view.findViewById(R.id.profile_description);
            pickup_view = view.findViewById(R.id.pickup_view);
            circle_space = view.findViewById(R.id.circle_space);
            circle_name = view.findViewById(R.id.circle_name);
        }
    }

    protected Integer getRowBackColor(Integer day, Integer position){
        Resources res = context.getResources();
        Integer color;
        Integer p = position % 2;
        if (day == 1){
            if (p == 0){
                color = res.getColor(R.color.first_row_back_1);
            } else {
                color = res.getColor(R.color.first_row_back_2);
            }
        } else if (day == 2){
            if (p == 0){
                color = res.getColor(R.color.second_row_back_1);
            } else {
                color = res.getColor(R.color.second_row_back_2);
            }
        } else if (day == 3) {
            if (p == 0){
                color = res.getColor(R.color.third_row_back_1);
            } else {
                color = res.getColor(R.color.third_row_back_2);
            }
        } else {
            if (p == 0){
                color = res.getColor(R.color.unknown_row_back_1);
            } else {
                color = res.getColor(R.color.unknown_row_back_2);
            }
        }

        return color;
    }

    protected Integer getHeaderBackColor(Integer day){
        Resources res = context.getResources();
        Integer color;
        if (day == 1){
            color = res.getColor(R.color.first_header_back);
        } else if (day == 2){
            color = res.getColor(R.color.second_header_back);
        } else if (day == 3) {
            color = res.getColor(R.color.third_header_back);
        } else {
            color = res.getColor(R.color.unknown_header_back);
        }

        return color;
    }
}
