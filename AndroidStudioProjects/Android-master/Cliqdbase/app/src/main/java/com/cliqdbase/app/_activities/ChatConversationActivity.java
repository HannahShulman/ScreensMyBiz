package com.cliqdbase.app._activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app.chats_stuff.ChatsSQLiteHelper;
import com.cliqdbase.app.chats_stuff.SendChatMessageService;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.chats_stuff.ConversationCursorAdapter;
import com.cliqdbase.app.chats_stuff.LoadAdapterAsync;
import com.cliqdbase.app.chats_stuff.OnAdapterTaskComplete;
import com.cliqdbase.app.server_model.ChatMessage;

import java.util.ArrayList;

/**
 * This activity is the chat conversation activity.
 * In this activity the user will be able to view a conversation between him and another user.
 */
public class ChatConversationActivity extends ListActivity implements OnAdapterTaskComplete, View.OnClickListener {

    private long chatUserId;

    private ConversationCursorAdapter currentAdapter;

    private EditText newMessageText_et;

    private ArrayList<String> selected_messages_text;

    private LoadAdapterAsync loadAdapterAsync;

    /**
     * This is a local broadcast receiver. This will update our UI if in the background service the database has changed. e.g. a new message had arrived.
     */
    private BroadcastReceiver databaseChangedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean arrayContainsThisId = false;

            Bundle extras = intent.getExtras();
            if (extras == null || !extras.containsKey(IntentConstants.INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES))
                return;

            long userIdArr[] = extras.getLongArray(IntentConstants.INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES);      // we only need to change the UI if a new message had been received in the current conversation. (NOTICE: this is a chat conversation activity, not the chat main activity!)
            if (userIdArr != null){
                for (long id : userIdArr) {
                    if (id == chatUserId) {
                        arrayContainsThisId = true;
                        refreshListAdapter();
                        break;
                    }
                }
            }

