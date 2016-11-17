//package com.screensmybiz;
//
//import android.content.Context;
//import android.support.v4.view.ViewPager;
//import android.support.v7.widget.RecyclerView;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import java.text.DateFormat;
//import java.util.ArrayList;
//
///**
// * Created by hannashulmah on 15/11/2016.
// */
//public class MessageAdapter extends BaseAdapter {
//
//    Context ctx;
//    ArrayList<Message> messageList;
//
//    public MessageAdapter(Context ctx, ArrayList<Message> messageList) {
//        this.ctx = ctx;
//        this.messageList = messageList;
//    }
//
//    @Override
//    public int getCount() {
//        return messageList.size();
//    }
//
//    @Override
//    public Message getItem(int position) {
//        return messageList.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return messageList.get(position).getReciever().equals("user")?0:1;
//    }
//
//
//    MessageViewHolder holder;
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        holder = new MessageViewHolder();
//        if(convertView==null){
//            if(getItemViewType(position)==1) {
//                convertView = LayoutInflater.from(ctx).inflate(R.layout.single_message_recieved, parent, false);
//            }
//            if (getItemViewType(position)==0){
//                convertView = LayoutInflater.from(ctx).inflate(R.layout.single_message_sent, parent, false);
//            }
//
//            holder.msg_container = (LinearLayout) convertView.findViewById(R.id.single_msg_main_layout);
//            holder.check = (ImageView) convertView.findViewById(R.id.check);
//            holder.message_txt = (TextView) convertView.findViewById(R.id.message_txt);
//            holder.date = (TextView) convertView.findViewById(R.id.date);
//            convertView.setTag(holder);
////            }
//        }
//        else
//        {
//            holder = (MessageViewHolder) convertView.getTag();
//        }
//
//        if(getItemViewType(position)==0){
//            holder.msg_container.setGravity(Gravity.RIGHT);
//        }else{
//            holder.msg_container.setGravity(Gravity.LEFT);
//        }
//        holder.message_txt.setText(messageList.get(position).getMessage());
//        holder.date.setText(position+"");
//
//        return convertView;
//    }
//
//
//    class MessageViewHolder{
//
//        TextView message_txt, date;
//        ImageView check;
//        LinearLayout msg_container;
//
//    }
//}
