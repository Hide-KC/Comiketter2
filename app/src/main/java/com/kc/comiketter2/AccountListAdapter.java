package com.kc.comiketter2;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by HIDE on 2018/01/18.
 */

public class AccountListAdapter extends ArrayAdapter<ListDTO> {
    private Context context;
    private LayoutInflater mInflater;

    public AccountListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
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



        return super.getView(position, convertView, parent);
    }

    public static class ItemViewHolder{
        ImageView checkImage;
        TextView listName;

        public ItemViewHolder(View view){
            checkImage = view.findViewById(R.id.image_check);
            listName = view.findViewById(R.id.list_item_name);
        }
    }
}
