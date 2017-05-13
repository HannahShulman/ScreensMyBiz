package com.cliqdbase.app.venue_chat;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cliqdbase.app.R;
import com.cliqdbase.app.general.Common;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Yuval on 28/07/2015.
 *
 * @author Yuval Siev
 */
public class VenueChatArrayAdapter extends ArrayAdapter<Message> {
    private Context context;
    private int layoutResourceId;


    public VenueChatArrayAdapter(Context context, int layoutResourceId, ArrayList<Message> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    @SuppressWarnings("deprecation")
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);


        TextView userId_tv = (TextView) convertView.findViewById(R.id.venue_chat_one_message_user_id);
        TextView userName_tv = (TextView) convertView.findViewById(R.id.venue_chat_one_message_user_name);
        TextView messageText_tv = (TextView) convertView.findViewById(R.id.venue_chat_one_message_text);
        TextView messageDate_tv = (TextView) convertView.findViewById(R.id.venue_chat_one_message_date);


        RelativeLayout messageContainer_rl = (RelativeLayout) convertView.findViewById(R.id.venue_chat_message_container);

        long messageUserId = message.getUserId();
        String messageText = message.getText();
        String messageUserName = message.getUserName() + ":";
        Date date = message.getDate();

        if (messageUserId == Common.getLoggedInUserId(context)) {
            messageContainer_rl.setGravity(Gravity.END);
            if (Build.VERSION.SDK_INT >= 23)
                userName_tv.setTextColor(context.getResources().getColor(R.color.message_sent, null));
            else
                userName_tv.setTextColor(context.getResources().getColor(R.color.message_sent));
        }
        else {
            messageContainer_rl.setGravity(Gravity.START);
            if (Build.VERSION.SDK_INT >= 23)
                userName_tv.setTextColor(context.getResources().getColor(R.color.message_received, null));
            else
                userName_tv.setTextColor(context.getResources().getColor(R.color.message_received));
        }


        String dateString = Common.getTimeString(date.getTime());

        userId_tv.setText(String.valueOf(messageUserId));
        userName_tv.setText(messageUserName);
        messageText_tv.setText(messageText);
        messageDate_tv.setText(dateString);


        return convertView;
    }
}
