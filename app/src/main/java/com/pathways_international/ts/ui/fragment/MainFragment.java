package com.pathways_international.ts.ui.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    @BindView(R.id.raila_text)
    TextView railaText;
    @BindView(R.id.uhuru_text)
    TextView uhuruText;
    @BindView(R.id.dida_text)
    TextView didaText;
    @BindView(R.id.nyagah_text)
    TextView nyagahText;
    @BindView(R.id.jirongo_text)
    TextView jirongoText;
    @BindView(R.id.aukot_text)
    TextView aukotText;
    @BindView(R.id.mwaura_text)
    TextView mwauraText;
    @BindView(R.id.kaluyu_text)
    TextView kaluyuText;


    @BindView(R.id.raila_total)
    EditText railaTotal;
    @BindView(R.id.aukot_total)
    EditText aukotTotal;

    @BindView(R.id.jirongo_total)
    EditText jirongoTotal;
    @BindView(R.id.nyagah_total)
    EditText nyagahTotal;

    @BindView(R.id.uhuru_total)
    EditText uhuruTotal;
    @BindView(R.id.dida_total)
    EditText didaTotal;

    @BindView(R.id.kaluyu_total)
    EditText kaluyuTotal;
    @BindView(R.id.mwaura_total)
    EditText mwauraTotal;

    @BindView(R.id.county_spinner)
    Spinner countySpinner;
    @BindView(R.id.constituency_spinner)
    Spinner constituencySpinner;
    @BindView(R.id.ward_spinner)
    Spinner wardSpinner;
    @BindView(R.id.poll_spinner)
    Spinner pollSpinner;
    @BindView(R.id.image_name)
    TextView imageName;

    String railaStr, uhuruStr, didaStr, nyagahStr, jirongoStr, aukotStr, mwauraStr, kaluyuStr;

    Bitmap bitmap;

    private int position = 0;
    private ImagePicker imagePicker = new ImagePicker();

    private SQLiteHandler sqLiteHandler;


    String countyStr, constStr, wardStr, pollStStr = "";

//    Spinner seatSpinner;

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

