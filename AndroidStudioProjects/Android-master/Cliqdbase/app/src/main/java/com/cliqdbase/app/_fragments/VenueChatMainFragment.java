package com.cliqdbase.app._fragments;


import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cliqdbase.app.R;
import com.cliqdbase.app._activities.VenueChatActivity;
import com.cliqdbase.app.constants.IntentConstants;

public class VenueChatMainFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_venue_chat_main, container, false);

        Button b1 = (Button) view.findViewById(R.id.button4);
        Button b2 = (Button) view.findViewById(R.id.button5);
        Button b3 = (Button) view.findViewById(R.id.button6);

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null)
            actionBar.setTitle("Venue Chat");
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        String venueName = null;
        switch (v.getId()) {
            case R.id.button4:
                venueName = "Venue #1";
                break;
            case R.id.button5:
                venueName = "Venue #2";
                break;
            case R.id.button6:
                venueName = "Venue #3";
                break;
        }

        Intent intent = new Intent(getActivity(), VenueChatActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_VENUE_CHAT_NAME_KEY, venueName);
        startActivity(intent);
    }
}
