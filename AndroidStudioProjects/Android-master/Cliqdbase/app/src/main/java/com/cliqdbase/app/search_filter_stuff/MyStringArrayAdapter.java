package com.cliqdbase.app.search_filter_stuff;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by Yuval on 07/08/2015.
 *
 * @author Yuval Siev
 */
public class MyStringArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> items;

    public MyStringArrayAdapter(Context context, int layoutResourceId, ArrayList<String> items) {
        super(context, layoutResourceId, items);

        this.items = new ArrayList<>(items);
    }

    public boolean containing(String item) {
        item = item.trim();
        for (String s : items) {
            s = s.trim();
            if (item.equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    /*public boolean containing(String[] strings) {
        boolean exists;
        for (String s : strings) {
            exists = false;
            s = s.trim();
            for (String item : items) {
                item = item.trim();
                if (s.equalsIgnoreCase(item))
                    exists = true;
            }
            if (!exists)
                return false;
        }
        return true;
    }*/

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                filterResults.values = items;
                filterResults.count = items.size();
            }
            else {
                String searchingFor = constraint.toString().trim().toLowerCase();
                ArrayList<String> returnList = new ArrayList<>();
                for (String s : items)
                    if (s.toLowerCase().contains(searchingFor))
                        returnList.add(s);

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
                addAll((ArrayList<String>)results.values);
            else
                addAll(items);
            notifyDataSetChanged();
        }
    };
}
