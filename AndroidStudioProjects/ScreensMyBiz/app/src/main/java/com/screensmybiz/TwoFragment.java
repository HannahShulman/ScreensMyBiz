package com.screensmybiz;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class TwoFragment extends Fragment{
    GridView gridView;
    LinearLayout container;
    ListView singleServiceList, messageList;
    ArrayList<SingleService> serviceList;
    View c;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_two, container, false);
        c = rootView.findViewById(R.id.container);
        gridView = (GridView) rootView.findViewById(R.id.gridview);

        Service ser1 = new Service(R.drawable.instelator, getResources().getString(R.string.plumber));
        Information.serviceList.add(ser1);
        Service ser2 = new Service(R.drawable.babysitter, getResources().getString(R.string.babysitter));
        Information.serviceList.add(ser2);
        Service ser3 = new Service(R.drawable.hadbara, "Pest Control");
        Information.serviceList.add(ser3);
        Service ser4 = new Service(R.drawable.private_teaching, "Private teaching");
        Information.serviceList.add(ser4);
        Service ser5 = new Service(R.drawable.electrition, "electrition");
        Information.serviceList.add(ser5);
        Service ser6 = new Service(R.drawable.pc_thec, "PC Tech");
        Information.serviceList.add(ser6);
        Service ser7 = new Service(R.drawable.air_conditionning, "air_conditionning");
        Information.serviceList.add(ser7);
        Service ser8 = new Service(R.drawable.mtavchim, "agent");
        Information.serviceList.add(ser8);
        Service ser9 = new Service(R.drawable.veterinar, "veterinar");
        Information.serviceList.add(ser9);
        Service ser10 = new Service(R.drawable.cleanning, "cleanning");
        Information.serviceList.add(ser10);
        Service ser11 = new Service(R.drawable.cosmetics, "cosmetics");
        Information.serviceList.add(ser11);
        Service ser12 = new Service(R.drawable.shipuznik, "shipuznik");
        Information.serviceList.add(ser12);


        ServiceAdapter adapter = new ServiceAdapter(Information.serviceList, getContext());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (((LinearLayout) c).getChildCount() > 0)
                    ((LinearLayout) c).removeAllViews();
                singleServiceList = new ListView(getContext());

                serviceList = new ArrayList<>();
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        serviceList.add(new SingleService("כותרת 1", "כתובת 1", "תאור 1", "עיר 1", R.drawable.veterinar, true, 10, 2));
                        serviceList.add(new SingleService("כותרת 1", "כתובת 1", "תאור 1", "יער 2", R.drawable.veterinar, true, 10, 1));
                        serviceList.add(new SingleService("title 3", "address 3", "description 3", " town  ", R.drawable.veterinar, true, 20, 2));
                        serviceList.add(new SingleService("title 4", "address 4", "description 4", "townnnn", R.drawable.veterinar, true, 10, 2));
                        serviceList.add(new SingleService("title 5", "address 5", "description 5", "opafiosh", R.drawable.veterinar, true, 40, 2));
//                getInfo(serviceList,position);
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        singleServiceList.setAdapter(new SingleServiceAdapter(getContext(), serviceList));
                    }
                }.execute();

                singleServiceList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                Resources r = getResources();
                float padding_top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12.75f, r.getDisplayMetrics());
//        singleServiceList.setPadding(0, (int)padding_top, 0, 0);
                float divider_height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.75f, r.getDisplayMetrics());
                singleServiceList.setDividerHeight((int) divider_height);
                TextView tv = new TextView(getContext());
                tv.setBackgroundColor(getContext().getResources().getColor(R.color.yellow_star));
                tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                tv.setHeight(-15);
                singleServiceList.addHeaderView(tv);
                ((LinearLayout) c).addView(singleServiceList);



            }

        });




        return rootView;
    }





}

