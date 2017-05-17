package com.pathways_international.ts.ui.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.adapter.ConstituencyAutoCompleteAdapter;
import com.pathways_international.ts.ui.adapter.CountyAutoCompleteAdapter;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.LocationSharedPrefs;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.model.LocationModel;
import com.pathways_international.ts.ui.utils.CropImage;
import com.pathways_international.ts.ui.utils.CropImageView;
import com.pathways_international.ts.ui.utils.ImagePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.submit_button)
    Button buttonSubmit;
    @BindView(R.id.imageview_container)
    ImageView imageViewContainer;
    @BindView(R.id.num_input1)
    EditText numInput1;
    @BindView(R.id.num_input2)
    EditText numInput2;

    private int position = 0;
    private ImagePicker imagePicker = new ImagePicker();

    private SQLiteHandler sqLiteHandler;

    String[] counties = {"County1", "County2", "County3", "County4"};
    String[] constituenciesForCounty1 = {"Const1", "Const2"};
    String[] wards = {"Ward1", "Ward2"};
    String[] pollStation = {"PollSt1", "PollSt2"};

    String countyStr, constStr, wardStr, pollStStr;

    Spinner seatSpinner;

    String pollStId;

    ArrayAdapter countyAdapter, constAdapter, wardAdapter, pollAdapter;
    LocationSharedPrefs locationsPreference;
    List<LocationModel> countyModelList = new ArrayList<>();
    List<LocationModel> locationModelList = new ArrayList<>();
    Boolean requestedStarted = false;

    private ProgressDialog pDialog;

    Activity parentActivity;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        seatSpinner = (Spinner) getActivity().findViewById(R.id.seat_spinner);

        locationsPreference = new LocationSharedPrefs(getActivity());
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        sqLiteHandler = new SQLiteHandler(getContext());
        initViews();

//        // County auto complete
//        countyAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_expandable_list_item_1, counties);
//        edCounty.setThreshold(1);
//        edCounty.setAdapter(countyAdapter);
        edCounty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                countyStr = edCounty.getText().toString();

            }
        });
//
//        // Constituency auto complete
//        constAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_expandable_list_item_1, constituenciesForCounty1);
//        edConstituency.setThreshold(1);
//        edConstituency.setAdapter(constAdapter);
        edConstituency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                constStr = edConstituency.getText().toString();
            }
        });
