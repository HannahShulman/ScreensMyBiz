package com.cliqdbase.app.search_filter_stuff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cliqdbase.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuval on 05/08/2015.
 *
 * @author Yuval Siev
 */
public class ColorsArrayAdapter extends ArrayAdapter<MyColor> implements Filterable {

    private List<MyColor> allColorsBackup;
    private Context context;
    private int layoutResourceId;

    public ColorsArrayAdapter(Context context, int layoutResourceId, List<MyColor> items) {
        super(context, layoutResourceId, items);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.allColorsBackup = new ArrayList<>(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);

        MyColor myColor = getItem(position);
        String prettyString = myColor.getPrettyString();

        ImageView colorImageView = (ImageView) convertView.findViewById(R.id.item_color_image_view);
        TextView colorTextView = (TextView) convertView.findViewById(R.id.item_color_text_view);

        if (!myColor.isOther()) {
            colorImageView.setVisibility(View.VISIBLE);
            colorImageView.setBackgroundColor(0xFF000000 + myColor.getExactColorCode());
        }
        else
            colorImageView.setVisibility(View.INVISIBLE);
        colorTextView.setText(prettyString);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    private Filter filter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue == null)
                return null;
            return ((MyColor)resultValue).getPrettyString();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                filterResults.values = allColorsBackup;
                filterResults.count = allColorsBackup.size();
            }
            else {
                String searchingFor = constraint.toString().trim().toLowerCase();
                ArrayList<MyColor> returnList = new ArrayList<>();
                for (MyColor color : allColorsBackup)
                    if (color.getPrettyString().toLowerCase().contains(searchingFor))
                        returnList.add(color);

                filterResults.values = returnList;
                filterResults.count = returnList.size();
            }

            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();        // Clearing the adapter
            if (results != null && results.count > 0)
                addAll((ArrayList<MyColor>)results.values);
            else
                addAll(allColorsBackup);
            notifyDataSetChanged();
        }
    };


    public List<MyColor> getItemsList() {
        return this.allColorsBackup;
    }
}
