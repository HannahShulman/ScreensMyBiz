package com.screensmybiz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hannashulmah on 09/11/2016.
 */
public class LastMessageAdapter extends BaseAdapter {
    ArrayList<LastMessage> lastMessagesList;
    Context ctx;

    public LastMessageAdapter(Context ctx, ArrayList<LastMessage> lastMessagesList) {
        this.ctx = ctx;
        this.lastMessagesList = lastMessagesList;
    }

    @Override
    public int getCount() {
        return lastMessagesList.size();
    }

    @Override
    public LastMessage getItem(int position) {
        return lastMessagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    ViewHolder holder=null;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = LayoutInflater.from(ctx);
        if(convertView==null){
            holder = new ViewHolder();
            convertView =  inflater.inflate(R.layout.single_last_msg, parent, false);
            holder.contact = (TextView) convertView.findViewById(R.id.contact_name);
            holder.done_all = (ImageView) convertView.findViewById(R.id.seen_icon);
            holder.profile = (de.hdodenhof.circleimageview.CircleImageView) convertView.findViewById(R.id.profile);
            holder.message = (TextView) convertView.findViewById(R.id.txt_msg);
            holder.time = (TextView) convertView.findViewById(R.id.msg_sent);
            holder.category = (TextView) convertView.findViewById(R.id.category);
            convertView.setTag(holder);

        }else{

            holder = (ViewHolder) convertView.getTag();
        }

        LastMessage message = lastMessagesList.get(position);
        if(message.isMy_msg()){
            holder.done_all.setVisibility(View.VISIBLE);
        }else{
            holder.done_all.setVisibility(View.GONE);
        }
        holder.contact.setText(message.getContact());
        holder.time.setText(message.getTime());
        holder.message.setText(message.getMessage());
        holder.category.setText(message.getCategory());
        holder.profile.setBackgroundResource(message.getProfile());
//        holder.profile.setBackgroundResource(message.getProfile());
        return convertView;
    }

    class  ViewHolder{
        de.hdodenhof.circleimageview.CircleImageView profile;
        ImageView done_all;
        TextView category, time, contact, message;

    }
}
