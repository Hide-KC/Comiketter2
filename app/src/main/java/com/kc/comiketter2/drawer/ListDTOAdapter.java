package com.kc.comiketter2.drawer;

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

import com.kc.comiketter2.R;

import java.util.ArrayList;

/**
 * Created by HIDE on 2018/01/20.
 */

public class ListDTOAdapter extends ArrayAdapter<ListDTO> {
    private Context context;
    private LayoutInflater mInflater;

    public ListDTOAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ItemViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder)convertView.getTag();
        }

        //Adapterに渡された各値を格納します。
        //Userオブジェクトの取得
        final ListDTO listDTO = getItem(position);
        holder.name.setText(listDTO.name);

        if (listDTO.subscribed){
            holder.check.setImageResource(R.drawable.check);
        } else {
            holder.check.setImageResource(R.drawable.uncheck);
        }

        return convertView;
    }

    public ArrayList<ListDTO> toArrayList(){
        ArrayList<ListDTO> listDTOs = new ArrayList<>();
        for (int i = 0; i < this.getCount(); i++){
            listDTOs.add(this.getItem(i));
        }
        return listDTOs;
    }

    public static class ItemViewHolder{
        TextView name;
        ImageView check;
        public ItemViewHolder(View view){
            name = view.findViewById(R.id.list_item_name);
            check = view.findViewById(R.id.image_check);
        }
    }
}
