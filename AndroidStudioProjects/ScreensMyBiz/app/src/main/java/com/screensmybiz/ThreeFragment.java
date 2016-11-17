package com.screensmybiz;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ThreeFragment extends Fragment {


    ImageView image;
    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_three, container, false);

        image = (ImageView) rootview.findViewById(R.id.image_t);
//        image.setImageResource(R.drawable.michraz);
        // Inflate the layout for this fragment
        return rootview;
    }

}
