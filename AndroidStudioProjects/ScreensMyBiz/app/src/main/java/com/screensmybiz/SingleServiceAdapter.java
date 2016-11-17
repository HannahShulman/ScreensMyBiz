package com.screensmybiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hannashulmah on 14/11/2016.
 */
public class SingleServiceAdapter extends BaseAdapter {

    Context ctx;
    ArrayList<SingleService> list;

    public SingleServiceAdapter(Context ctx, ArrayList<SingleService> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public SingleService getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    SingleServiceViewHolder viewHolder=null;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        viewHolder = new SingleServiceViewHolder();
        if (convertView==null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.single_service_info, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.service_title);
            viewHolder.address = (TextView) convertView.findViewById(R.id.service_address);
            viewHolder.description = (TextView) convertView.findViewById(R.id.service_description);
            viewHolder.profile = (ImageView) convertView.findViewById(R.id.single_profile_image);
            viewHolder.reviews = (TextView) convertView.findViewById(R.id.num_reviews);
            viewHolder.rb = (RatingBar) convertView.findViewById(R.id.ratingBar);
            viewHolder.chat_layout = (LinearLayout) convertView.findViewById(R.id.chat_layout);
            convertView.setTag(viewHolder);

        }else {
            viewHolder = (SingleServiceViewHolder) convertView.getTag();
        }

        LayerDrawable stars = (LayerDrawable) viewHolder.rb.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(ctx.getResources().getColor(R.color.yellow_star), PorterDuff.Mode.SRC_IN);
        viewHolder.title.setText(list.get(position).getTitle());
        String allAddress = list.get(position).getAddress()+"   "+list.get(position).getTown();
        final SpannableStringBuilder str = new SpannableStringBuilder(allAddress);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), list.get(position).getAddress().length()+3, allAddress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        viewHolder.chat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(ctx, MessagingActivity.class));
            }
        });

//        viewHolder.address.setText(list.get(position).getAddress()+ "    "+str);
        viewHolder.address.setText(str);

        viewHolder.description.setText(list.get(position).getDescription());
//        viewHolder.profile.setImageResource(list.get(position).getImage());
        viewHolder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "here should be something", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.reviews.setText("("+list.get(position).getNum_reviews()+")");
//        viewHolder.rb.setRating(2f);
        viewHolder.rb.setStepSize(0.5f);
//        viewHolder.rb.
        viewHolder.rb.setRating(list.get(position).getNum_stars());



        return convertView;
    }

    class SingleServiceViewHolder{

        ImageView profile, like, share;
        TextView title, address, description, reviews;
        RatingBar rb;
        LinearLayout chat_layout;


    }
}
