package com.pathways_international.ts.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.pathways_international.ts.R;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    @BindView(R.id.county)
    AutoCompleteTextView edCounty;
    @BindView(R.id.constituency)
    AutoCompleteTextView edConstituency;
    @BindView(R.id.ward)
    AutoCompleteTextView edWard;
    @BindView(R.id.polling_station)
    AutoCompleteTextView edPollStation;
    @BindView(R.id.img_1)
    ImageView imageView1;
    private int position = 0;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

}
