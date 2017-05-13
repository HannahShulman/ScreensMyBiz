package com.cliqdbase.app._fragments;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.google_maps.GetLocationListAsync;
import com.cliqdbase.app.async.google_maps.GetLocationListAsyncCallback;

import java.util.List;

public class GmapsSearchFragment extends ListFragment {

    private EditText query_et;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_gmaps_search, container, false);

        this.query_et = (EditText) view.findViewById(R.id.google_maps_query_edit_text);

        Button runQuery_button = (Button) view.findViewById(R.id.run_query);
        runQuery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetLocationListAsync(getActivity(), query_et.getText().toString(), new GetLocationListAsyncCallback() {
                    @Override
                    public void callback(List<String> addresses) {
                        if (addresses != null)
                            getListView().setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, addresses));
                    }
                }).execute();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null)
            actionBar.setTitle("Location Test");
        super.onResume();
    }
}
