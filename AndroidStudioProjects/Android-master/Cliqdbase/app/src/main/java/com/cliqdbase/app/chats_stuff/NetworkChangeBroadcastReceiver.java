package com.cliqdbase.app.chats_stuff;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class NetworkChangeBroadcastReceiver extends WakefulBroadcastReceiver {
    public NetworkChangeBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //startWakefulService(context, new Intent)          // TODO check if there is a task that needs to be sent
        Log.d("yuval", "On network change");
    }
}
