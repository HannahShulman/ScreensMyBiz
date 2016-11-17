package com.screensmybiz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hannashulmah on 10/11/2016.
 */
public class ServiceAdapter extends BaseAdapter {
    ArrayList<Service> service_list;
    Context ctx;

    public ServiceAdapter(ArrayList<Service> service_list, Context ctx) {
        this.service_list = service_list;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return service_list.size();
    }

    @Override
    public Service getItem(int position) {
        return service_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    ViewHolder holder = null;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = new ViewHolder();
        if(convertView==null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.service_grid_layout, parent, false);
            holder.image = (ImageView) convertView.findViewById(R.id.image_service);
//            holder.serciveTitle = (TextView) convertView.findViewById(R.id.service_title);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Bitmap mbitmap = ((BitmapDrawable)ctx.getResources().getDrawable(service_list.get(position).getImage())).getBitmap();
        Bitmap imageRounded = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
        Canvas canvas = new Canvas(imageRounded);
        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 40, 40, mpaint);// Round Image Corner 100 100 100 100

        holder.image.setImageBitmap(imageRounded);
//        holder.serciveTitle.setText(service_list.get(position).getTitle());
        return convertView;
    }

    class ViewHolder{
        ImageView image;
        TextView serciveTitle;

    }
}
