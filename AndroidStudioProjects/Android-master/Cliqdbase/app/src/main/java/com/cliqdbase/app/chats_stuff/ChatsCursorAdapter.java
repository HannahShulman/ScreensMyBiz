package com.cliqdbase.app.chats_stuff;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.cliqdbase.app.R;

/**
 * Created by Yuval on 10/05/2015.
 *
 * @author Yuval Siev
 */
public class ChatsCursorAdapter extends CursorAdapter {


    private ArrayList<Long> selectedIds;


    public ChatsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.selectedIds = new ArrayList<>();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.chat_main_item, parent, false);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name_tv = (TextView) view.findViewById(R.id.chat_item_user_name);
        TextView lastMessage_tv = (TextView) view.findViewById(R.id.chat_item_last_message);
        TextView lastMessageTime_tv = (TextView) view.findViewById(R.id.chat_item_last_message_time);
        TextView chatUserId_tv = (TextView) view.findViewById(R.id.chat_item_user_id);

        String name = cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_USERS_FIRST_NAME)) + " " + cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_USERS_LAST_NAME));
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TEXT));
        long lastMessageTime = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TIME));
        long chatUserId = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_USER_ID));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm", Locale.getDefault());
        String lastMessageTimeStr = simpleDateFormat.format(new Date(lastMessageTime));

        name_tv.setText(name);
        lastMessage_tv.setText(lastMessage);
        lastMessageTime_tv.setText(lastMessageTimeStr);
        chatUserId_tv.setText(String.valueOf(chatUserId));

        if (Build.VERSION.SDK_INT >= 23) {
            if (selectedIds.contains(chatUserId))
                view.setBackgroundColor(context.getResources().getColor(R.color.list_selected_background, null));
            else
                view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent, null));
        }
        else {
            if (selectedIds.contains(chatUserId))
                view.setBackgroundColor(context.getResources().getColor(R.color.list_selected_background));
            else
                view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
    }

    public void addToSelectedIds(long id) {
        this.selectedIds.add(id);
        notifyDataSetChanged();
    }

    public void removeFromSelectedIds(long id) {
        this.selectedIds.remove(id);
        notifyDataSetChanged();
    }

    public void clearSelectedIds() {
        this.selectedIds.clear();
        notifyDataSetChanged();
    }

    public List<Long> getSelectedIds() {
        return this.selectedIds;
    }

}
