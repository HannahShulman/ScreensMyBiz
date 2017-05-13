package com.cliqdbase.app.chats_stuff;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;

import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.server_model.ChatMessage;


public class SendChatMessageService extends IntentService implements AsyncResponse_Server {

    // The wakelock to ensure that this service will complete it's work.
    private PowerManager.WakeLock wakeLock;

    private long recipientUserId;

    public SendChatMessageService() {
        super("SendChatMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SendChatMessageService");     // The tag is just for debugging purposes
        this.wakeLock.acquire();

        Bundle extras = intent.getExtras();

        if (extras == null) {
            releaseWakeLock();
            return;
        }
        this.recipientUserId = extras.getLong(IntentConstants.INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_USER_ID, -1);
        String messageText = extras.getString(IntentConstants.INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_TEXT);


        if (this.recipientUserId == -1 || messageText == null) {
            releaseWakeLock();
            return;
        }


        // TODO remove this after cliqs list is available
        checkIfUserExistsInDb();


        ChatMessage.FromPhone chatMessage = new ChatMessage.FromPhone(this.recipientUserId, messageText);

        Gson gson = new Gson();
        Type chatMessageType = new TypeToken<ChatMessage.FromPhone>() {}.getType();

        String jsonParam = gson.toJson(chatMessage, chatMessageType);


        SharedPreferences prefs = getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long tempMessageId = prefs.getLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_TEMP_MESSAGE_ID, -1);
        editor.putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_TEMP_MESSAGE_ID, tempMessageId-1).apply();

        // Insert the message to the local database.
        ChatMessage.insertMessageToDatabase(getApplicationContext(), tempMessageId, chatMessage.getMessageText(), System.currentTimeMillis(), 'C', 1, this.recipientUserId);
        long idAsArr[] = new long[1];
        idAsArr[0] = this.recipientUserId;
        Intent intent1 = new Intent(IntentConstants.INTENT_FILTER_MESSAGES_DATABASE_UPDATED);
        intent1.putExtra(IntentConstants.INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES, idAsArr);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);




        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            // Sending the message to the server
            ConnectToServer sendMessageAsync = new ConnectToServer(getApplicationContext(), false, tempMessageId);      // The com.cliqdbase.app.async task code is the temp message id. In that way we will know which message id we should update after receiving a response from the server.
            sendMessageAsync.delegate = this;
            sendMessageAsync.executeServletOnServer(ServerUrlConstants.SERVER_HOST + ServerUrlConstants.SEND_CHAT_MESSAGE, "POST", jsonParam, false);
        }
        else {
            //long messagesArray = prefs.get

            // TODO create an offline queue of tasks that needed to be completed once network connection will be available

            releaseWakeLock();
        }
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        final char status;
        final long newMessageId;
        if (httpResultCode == 200) {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(data));
            reader.setLenient(true);

            Type longType = new TypeToken<Long>(){}.getType();
            newMessageId = gson.fromJson(reader, longType);

            status = 'S';
        }
        else {
            status = 'E';
            newMessageId = -1;      // We didn't receive a new id for the message, therefore, we will pass -1 here to keep the message's old id.
        }


        ChatMessage.updateIdOfSentMessage(getApplicationContext(), taskCode, newMessageId, status);

        // Update UI thread if needed
        long userId[] = new long[1];
        userId[0] = this.recipientUserId;

        Intent updateUiIntent = new Intent(IntentConstants.INTENT_FILTER_MESSAGES_DATABASE_UPDATED);
        updateUiIntent.putExtra(IntentConstants.INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES, userId);             // Needed to update the ui thread. See ChatConversationActivity#databaseChangedBroadcastReceiver
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateUiIntent);

        // Releasing the wake lock.
        releaseWakeLock();
    }


    private void releaseWakeLock() {
        this.wakeLock.release();
    }



    private void checkIfUserExistsInDb() {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ChatsSQLiteHelper.USERS_TABLE + " WHERE " + ChatsSQLiteHelper.COLUMN_USERS_USER_ID + " = ?", new String[]{String.valueOf(this.recipientUserId)});
        if (!cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(ChatsSQLiteHelper.COLUMN_USERS_USER_ID, this.recipientUserId);
            values.put(ChatsSQLiteHelper.COLUMN_USERS_FIRST_NAME, "First");
            values.put(ChatsSQLiteHelper.COLUMN_USERS_LAST_NAME, "Last");
            db.insert(ChatsSQLiteHelper.USERS_TABLE, null, values);
        }

        cursor.close();
        db.close();
        helper.close();
    }
}