            if (!arrayContainsThisId) {
                //TODO handle notification. (The notification was created at the GcmIntentService, we need to remove the messages from this id from the notification
                Log.d(getLocalClassName(), "if");
            }


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_conversation);


        newMessageText_et = (EditText) findViewById(R.id.send_message_edit_text);

        loadAdapterAsync = null;


        Bundle extras = getIntent().getExtras();        // This activity must receive an intent with the id of the user with whom we want to converse.
        if (extras == null || !extras.containsKey(IntentConstants.INTENT_EXTRA_CHAT_USER_ID)) {
            System.out.println("extras error");
            finish();
            return;
        }
        this.chatUserId = extras.getLong(IntentConstants.INTENT_EXTRA_CHAT_USER_ID);

        System.out.println("id: " + this.chatUserId);

        Button sendMessageButton = (Button) findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(this);


        // Receiving the draft from the database and displaying it.
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return ChatMessage.findDraftMessage(ChatConversationActivity.this, chatUserId);
            }

            @Override
            protected void onPostExecute(String s) {
                newMessageText_et.setText(s);
                newMessageText_et.setSelection(s.length());
            }
        }.execute();

        selected_messages_text = new ArrayList<>();

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);       // enabling the user to multi-select the messages
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {        // Procedure for selecting/deselecting an item on the list
                int checkedCount = getListView().getCheckedItemCount();
                switch (checkedCount) {     // Changing the text on the CAB
                    case 0:
                        mode.setSubtitle(null);
                        break;
                    case 1:
                        mode.setSubtitle("1 message selected");
                        break;
                    default:
                        mode.setSubtitle(checkedCount + " messages selected");
                }

                Cursor cursor = (Cursor) getListAdapter().getItem(position);

                long messageId = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID));
                String messageText = cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TEXT));

                if (checked) {      // if an item is selected, we will add it to our lists, in order to manipulate that list after the user takes action.
                    currentAdapter.addIdToSelected(messageId);
                    selected_messages_text.add(messageText);
                }
                else {
                    currentAdapter.removeIdFromSelected(messageId);
                    selected_messages_text.remove(messageText);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {     // Creating CAB
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.menu_contextual_chats_selected, menu);
                mode.setTitle("Select Messages");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {        // CAB action buttons procedure
                switch (item.getItemId()) {
                    case R.id.action_delete:        // delete the messages from our selected messages list.
                        AlertDialog alertDialog = new AlertDialog.Builder(ChatConversationActivity.this)
                            .setTitle(R.string.delete_chats_title)
                            .setMessage(R.string.delete_chats_content)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            ChatMessage.deleteChatMessages(ChatConversationActivity.this, currentAdapter.getSelectedIds());
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            Toast.makeText(ChatConversationActivity.this, "Messages deleted", Toast.LENGTH_LONG).show();
                                            refreshListAdapter();
                                        }
                                    }.execute();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unselect_all();
                                }
                            })
                            .create();
                        alertDialog.show();
                        break;
                    case R.id.action_copy:          // Copying the text of the selected messages
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Messages Text", getMessagesTextFromSelectedRows()));

                        break;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                currentAdapter.clearSelectedIds();
            }
        });
    }

    /**
     * De-selecting all of the list items
     */
    private void unselect_all() {
        getListView().clearChoices();
        getListView().requestLayout();
        currentAdapter.clearSelectedIds();
    }


    /**
     * Creates a string of the text from the selected messages.
     * @return The created string.
     */
    private String getMessagesTextFromSelectedRows() {
        StringBuilder builder = new StringBuilder();

        for (String string : selected_messages_text) {
            builder.append(string);
            builder.append("\n");
        }

        return builder.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_message_button:      // Starting a service to send the new message
                final String messageText = newMessageText_et.getText().toString();
                if (messageText.trim().isEmpty())
                    return;

                Intent intent = new Intent(ChatConversationActivity.this, SendChatMessageService.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_USER_ID, this.chatUserId);
                intent.putExtra(IntentConstants.INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_TEXT, messageText);

                startService(intent);

                newMessageText_et.setText("");      // Clearing the edit text.
                break;
        }
    }

    /**
     * Refreshes the cursor adapter of this list.
     */
    private void refreshListAdapter() {
        if (this.loadAdapterAsync != null)
            this.loadAdapterAsync.closeDatabaseConnection();
        loadAdapterAsync = new LoadAdapterAsync(ChatConversationActivity.this, this, this.chatUserId);
        loadAdapterAsync.execute();
    }


    @Override
    public void onTaskComplete(CursorAdapter cursorAdapter) {
        if (this.currentAdapter != null)
            this.currentAdapter.getCursor().close();

        this.currentAdapter = (ConversationCursorAdapter) cursorAdapter;
        //getListView().setAdapter(this.currentAdapter);
        setListAdapter(this.currentAdapter);
    }

    @Override
    protected void onResume() {
        refreshListAdapter();


        // re-registering our localBroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(IntentConstants.INTENT_FILTER_MESSAGES_DATABASE_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(databaseChangedBroadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // un-registering our local broadcase receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(databaseChangedBroadcastReceiver);
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (this.currentAdapter != null)
            this.currentAdapter.getCursor().close();


        /*
        If the user had left the activity without sending his message,
        we will save the message he had left as a draft.
         */
        String text = this.newMessageText_et.getText().toString();

        if (!text.trim().isEmpty())
            ChatMessage.insertNewDraftToDatabase(ChatConversationActivity.this, this.chatUserId, text);
        else
            ChatMessage.deleteDraftFromDatabase(ChatConversationActivity.this, this.chatUserId);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_conversation) {        // Deleted this entire conversation.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<Long> userIdAsList = new ArrayList<>();
                    userIdAsList.add(chatUserId);
                    ChatMessage.deleteChatConversations(ChatConversationActivity.this, userIdAsList);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Toast.makeText(ChatConversationActivity.this, "Conversations deleted", Toast.LENGTH_LONG).show();
                    ChatConversationActivity.this.finish();
                }
            }.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
