package com.cliqdbase.app.chats_stuff;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cliqdbase.app.R;
import com.cliqdbase.app.general.Common;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Yuval on 13/05/2015.
 *
 * @author Yuval Siev
 */
public class ConversationCursorAdapter extends CursorAdapter {

    private ArrayList<Long> selectedIds;


    public ConversationCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.selectedIds = new ArrayList<>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.message_single_item, parent, false);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void bindView(View view, Context context, Cursor cursor) {
        TextView messageText_tv = (TextView) view.findViewById(R.id.message_text);
        TextView messageTime_tv = (TextView) view.findViewById(R.id.message_time);
        TextView messageId_tv = (TextView) view.findViewById(R.id.message_id);

        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.message_item_container);

        String text = cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TEXT));
        long time = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TIME));
        long id = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID));

        String messageTime = Common.getTimeString(time);

        messageText_tv.setText(text);
        messageTime_tv.setText(messageTime);
        messageId_tv.setText(String.valueOf(id));

        int side = cursor.getInt(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_SIDE));
        int color;
        int gravity;
        if (side == 1) {
            if (Build.VERSION.SDK_INT >= 23)
                color = context.getResources().getColor(R.color.message_sent, null);
            else
                color = context.getResources().getColor(R.color.message_sent);
            gravity = Gravity.END;
        }
        else {
            if (Build.VERSION.SDK_INT >= 23)
                color = context.getResources().getColor(R.color.message_received, null);
            else
                color = context.getResources().getColor(R.color.message_received);
            gravity = Gravity.START;
        }

        container.setBackgroundColor(color);
        container.setGravity(gravity);

        if (selectedIds.contains(id)) {
            if (Build.VERSION.SDK_INT >= 23)
                view.setBackgroundColor(context.getResources().getColor(R.color.list_selected_background, null));
            else
                view.setBackgroundColor(context.getResources().getColor(R.color.list_selected_background));
        }
        else {
            if (Build.VERSION.SDK_INT >= 23)
                view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent, null));
            else
                view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
    }


    public void addIdToSelected(long id) {
        this.selectedIds.add(id);
        notifyDataSetChanged();
    }


    public void removeIdFromSelected(long id) {
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
