package com.cliqdbase.app._fragments;

import android.app.ActionBar;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app._activities.ChatConversationActivity;
import com.cliqdbase.app._activities.SignUpActivity;
import com.cliqdbase.app.chats_stuff.ChatsSQLiteHelper;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.chats_stuff.ChatsCursorAdapter;
import com.cliqdbase.app.chats_stuff.LoadAdapterAsync;
import com.cliqdbase.app.chats_stuff.OnAdapterTaskComplete;
import com.cliqdbase.app.general.Common;
import com.cliqdbase.app.server_model.ChatMessage;


public class ChatMainFragment extends ListFragment implements OnAdapterTaskComplete {

    private ChatsCursorAdapter currentAdapter;
    private final BroadcastReceiver databaseChangedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshListAdapter();
        }
    };


    private LoadAdapterAsync loadAdapterAsync;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (Common.isLoggedInAsGuest(getActivity())) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.not_accessible_for_guest_title)
                    .setMessage(R.string.not_accessible_for_guest_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.sign_up, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), SignUpActivity.class);
                            intent.putExtra(IntentConstants.INTENT_EXTRA_GUEST_WANT_TO_SIGN_UP, true);
                            startActivity(intent);

                            getActivity().getSupportFragmentManager().beginTransaction().remove(ChatMainFragment.this).commit(); // Closing the fragment
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(ChatMainFragment.this).commit(); // Closing the fragment
                        }
                    })
                    .create()
                    .show();
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_chat_main, container, false);

        this.loadAdapterAsync = null;

        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intent intent = new Intent(ChatMainActivity.this, GcmIntentService.class);
        //startService(intent);

        setListViewSettings();
    }


    private void setListViewSettings() {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getListView().getCheckedItemCount() == 0) {
                    // Extracting the conversation id.
                    TextView chatUserId_tv = (TextView) view.findViewById(R.id.chat_item_user_id);
                    long chatUserId = Long.parseLong(chatUserId_tv.getText().toString());

                    Intent intent = new Intent(getActivity(), ChatConversationActivity.class);
                    intent.putExtra(IntentConstants.INTENT_EXTRA_CHAT_USER_ID, chatUserId);
                    startActivity(intent);
                }
            }
        });


        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {        // CAB and list item multi-select procedures
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int checkedCount = getListView().getCheckedItemCount();
                switch (checkedCount) {
                    case 0:
                        mode.setSubtitle(null);
                        break;
                    case 1:
                        mode.setSubtitle("1 conversation selected");
                        break;
                    default:
                        mode.setSubtitle(checkedCount + " conversations selected");
                }

                Cursor cursor = (Cursor) getListAdapter().getItem(position);
                long userId = cursor.getLong(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_USER_ID));

                if (checked)
                    currentAdapter.addToSelectedIds(userId);
                else
                    currentAdapter.removeFromSelectedIds(userId);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = getActivity().getMenuInflater();
                menuInflater.inflate(R.menu.menu_contextual_chats_selected, menu);
                mode.setTitle("Select Conversations");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.delete_chats_title)
                            .setMessage(R.string.delete_chats_content)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            ChatMessage.deleteChatConversations(getActivity(), currentAdapter.getSelectedIds());
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            Toast.makeText(getActivity(), "Conversations deleted", Toast.LENGTH_LONG).show();
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
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                unselect_all();
            }
        });
    }


    private void unselect_all() {
        getListView().clearChoices();
        getListView().requestLayout();
        currentAdapter.clearSelectedIds();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_chat_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_newMessage) {
            final NumberPicker numberPicker = new NumberPicker(getActivity());

            final int maxId = 500;

            String num[] = new String[maxId];
            for (int i = 0; i < maxId; i++)
                num[i] = String.valueOf(i+1);

            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(maxId);
            numberPicker.setDisplayedValues(num);
            numberPicker.setValue(100);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(numberPicker)
                    .setTitle("Select user id")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = numberPicker.getValue();
                            Intent intent = new Intent(getActivity(), ChatConversationActivity.class);
                            intent.putExtra(IntentConstants.INTENT_EXTRA_CHAT_USER_ID, (long)id);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();

            dialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshListAdapter() {
        if (loadAdapterAsync != null)
            loadAdapterAsync.closeDatabaseConnection();
        loadAdapterAsync = new LoadAdapterAsync(getActivity(), this, null);
        loadAdapterAsync.execute();
    }

    @Override
    public void onTaskComplete(CursorAdapter cursorAdapter) {
        if (this.currentAdapter != null)
            this.currentAdapter.getCursor().close();

        this.currentAdapter = (ChatsCursorAdapter) cursorAdapter;
        setListAdapter(this.currentAdapter);
    }

    @Override
    public void onResume() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null)
            actionBar.setTitle("Chats");
        refreshListAdapter();
        IntentFilter intentFilter = new IntentFilter(IntentConstants.INTENT_FILTER_MESSAGES_DATABASE_UPDATED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(databaseChangedBroadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(databaseChangedBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (this.currentAdapter != null)
            this.currentAdapter.getCursor().close();

        if (loadAdapterAsync != null)
            loadAdapterAsync.closeDatabaseConnection();

        super.onDestroy();
    }
}
