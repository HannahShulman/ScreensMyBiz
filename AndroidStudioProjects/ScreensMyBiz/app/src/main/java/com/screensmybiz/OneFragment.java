package com.screensmybiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by hannashulmah on 09/11/2016.
 */
public class OneFragment extends Fragment{
    ListView listView;
    ArrayList<LastMessage>list;
    LastMessageAdapter adapter;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
        listView = (ListView) rootView.findViewById(R.id.last_msg_list);
        list = new ArrayList<>();
        LastMessage lastMessage1 = new LastMessage("הסעות","הסעות דרכי מלך", "אוקי, אז סגרנו  במוצ\"ש בתשע", R.drawable.profile2, "17:38", false);
        LastMessage lastMessage2 = new LastMessage("חשמלאי","שלום לוי", "תודה רבה", R.drawable.profile2, "14:56", true);
        LastMessage lastMessage3 = new LastMessage("בייביסיטר","חני שולמן", "היום ב6, טוב??", R.drawable.profile2, "01:15", true);
        LastMessage lastMessage4 = new LastMessage("פיצה","פיצה ירושלים", "תעדכן שהמגש מוכן", R.drawable.profile2, "01:15", true);
        list.add(lastMessage1);
        list.add(lastMessage2);
        list.add(lastMessage3);
        list.add(lastMessage4);
        adapter = new LastMessageAdapter(getContext(), list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getBaseContext(), MessagingActivity.class);
                startActivity(i);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

}