//
        // Ward auto complete
        wardAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_expandable_list_item_1, wards);
        edWard.setThreshold(1);
        edWard.setAdapter(wardAdapter);
        edWard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                wardStr = edWard.getText().toString();
            }
        });

        // Poll Station auto complete
        pollAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_expandable_list_item_1, pollStation);
        edPollStation.setThreshold(1);
        edPollStation.setAdapter(pollAdapter);
        edPollStation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollStStr = edPollStation.getText().toString();
            }
        });






        imagePicker.setCropImage(true);
        return rootView;
    }

    private void initViews() {
        if (locationsPreference.getCountyList().isEmpty()) {
            locationsRequestVolley("locations/counties", "county");
        } else {
            parseLocationData(locationsPreference.getCountyList(), "county");
            Log.d("ParseLoc data: ", "is called");
            Log.d("Parse Estates: ", locationsPreference.getCountyList());
        }
        // hide location views
        edConstituency.setVisibility(View.INVISIBLE);
        edWard.setVisibility(View.INVISIBLE);
        edPollStation.setVisibility(View.INVISIBLE);
//        tvTitle.setVisibility(View.INVISIBLE);

        edCounty.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("county_name", s.toString().toUpperCase());


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                for (LocationModel county : countyModelList) {
                    if (county.getCounty_label().equals(s.toString().toUpperCase())) {

                        locationsRequestVolley("locations/county/" + county.getCounty_id(), "places");
                        Log.d("dialog_shon", requestedStarted.toString());
                    } else {
//                        doneBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        edConstituency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                doneBtn.setVisibility(View.VISIBLE);
                edWard.setVisibility(View.VISIBLE);

            }
        });

        edWard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                edPollStation.setVisibility(View.VISIBLE);

            }
        });
    }


    @OnClick(R.id.submit_button)
    void submitButton() {
        String num1 = numInput1.getText().toString();
        String num2 = numInput2.getText().toString();
        Log.d(LOG_TAG, countyStr + "||" + constStr + "||" + wardStr + "||" + pollStStr + "||" + num1 + "||" + num2);
        if (seatSpinner != null) {
            String seat = String.valueOf(seatSpinner.getSelectedItem());
            if (pollStStr.equals("PollSt1")) {
                pollStId = "1";
            } else if (pollStStr.equals("PollSt2")) {
                pollStId = "2";
            }
            sqLiteHandler.addToTableOne(countyStr, constStr, wardStr, pollStStr);
            sqLiteHandler.addToTableTwo(pollStId, num1, num2, seat);
            Log.d(LOG_TAG, " Seat Spinner val: " + seat);
            Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(LOG_TAG, "Seat spinner is null");
        }


        edCounty.setText("");
        edConstituency.setText("");
        edWard.setText("");
        edPollStation.setText("");
        numInput1.setText("");
        numInput2.setText("");
        imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));


    }

    private void parseLocationData(String list, String variant) {
        try {
            JSONObject dataObject = new JSONObject(list);
            JSONArray locationsArray = dataObject.getJSONArray("data");
            for (int i = 0; i < locationsArray.length(); i++) {
                JSONObject locationObject = locationsArray.getJSONObject(i);
                LocationModel lm = new LocationModel();
                if (variant.equals("county")) {
                    lm.setCounty_id(locationObject.getString("county_id"));
                    lm.setCounty_label(locationObject.getString("county_label"));
                } else {
                    lm.setLocation_id(locationObject.getString("location_id"));
                    lm.setLocation_label(locationObject.getString("location_label"));
                }
                countyModelList.add(lm);
                Log.d("location-listedssssss", locationObject.getString("county_label"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("location-list", e.toString());
        }

        CountyAutoCompleteAdapter regionAutoCompleteAdapter = new CountyAutoCompleteAdapter(parentActivity,
                R.layout.autocomplete_row_item, countyModelList, variant);
        edCounty.setAdapter(regionAutoCompleteAdapter);
    }

    public void locationsRequestVolley(final String url, final String variant) {
        pDialog.setMessage("Loading places");
        pDialog.show();
        StringRequest request = new StringRequest(Request.Method.GET, getResources().getString(R.string.base_url) + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        Log.d(LOG_TAG, s);
                        Log.d("Bladdy", " hell");
                        requestedStarted = false;
                        if (variant.equals("places")) {
                            if (locationsPreference.getConstituencyList().isEmpty()) {
                                locationsPreference.setConstituencyList(s);
                            }

                            try {
                                JSONObject dataObject = new JSONObject(s);
                                JSONArray locationsArray = dataObject.getJSONArray("data");
                                for (int i = 0; i < locationsArray.length(); i++) {
                                    JSONObject locationObject = locationsArray.getJSONObject(i);
                                    LocationModel lm = new LocationModel();

                                    lm.setLocation_id(locationObject.getString("estate_id"));
                                    lm.setLocation_label(locationObject.getString("estate_label"));
                                    locationModelList.add(lm);


                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("locatiion_req", e.toString());
                            }
                            ConstituencyAutoCompleteAdapter regionAutoCompleteAdapter = new ConstituencyAutoCompleteAdapter(parentActivity,
                                    R.layout.autocomplete_row_item, locationModelList, "location");
                            edConstituency.setAdapter(regionAutoCompleteAdapter);

                            edConstituency.setVisibility(View.VISIBLE);
//                            tvTitle.setVisibility(View.VISIBLE);

                        } else {
                            locationsPreference.setCountyList(s);
                            parseLocationData(s, "county");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("locatiion_req-e", volleyError.getMessage());
                pDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        position = 1;
        startChooser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void startChooser() {
        imagePicker.startChooser(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }

            @Override
            public void onCropImage(Uri imageUri) {
                super.onCropImage(imageUri);
                if (position == 1) {
                    imageViewContainer.setImageURI(imageUri);
                }
            }

            public void cropConfig(CropImage.ActivityBuilder builder) {
                builder
                        .setMultiTouchEnabled(false)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(640, 640)
                        .setAspectRatio(1, 1);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated: hit");
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume: hit");
        super.onResume();
    }


    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause: hit");
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "onDestroyView: hit");
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy: hit");
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }



}
