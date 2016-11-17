package com.screensmybiz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MessagingActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    DatabaseReference mChildReference = mRootRef.child("Messages");
    ImageView send_icon;
    ListView messagesList;
    EditText new_message;
    String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging_activity);

        sender = getSharedPreferences("user", MODE_PRIVATE).getString("user", "other");
        new_message = (EditText) findViewById(R.id.new_message);
        send_icon = (ImageView) findViewById(R.id.send_icon);
        send_icon.setOnClickListener(this);
        messagesList = (ListView) findViewById(R.id.messaging_list);

      Firebase ref = new Firebase("https://mybiz-7d682.firebaseio.com/Messages");

        FirebaseListAdapter<Message> adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.single_message_sent, ref) {
            @Override
            protected void populateView(View view, Message sMessage, int i) {
                if(sMessage.getSenderName().equals(sender)) {
                    LinearLayout ll = (LinearLayout) view.findViewById(R.id.single_msg_main_layout);
                    LinearLayout msg_background = (LinearLayout) view.findViewById(R.id.msg_background);
                    msg_background.setBackground(getResources().getDrawable(R.drawable.single_sent_msg));
                    ll.setGravity(Gravity.RIGHT);
                    TextView tv = (TextView) view.findViewById(R.id.message_txt);
                    tv.setText(sMessage.getmText());
                    Log.d("test", sMessage.getmText());
                }else{

                    LinearLayout ll = (LinearLayout) view.findViewById(R.id.single_msg_main_layout);
                    ll.setGravity(Gravity.LEFT);
                    TextView tv = (TextView) view.findViewById(R.id.message_txt);
                    LinearLayout msg_background = (LinearLayout) view.findViewById(R.id.msg_background);
                    msg_background.setBackground(getResources().getDrawable(R.drawable.single_recieved_msg));
                    ImageView check = (ImageView) view.findViewById(R.id.check);
                    tv.setText(sMessage.getmText());
                    Log.d("test", sMessage.getmText());

                    if(sender.equals("user")){
                        check.setVisibility(View.GONE);
                    }
                }


                TextView time = (TextView) view.findViewById(R.id.time);
                time.setText(convertSecondsToHMmSs(sMessage.getTimeStamp()));
            }
        };

        messagesList.setAdapter(adapter);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_icon:
                Log.d("test", "message sent");
                Message mmm = new Message( sender,"123", "user","123", new_message.getText().toString(), 111, System.currentTimeMillis());
                mChildReference.push().setValue(mmm);
                new_message.getText().clear();
                break;

        }
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d", h,m);
    }
}