//        seatSpinner = (Spinner) getActivity().findViewById(R.id.seat_spinner);

        locationsPreference = new LocationSharedPrefs(getActivity());
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        if (pollStStr.isEmpty()) {
            railaTotal.setVisibility(View.GONE);
            railaText.setVisibility(View.GONE);

            aukotTotal.setVisibility(View.GONE);
            aukotText.setVisibility(View.GONE);

            uhuruTotal.setVisibility(View.GONE);
            uhuruText.setVisibility(View.GONE);

            didaTotal.setVisibility(View.GONE);
            didaText.setVisibility(View.GONE);

            nyagahTotal.setVisibility(View.GONE);
            nyagahText.setVisibility(View.GONE);

            jirongoTotal.setVisibility(View.GONE);
            jirongoText.setVisibility(View.GONE);

            mwauraTotal.setVisibility(View.GONE);
            mwauraText.setVisibility(View.GONE);

            kaluyuTotal.setVisibility(View.GONE);
            kaluyuText.setVisibility(View.GONE);

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
                railaTotal.setVisibility(View.VISIBLE);
                railaText.setVisibility(View.VISIBLE);

                uhuruTotal.setVisibility(View.VISIBLE);
                uhuruText.setVisibility(View.VISIBLE);

                didaTotal.setVisibility(View.VISIBLE);
                didaText.setVisibility(View.VISIBLE);

                nyagahTotal.setVisibility(View.VISIBLE);
                nyagahText.setVisibility(View.VISIBLE);

                jirongoTotal.setVisibility(View.VISIBLE);
                jirongoText.setVisibility(View.VISIBLE);

                aukotTotal.setVisibility(View.VISIBLE);
                aukotText.setVisibility(View.VISIBLE);

                mwauraTotal.setVisibility(View.VISIBLE);
                mwauraText.setVisibility(View.VISIBLE);

                kaluyuTotal.setVisibility(View.VISIBLE);
                kaluyuText.setVisibility(View.VISIBLE);

                imageName.setVisibility(View.VISIBLE);
                String iD = pollStationId.get(pollStationList.indexOf(pollSpinner.getSelectedItem().toString()));
                imageName.setText(iD);
                buttonSubmit.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        railaTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    railaStr = s.toString();
                    Log.d(LOG_TAG, railaStr);
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
        wardStr = wardStr.replace(" ", "%20");
        Log.d(LOG_TAG, wardStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.POLL_STATION + wardStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                if (pollStationList.size() > 0) {
                    pollStationList.clear();

                }
                if (pollStationId.size() > 0) {
                    pollStationId.clear();
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String ward = obj.getString("poll_station");
                            String id = obj.getString("id");
                            pollStationId.add(id);
                            pollStationList.add(ward);
                        }

                        Log.d(LOG_TAG, "" + pollStationId.size());
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
        railaStr = railaTotal.getText().toString();
        uhuruStr = uhuruTotal.getText().toString();
        didaStr = didaTotal.getText().toString();
        nyagahStr = nyagahTotal.getText().toString();
        jirongoStr = jirongoTotal.getText().toString();
        aukotStr = aukotTotal.getText().toString();
        mwauraStr = mwauraTotal.getText().toString();
        kaluyuStr = kaluyuTotal.getText().toString();

        if (!railaStr.isEmpty() && !uhuruStr.isEmpty() && !didaStr.isEmpty() && !nyagahStr.isEmpty() && !jirongoStr.isEmpty() && !aukotStr.isEmpty()
                && !mwauraStr.isEmpty() && !kaluyuStr.isEmpty()) {
            Log.d(LOG_TAG, countyStr + "||" + constStr + "||" + wardStr + "||" + pollStStr + "||" + railaStr + "||" + uhuruStr + "||" + didaStr);
//            String seat = String.valueOf(seatSpinner.getSelectedItem());
            String iD = pollStationId.get(pollStationList.indexOf(pollSpinner.getSelectedItem().toString()));

//          sqLiteHandler.addToTableOne(countyStr, constStr, wardStr, pollStStr);
//          sqLiteHandler.addToTableTwo(pollStId, railaStr, uhuruStr, seat);
            pushToTabeleOne(countyStr, constStr, wardStr, pollStStr);
            pushToTableTwo(iD, railaStr, uhuruStr, didaStr, nyagahStr, jirongoStr, aukotStr, mwauraStr, kaluyuStr);
            uploadImageClient(iD);
//            Log.d(LOG_TAG, " Seat Spinner val: " + seat);
//            Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
        } else {
            railaTotal.setError("Please fill in this field");
            uhuruTotal.setError("Please fill in this field");
            didaTotal.setError("Please fill in this field");
            nyagahTotal.setError("Please fill in this field");
            jirongoTotal.setError("Please fill in this field");
            aukotTotal.setError("Please fill in this field");
            mwauraTotal.setError("Please fill in this field");
            kaluyuTotal.setError("Please fill in this field");
        }

        edCounty.setText("");
        edConstituency.setText("");
        edWard.setText("");
        edPollStation.setText("");
        railaTotal.setText("");
        uhuruTotal.setText("");
        didaTotal.setText("");
        nyagahTotal.setText("");
        jirongoTotal.setText("");
        mwauraTotal.setText("");
        kaluyuTotal.setText("");
        aukotTotal.setText("");
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
        if (pollStStr.isEmpty()) {
            Toast.makeText(getContext(), "Select a poll station first", Toast.LENGTH_SHORT).show();
        } else {
            startChooser();
        }

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
        imagePicker.startCamera(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }

            @Override
            public void onCropImage(Uri imageUri) {
                super.onCropImage(imageUri);
                if (position == 1) {
//                    imageViewContainer.setImageURI(imageUri);
                    try {
                        Log.d(LOG_TAG, imageUri.getPath());
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        imageViewContainer.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    private static File getOutputMediaFile() {


        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                CropImage.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("LOOOOOD", "Oops! Failed create "
                        + CropImage.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void uploadImageClient(final String pollStId) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
//        progressBar.setVisibility(View.VISIBLE);
        pDialog.setMessage("Uploading...");
        showDialog();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, "http://inovatec.co.ke/ts/upload_image.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("Upload image", s);
                        pDialog.dismiss();
//                        progressBar.setVisibility(View.GONE);
//                        try {
//                            JSONObject jsonObject = new JSONObject(s);
//                            boolean error = jsonObject.getBoolean("error");
//                            if (!error) {
//                                String avatarUrl = jsonObject.getString("avatar");
//
//                                Log.d("CLIENT PROFILE PIC", avatarUrl);
//
//                                Log.d("Edit Profile Response", "Success");
////                                locationSharedPrefs.setAvatarUrl(avatarUrl);
////                                finish();
//
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//                progressBar.setVisibility(View.GONE);
//                Log.d("Upliad error", volleyError.getMessage());
//                finish();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = getStringImage(bitmap);

                Map<String, String> params = new HashMap<>();
                params.put("image", image);
                params.put("name", pollStId + "_" + timeStamp);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }

    private void pushToTabeleOne(final String countyStr, final String constStr, final String wardStr, final String pollStStr) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_TO_TABELE_ONE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Server Response", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Server Response", error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("county", countyStr);
                params.put("constituency", constStr);
                params.put("ward", wardStr);
                params.put("poll_station", pollStStr);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    private void pushToTableTwo(final String pollStId, final String railaStr, final String uhuruStr, final String didaStr,
                                final String nyagahStr, final String jirongoStr, final String aukotStr, final String mwauraStr, final String kaluyuStr) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_TO_TABLE_TWO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Server Response", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Server Response", error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("poll_station_id", pollStId);
                params.put("raila", railaStr);
                params.put("uhuru", uhuruStr);
                params.put("dida", didaStr);
                params.put("nyagah", nyagahStr);
                params.put("jirongo", jirongoStr);
                params.put("aukot", aukotStr);
                params.put("mwaura", mwauraStr);
                params.put("kaluyu", kaluyuStr);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }

    private File saveImageToExternal(Uri imageUri, String pollStId) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                CropImage.IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("LOOOOOD", "Oops! Failed create "
                        + CropImage.IMAGE_DIRECTORY_NAME + " directory");

            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + pollStId + "_" + timeStamp + ".jpg");

        final int chunkSize = 1024;  // We'll read in one kB at a time
        byte[] imageData = new byte[chunkSize];
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getContext().getContentResolver().openInputStream(imageUri);
            out = new FileOutputStream(mediaFile);  // I'm assuming you already have the File object for where you're writing to

            int bytesRead;
            while ((bytesRead = in.read(imageData)) > 0) {
                out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
            }

        } catch (Exception ex) {
            Log.e("Something went wrong.", ex.toString());
        } finally {
            try {
                assert in != null;
                in.close();
                assert out != null;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return mediaFile;
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
        pDialog.setCancelable(true);
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
