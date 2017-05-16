package com.pathways_international.ts.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.model.LocationModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android-dev on 5/16/17.
 */

public class PollingStationAutoCompleteAdapter extends ArrayAdapter<LocationModel> {

    String variant;
    private List<LocationModel> mLocationModels;
    private LayoutInflater layoutInflater;
    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((LocationModel) resultValue).getLocation_label();
        }

        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<LocationModel> suggestions = new ArrayList<LocationModel>();
                for (LocationModel LocationModel : mLocationModels) {
                    Log.d("filtering_", String.valueOf(constraint));
                    // Note: change the "contains" to "startsWith" if you only want starting matches
                    if (LocationModel.getLocation_label().startsWith(constraint.toString().toUpperCase())) {
                        suggestions.add(LocationModel);
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((ArrayList<LocationModel>) results.values);
            } else {
                // no filter, add entire original list back in
                addAll(mLocationModels);
            }
            notifyDataSetChanged();
        }
    };


    public PollingStationAutoCompleteAdapter(Context context, int textViewResourceId, List<LocationModel> locationModels, String variant) {
        super(context, textViewResourceId, locationModels);
        // copy all the LocationModels into a master list
        mLocationModels = new ArrayList<>(locationModels.size());
        mLocationModels.addAll(locationModels);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.variant = variant;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.autocomplete_row_item, null);
        }

        LocationModel locationModel = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.textViewItem);
        if (variant.equals("county")) {
            name.setText(locationModel.getLocation_label());
        } else {
            name.setText(locationModel.getLocation_label());
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
