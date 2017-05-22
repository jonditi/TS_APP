package com.pathways_international.ts.ui.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.pathways_international.ts.ui.utils.Urls;

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
    @BindView(R.id.county_spinner)
    Spinner countySpinner;
    @BindView(R.id.constituency_spinner)
    Spinner constituencySpinner;
    @BindView(R.id.ward_spinner)
    Spinner wardSpinner;
    @BindView(R.id.poll_spinner)
    Spinner pollSpinner;

    String num1, num2;

    private int position = 0;
    private ImagePicker imagePicker = new ImagePicker();

    private SQLiteHandler sqLiteHandler;


    String countyStr, constStr, wardStr, pollStStr = "";

    Spinner seatSpinner;

    String pollStId;

    ArrayAdapter<String> countyAdapter, constAdapter, wardAdapter, pollAdapter;
    LocationSharedPrefs locationsPreference;
    List<LocationModel> countyModelList = new ArrayList<>();
    List<LocationModel> locationModelList = new ArrayList<>();
    ArrayList<String> cosntituencies = new ArrayList<>();
    ArrayList<String> wardsList = new ArrayList<>();
    ArrayList<String> pollStationList = new ArrayList<>();
    ArrayList<String> pollStationId = new ArrayList<>();
    Boolean requestedStarted = false;

    private ProgressDialog pDialog;

    Activity parentActivity;

    boolean isInitialDisplay = true;


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

        if (pollStStr.isEmpty()) {
            numInput1.setVisibility(View.GONE);
            numInput2.setVisibility(View.GONE);
            buttonSubmit.setEnabled(false);
        }

        sqLiteHandler = new SQLiteHandler(getContext());

        countySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialDisplay) {
                    isInitialDisplay = false;
                } else {
                    countyStr = parent.getItemAtPosition(position).toString();
                    if (countyStr.contains(" ")) {
                        Log.d(LOG_TAG, "Spacesssssssssssssssssssssssssss");
                    }
                    loadConstituencies(countyStr);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        constituencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                constStr = parent.getItemAtPosition(position).toString();
                loadWardsRemote(constStr);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wardStr = parent.getItemAtPosition(position).toString();
                Log.d(LOG_TAG, wardStr);
                loadPollStationsRemote(wardStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pollSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pollStStr = parent.getItemAtPosition(position).toString();
                numInput1.setVisibility(View.VISIBLE);
                numInput2.setVisibility(View.VISIBLE);
                buttonSubmit.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        numInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    num1 = s.toString();
                    Log.d(LOG_TAG, num1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        imagePicker.setCropImage(true);
        return rootView;
    }

    private void loadConstituencies(String countyStr) {

        pDialog.setMessage("Loading constituencies");
        pDialog.show();
        pDialog.setCancelable(true);
        countyStr = countyStr.replace(" ", "%20");
        Log.d(LOG_TAG, countyStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.CONSTITUENCIES + countyStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                if (cosntituencies.size() > 0) {
                    cosntituencies.clear();
                }
                Log.d(LOG_TAG, "Constituencies: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String constituency = obj.getString("constituency");
                            cosntituencies.add(constituency);

                        }
                        Log.d(LOG_TAG, "" + cosntituencies.size());
                        constAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, cosntituencies);
                        constituencySpinner.setAdapter(constAdapter);

                        if (constituencySpinner.getVisibility() == View.INVISIBLE) {
                            constituencySpinner.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void loadWardsRemote(String constStr) {
        pDialog.setMessage("Loading Wards");
        pDialog.show();
        constStr = constStr.replace(" ", "%20");
        Log.d(LOG_TAG, constStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.WARDS + constStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                if (wardsList.size() > 0) {
                    wardsList.clear();
                }
                Log.d(LOG_TAG, "wards: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String ward = obj.getString("ward");
                            wardsList.add(ward);
                        }

                        wardAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, wardsList);
                        wardSpinner.setAdapter(wardAdapter);
                        wardSpinner.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void loadPollStationsRemote(String wardStr) {
        pDialog.setMessage("Loading poll stations");
        pDialog.dismiss();
        wardStr = wardStr.replace(" ", wardStr);
        Log.d(LOG_TAG, wardStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.POLL_STATION + wardStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                if (pollStationList.size() > 0) {
                    pollStationList.clear();
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String ward = obj.getString("poll_station");
//                            String id = obj.getString("id");
//                            pollStationId.add(id);
                            pollStationList.add(ward);
                        }

                        pollAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, pollStationList);
                        pollSpinner.setAdapter(pollAdapter);
                        pollSpinner.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }


    @OnClick(R.id.submit_button)
    void submitButton() {
        num1 = numInput1.getText().toString();
        num2 = numInput2.getText().toString();
        if (!num1.isEmpty() && !num2.isEmpty()) {
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
        } else {
            numInput1.setError("Please fill in this field");
            numInput2.setError("Please fill in this field");
        }

        edCounty.setText("");
        edConstituency.setText("");
        edWard.setText("");
        edPollStation.setText("");
        numInput1.setText("");
        numInput2.setText("");
        imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));


    }

    private void loadCounties() {
        List<String> countyList = sqLiteHandler.getCounties();
        Log.d(LOG_TAG, "Size sssssssss" + countyList.size());
        ArrayAdapter<String> countyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                countyList);

        countySpinner.setAdapter(countyAdapter);

    }

    private void loadWards(String constituency) {
        List<String> countyList = sqLiteHandler.getWards(constituency);
        Log.d(LOG_TAG, "Size sssssssss" + countyList.size());
        ArrayAdapter<String> countyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                countyList);

        wardSpinner.setAdapter(countyAdapter);
    }

    private void loadPollStations(String ward) {
        List<String> countyList = sqLiteHandler.getPollStations(ward);
        Log.d(LOG_TAG, "Size sssssssss" + countyList.size());
        ArrayAdapter<String> countyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                countyList);

        pollSpinner.setAdapter(countyAdapter);
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


    private void sampleQuery() {
        pDialog.setMessage("Fetching places data...");
        pDialog.show();
        Log.d(LOG_TAG, "Exceddd");
        StringRequest request = new StringRequest(Request.Method.GET, "http://inovatec.co.ke/redwood/table10.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d(LOG_TAG, response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("location");
                            Log.d(LOG_TAG, "" + jsonArray.length());
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    String id = obj.getString("id");
                                    String county = obj.getString("county");
                                    String constituency = obj.getString("constituency");
                                    String ward = obj.getString("ward");
                                    String pollStation = obj.getString("poll_st");

                                    if (sqLiteHandler.getRowCount() < jsonArray.length()) {
                                        sqLiteHandler.addToLoc(county, id, constituency, ward, pollStation);
                                    }
                                }
                            }
                            loadCounties();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(request);
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
                                    android.R.layout.simple_dropdown_item_1line, locationModelList, "location");
//                            edConstituency.setAdapter(regionAutoCompleteAdapter);
                            constituencySpinner.setAdapter(regionAutoCompleteAdapter);
                            constituencySpinner.setVisibility(View.VISIBLE);


//                            edConstituency.setVisibility(View.VISIBLE);
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
        Log.d(LOG_TAG, "" + cosntituencies.size());
        constAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, cosntituencies);
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
