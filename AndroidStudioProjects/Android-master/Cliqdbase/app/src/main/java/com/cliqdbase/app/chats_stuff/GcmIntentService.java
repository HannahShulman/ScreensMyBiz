package com.cliqdbase.app.chats_stuff;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cliqdbase.app.R;
import com.cliqdbase.app._fragments.ChatMainFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.server_model.ChatMessage;


public class GcmIntentService extends IntentService implements AsyncResponse_Server {

    private Intent intent;

    public GcmIntentService() {
        super("GcmIntentService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("CGM", "Service");

        // We received a GCM sync message - We need to extract messages from our server.
        SharedPreferences prefs = getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        long maxMidSender = prefs.getLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_SENDER, 0);
        long maxMidRecipient = prefs.getLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_RECIPIENT, 0);

        JsonObject jsonParams = new JsonObject();

        jsonParams.addProperty("messageIdSender", maxMidSender);            // If doesn't exists, we will send 0 and still get all the messages
        jsonParams.addProperty("messageIdRecipient", maxMidRecipient);

        this.intent = intent;
        ConnectToServer async = new ConnectToServer(getApplicationContext(), false, 0);
        async.delegate = this;
        async.executeServletOnServer(ServerUrlConstants.SERVER_HOST + ServerUrlConstants.GET_CHAT_MESSAGES, "POST", jsonParams.toString(), false);
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        System.out.println(data);

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(data));
        reader.setLenient(true);
        Type messagesType = new TypeToken<ChatMessage.MessagesListToPhone>() {}.getType();
        ChatMessage.MessagesListToPhone messages = gson.fromJson(reader, messagesType);


        if (messages == null) {
            GcmBroadcastReceiver.completeWakefulIntent(this.intent);
            return;
        }

        // Inserting the given data to the database.
        ArrayList<Long> userIds = ChatMessage.insertMessagesAndUserDataToDatabase(getApplicationContext(), messages);

        // Updating the maximum ids of the messages in the shared preferences.
        SharedPreferences prefs = getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_SENDER, messages.getNewMaxMessageIdSender());
        editor.putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_RECIPIENT, messages.getNewMaxMessageIdRecipient());
        editor.apply();


        Intent updateIntent = new Intent(IntentConstants.INTENT_FILTER_MESSAGES_DATABASE_UPDATED);
        updateIntent.putExtra(IntentConstants.INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES, toPrimitiveLongArray(userIds));


        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);


        Intent nIntent = new Intent(getApplicationContext(), ChatMainFragment.class);
        PendingIntent nPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, nIntent, 0);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("New chat messages!")
                    .setContentText("")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(nPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(12345, notification);
        }


        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(this.intent);
    }


    private long[] toPrimitiveLongArray(List<Long> lst) {
        int size = lst.size();
        long[] arr = new long[size];

        for (int i = 0; i < size; i++)
            arr[i] = lst.get(i);

        return arr;
    }

}
