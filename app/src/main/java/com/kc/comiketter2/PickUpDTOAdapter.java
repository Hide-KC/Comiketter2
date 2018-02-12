package com.kc.comiketter2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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
 * Created by HIDE on 2017/11/05.
 */

public class PickUpDTOAdapter extends ArrayAdapter<UserDTO> implements StickyListHeadersAdapter {
    private Context context;
    private LayoutInflater mInflater;

    public PickUpDTOAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Headerのレイアウトを定義します。
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(R.layout.sticky_header_row, null);
            holder = new HeaderViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder)convertView.getTag();
        }

        if (getHeaderId(position) == 9) {
            holder.textView.setText(context.getString(R.string.unknown));
        } else if (getHeaderId(position) == 99){
            holder.textView.setText(context.getString(R.string.other));
        } else {
            holder.textView.setText(getHeaderId(position) + context.getString(R.string.nichime));
        }

        int day = (int)getHeaderId(position);
        convertView.setBackgroundColor(getHeaderBackColor(day));

        return convertView;
    }

    // 【重要】Headerグループ毎に同じ値を返すようにしましょう
    // ここではItemポジションを5で割った商が同じになるものをグループにします（5個/グループ）
    // getHeaderItemを参考にしましょう
    @Override
    public long getHeaderId(int position) {
        return getHeaderItem(position);
    }

    // getItemにならい、Header版も作る優しさが持てるといいですね
    // 各アイテムのHeaderはポジションを5で割った商です
    // getHeaderIdを参考にしましょう
    public int getHeaderItem(int position){
        UserDTO user;
        if (position > getCount()){
            user = getItem(getCount() - 1);
        } else {
            user = getItem(position);
        }
        return user.auto_day;

//        if (user.manual_day != 0){
//            return user.manual_day;
//        } else {
//            return user.auto_day;
//        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ItemViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(R.layout.sticky_item_row_pickup, null);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
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


        //部数の設定
        if (user.busuu > 0){
            holder.busuu.setText(user.busuu + "部");
        } else {
            holder.busuu.setText("");
        }

        //予算の設定
        if (user.yosan > 0){
            holder.yosan.setText(user.yosan + "円");
        } else {
            holder.yosan.setText("");
        }

        //メモの設定
        holder.memo.setText(user.memo);

        //ターゲットの設定
        if (user.target == 0){
            holder.target.setText("新刊");
        } else if (user.target == 1){
            holder.target.setText("新既刊");
        } else if (user.target == 2){
            holder.target.setText("既刊");
        } else if (user.target == 3){
            holder.target.setText("その他");
        }

        //ふぁぼの設定
        if (user.pickup == 1){
            holder.pickup_view.setImageResource(R.drawable.pickup_on);
        } else {
            holder.pickup_view.setImageResource(R.drawable.pickup_off);
        }
        holder.pickup_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper helper = DatabaseHelper.getInstance(context);
                if (user.pickup == 1){
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "pickup", 0);
                    holder.pickup_view.setImageResource(R.drawable.pickup_off);
                    user.pickup = 0;
                } else {
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "pickup", 1);
                    holder.pickup_view.setImageResource(R.drawable.pickup_on);
                    user.pickup = 1;
                }
            }
        });


        //チェックマークの設定
        if (user.hasgot == 1){
            holder.check_view.setImageResource(R.drawable.sumi3_on_transparent);
        } else {
            holder.check_view.setImageResource(R.drawable.sumi3_off_transparent);
        }
        holder.check_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper helper = DatabaseHelper.getInstance(context);
                if (user.hasgot == 1){
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "hasgot", 0);
                    holder.check_view.setImageResource(R.drawable.sumi3_off_transparent);
                    user.hasgot = 0;
                } else {
                    helper.setValue(helper.OPTIONAL_INFO, user.user_id, "hasgot", 1);
                    holder.check_view.setImageResource(R.drawable.sumi3_on_transparent);
                    user.hasgot = 1;
                }
            }
        });

        //1列の高さの指定。不規則に高さがばらついてしまうため指定している
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, holder.profile_image.getHeight());
//        convertView.setLayoutParams(params);

        int day = 0;
        if (user.manual_day == 0){
            day = user.auto_day;
        } else {
            day = user.manual_day;
        }
        convertView.setBackgroundColor(getRowBackColor(day, position));
        //pickup_viewに背景色を設定
        holder.pickup_view.setBackgroundColor(getRowBackColor(day, position));
        //済に背景色を設定
        holder.check_view.setBackgroundColor(getRowBackColor(day, position));

        return convertView;
    }

    @Override
    public void clear(){ super.clear(); }

    public static class HeaderViewHolder{
        TextView textView;

        public HeaderViewHolder(View view){
            textView = view.findViewById(R.id.header_text);
        }
    }

    public static class ItemViewHolder{
        ImageButton profile_image;
        TextView name;
        TextView circle_space;
        TextView circle_name;
        TextView busuu;
        TextView yosan;
        TextView memo;
        TextView target;
        TextView hole_name;

        ImageView pickup_view;
        ImageView check_view;

        // コンストラクタ内でidバインドを行なうとスッキリします
        public ItemViewHolder(View view) {
            profile_image = view.findViewById(R.id.profile_image);
            name = view.findViewById(R.id.name);
            circle_space = view.findViewById(R.id.circle_space);
            circle_name = view.findViewById(R.id.circle_name);
            target = view.findViewById(R.id.target);
            busuu = view.findViewById(R.id.busuu);
            yosan = view.findViewById(R.id.yosan);
            memo = view.findViewById(R.id.memo);
            hole_name = view.findViewById(R.id.hole_name);

            pickup_view = view.findViewById(R.id.pickup_view);
            check_view = view.findViewById(R.id.hasgot_view);
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
        } else if (day == 9) {
            if (p == 0){
                color = res.getColor(R.color.unknown_row_back_1);
            } else {
                color = res.getColor(R.color.unknown_row_back_2);
            }
        } else {
            if (p == 0){
                color = res.getColor(R.color.other_row_back_1);
            } else {
                color = res.getColor(R.color.other_row_back_2);
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
        } else if (day == 9){
            color = res.getColor(R.color.unknown_header_back);
        } else {
            color = res.getColor(R.color.other_header_back);
        }

        return color;
    }
}
