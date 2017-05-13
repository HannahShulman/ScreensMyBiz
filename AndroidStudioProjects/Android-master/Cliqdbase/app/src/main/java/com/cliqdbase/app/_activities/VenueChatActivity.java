package com.cliqdbase.app._activities;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.venue_chat.Message;
import com.cliqdbase.app.venue_chat.SocketThread;
import com.cliqdbase.app.venue_chat.VenueChatArrayAdapter;

import java.util.ArrayList;

public class VenueChatActivity extends ListActivity implements View.OnClickListener{

    private String venue;
    private SocketThread socketThread;

    private ArrayList<Message> messages;

    private BroadcastReceiver socketDataReceivedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            Message message = null;

            if (extras != null)
                message = extras.getParcelable(IntentConstants.INTENT_EXTRA_VENUE_CHAT_SOCKET_MESSAGE_KEY);

            if (message == null) {
                Toast.makeText(VenueChatActivity.this, "Disconnected from server", Toast.LENGTH_LONG).show();
            }
            else {
                messages.add(message);
                ((VenueChatArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_chat);

        // Registering the BroadcastReceiver locally.
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(VenueChatActivity.this);
        localBroadcastManager.registerReceiver(socketDataReceivedBroadcastReceiver, new IntentFilter(IntentConstants.INTENT_FILTER_VENUE_CHAT_SOCKET_BROADCAST));


        // Setting the on click for the send button.
        Button sendButton = (Button) findViewById(R.id.venue_chat_send_button);
        sendButton.setOnClickListener(this);


        // Setting the list adapter.
        this.messages = new ArrayList<>();
        VenueChatArrayAdapter adapter = new VenueChatArrayAdapter(getApplicationContext(), R.layout.venue_chat_one_message, this.messages);
        getListView().setAdapter(adapter);

        // finding the venue name from the intent.
        Intent intent = getIntent();
        this.venue = intent.getStringExtra(IntentConstants.INTENT_EXTRA_VENUE_CHAT_NAME_KEY);

        if (this.venue == null)
            this.venue = "Temp Place";


        // Connecting to the server.
        this.socketThread = new SocketThread(VenueChatActivity.this, this.venue);
        this.socketThread.start();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.venue_chat_send_button:
                EditText messageText_et = (EditText) findViewById(R.id.venue_chat_message_edit_text);
                String messageText = messageText_et.getText().toString();
                messageText_et.setText("");
                this.socketThread.sendMessageToServer(messageText);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_venue_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(VenueChatActivity.this);
        localBroadcastManager.unregisterReceiver(socketDataReceivedBroadcastReceiver);

        this.socketThread.closeConnection();

        super.onDestroy();
    }
}
