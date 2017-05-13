package com.cliqdbase.app.chats_stuff;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.CursorAdapter;

/**
 * Created by Yuval on 13/05/2015.
 *
 * @author Yuval Siev
 */
public class LoadAdapterAsync extends AsyncTask<Void, Void, CursorAdapter>{
    private Context context;
    private OnAdapterTaskComplete callback;
    private Long id;

    private SQLiteDatabase db;

    /**
     * Creates the Async object.
     * This constructor defines what adapter this Async will return, depending on the given id.
     * @param context     The application's context.
     * @param callback    A callback function to be called after the com.cliqdbase.app.async completes.
     * @param id          The id of a specific chat conversation adapter, or null for the chats table adapter.
     */
    public LoadAdapterAsync(Context context, OnAdapterTaskComplete callback, Long id) {
        this.context = context;
        this.callback = callback;
        this.id = id;
    }

    @Override
        protected CursorAdapter doInBackground(Void... params) {
            ChatsSQLiteHelper helper = new ChatsSQLiteHelper(this.context);
            db = helper.getReadableDatabase();

            if (this.id == null)
                return new ChatsCursorAdapter(this.context, db.rawQuery(ChatsSQLiteHelper.SELECT_CHAT_CONVERSATIONS_LIST, null));

            String sqlParam[] = {String.valueOf(this.id)};
            return new ConversationCursorAdapter(this.context, db.rawQuery(ChatsSQLiteHelper.SELECT_CONVERSATION_MESSAGES_LIST, sqlParam));
    }


    @Override
    protected void onPostExecute(CursorAdapter adapter) {
        if (callback != null)
            callback.onTaskComplete(adapter);
    }

    public void closeDatabaseConnection() {
        if (db != null)
            db.close();
    }

}
