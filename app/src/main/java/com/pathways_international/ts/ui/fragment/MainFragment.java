package com.pathways_international.ts.ui.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.pathways_international.ts.ui.helper.SessionManager;
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

    @BindView(R.id.raila_total)
    EditText railaTotal;


    @BindView(R.id.uhuru_total)
    EditText uhuruTotal;
    @BindView(R.id.spoilt_votes)
    EditText spoiltVotes;
    @BindView(R.id.total_votes)
    EditText totalVotes;

    @BindView(R.id.county_spinner)
    Spinner countySpinner;
    @BindView(R.id.constituency_spinner)
    Spinner constituencySpinner;
    @BindView(R.id.ward_spinner)
    Spinner wardSpinner;
    @BindView(R.id.poll_spinner)
    Spinner pollSpinner;
    @BindView(R.id.stream_spinner)
    Spinner streamSpinner;
    @BindView(R.id.image_name)
    TextView imageName;
    @BindView(R.id.page_title)
    TextView pageTitle;

    @BindView(R.id.candidates_view)
    LinearLayout candidatesView;

    String railaStr, uhuruStr, spoiltVotesStr, total;

    Bitmap bitmap;

    private int position = 0;
    private ImagePicker imagePicker = new ImagePicker();

    private SQLiteHandler sqLiteHandler;
    private SessionManager sessionManager;


    String countyStr, streamStr = "";
    String pollStStr = "";

//    Spinner seatSpinner;

    String pollStId;

    ArrayAdapter<String> countyAdapter, constAdapter, wardAdapter, pollAdapter, pollStreamAdapter;
    LocationSharedPrefs locationsPreference;
    List<LocationModel> countyModelList = new ArrayList<>();
    List<LocationModel> locationModelList = new ArrayList<>();
    ArrayList<String> cosntituencies = new ArrayList<>();
    ArrayList<String> wardsList = new ArrayList<>();
    ArrayList<String> pollStationList = new ArrayList<>();
    ArrayList<String> pollStationStreamList = new ArrayList<>();
    ArrayList<String> pollStationId = new ArrayList<>();
    Boolean requestedStarted = false;

    private ProgressDialog pDialog;

    Activity parentActivity;

    boolean isInitialDisplay = true;

    String constName, constCode, wardName, wardCode;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        locationsPreference = new LocationSharedPrefs(getActivity());
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        if (bitmap == null) {
            candidatesView.setVisibility(View.GONE);
            buttonSubmit.setEnabled(false);
        }

        sqLiteHandler = new SQLiteHandler(getContext());
        sessionManager = new SessionManager(getContext());

        if (sessionManager.isLoggedIn()) {
            HashMap<String, String> user = sqLiteHandler.getUserDetails();
            constCode = user.get("constituency_code");
            constName = user.get("constituency_name");
            wardName = user.get("ward_name");
            wardCode = user.get("ward_code");

            Log.d(LOG_TAG, constName + "||" + wardName);

            pageTitle.setText(getString(R.string.ward) + ": " + wardName);


            if (pollStationList != null && pollStationList.isEmpty() && pollStationList.size() == 0) {
                loadPollStationsRemote(wardName);
            }


        }

        pollSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pollStStr = parent.getItemAtPosition(position).toString();
                loadPollStreamsRemote(pollStStr);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        streamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                streamStr = parent.getItemAtPosition(position).toString();
                String iD = pollStationId.get(pollStationStreamList.indexOf(streamSpinner.getSelectedItem().toString()));

                imageName.setText(iD);
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
                if (railaTotal.getText().toString().isEmpty()) {
                    railaTotal.setText("0");
                }

                if (!uhuruTotal.getText().toString().isEmpty() && !spoiltVotes.getText().toString().isEmpty()) {
                    int raila = Integer.parseInt(railaTotal.getText().toString());
                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
                    if (!s.toString().isEmpty()) {
                        int spoilt = Integer.parseInt(spoiltVotes.getText().toString());
                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
                    }


                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
//                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        uhuruTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (uhuruTotal.getText().toString().isEmpty()) {
                    uhuruTotal.setText("0");

                }

                if (!railaTotal.getText().toString().isEmpty() && !spoiltVotes.getText().toString().isEmpty()) {
                    int raila = Integer.parseInt(railaTotal.getText().toString());
                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
                    if (!s.toString().isEmpty()) {
                        int spoilt = Integer.parseInt(spoiltVotes.getText().toString());
                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
                    }


                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
//                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        spoiltVotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (railaTotal.getText().toString().isEmpty()) {
                    railaTotal.setText("0");
                }
                if (uhuruTotal.getText().toString().isEmpty()) {
                    uhuruTotal.setText("0");
                }
                if (spoiltVotes.getText().toString().isEmpty()) {
                    spoiltVotes.setText("0");
                }
                if (!railaTotal.getText().toString().isEmpty() && !uhuruTotal.getText().toString().isEmpty()) {
                    int raila = Integer.parseInt(railaTotal.getText().toString());
                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
                    if (!s.toString().isEmpty()) {
                        int spoilt = Integer.parseInt(s.toString());
                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
                    }


                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
//                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
                    }
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
                            countyStr = obj.getString("county");
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
        pDialog.show();
        wardStr = wardStr.replace(" ", "%20");
        Log.d(LOG_TAG, wardStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.POLL_STATION + wardStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                if (pollStationList.size() > 0) {
                    pollStationList.clear();

                }
//                if (pollStationId.size() > 0) {
//                    pollStationId.clear();
//                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("location");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String ward = obj.getString("poll_station");
                            String id = obj.getString("id");
                            if (!pollStationList.contains(ward)) {
//                                pollStationId.add(id);
                                pollStationList.add(ward);
                            }
                            countyStr = obj.getString("county");

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

    private void loadPollStreamsRemote(String pollStStr) {
        pDialog.setMessage("Loading streams stations");
        pDialog.dismiss();
        pollStStr = pollStStr.replace(" ", "%20");
        Log.d(LOG_TAG, pollStStr);
        StringRequest request = new StringRequest(Request.Method.GET, Urls.POLL_STREAM + pollStStr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                Log.d("Streams", response);
                if (pollStationStreamList.size() > 0) {
                    pollStationStreamList.clear();

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
                            String ward = obj.getString("ward");
                            String stream = obj.getString("stream");
                            String id = obj.getString("id");

                            if (ward.equals(wardName)) {
                                pollStationStreamList.add(stream);

                                pollStationId.add(id);
                                Log.d(LOG_TAG, "Id added");
                            }


                        }

                        Log.d(LOG_TAG, "" + pollStationId.size());
                        pollStreamAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, pollStationStreamList);
                        streamSpinner.setAdapter(pollStreamAdapter);
                        streamSpinner.setVisibility(View.VISIBLE);
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
        spoiltVotesStr = spoiltVotes.getText().toString();
        total = totalVotes.getText().toString();


        if (!railaStr.isEmpty() && !uhuruStr.isEmpty() && !spoiltVotesStr.isEmpty() && !total.isEmpty()) {
            buttonSubmit.setEnabled(false);
            Log.d(LOG_TAG, countyStr + "||" + constName + "||" + wardName + "||" + pollStStr + "||" + railaStr + "||" + uhuruStr + "||" + spoiltVotesStr);

            String iD = pollStationId.get(pollStationStreamList.indexOf(streamSpinner.getSelectedItem().toString()));
            countyStr = countyStr.replace("'", "\\'");
            wardName = wardName.replace("'", "\\'");
            constName = constName.replace("'", "\\'");
            pollStStr = pollStStr.replace("'", "\\'");

            pushToTabeleOne(countyStr, constName, wardName, pollStStr, streamStr);
            pushToTableTwo(iD, railaStr, uhuruStr, spoiltVotesStr, total);
            uploadImageClient(iD);
//                railaTotal.setText("");
//                uhuruTotal.setText("");
//
//                spoiltVotes.setText("");
            candidatesView.setVisibility(View.GONE);
            imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));

        } else {
            railaTotal.setError("Please fill in this field");
            uhuruTotal.setError("Please fill in this field");
            spoiltVotes.setError("Please fill in this field");
        }

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
        imagePicker.startChooser(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }

            @Override
            public void onCropImage(Uri imageUri) {
                super.onCropImage(imageUri);
                if (position == 1) {
                    try {
                        Log.d(LOG_TAG, imageUri.getPath());
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        imageViewContainer.setImageBitmap(bitmap);
                        if (bitmap != null) {
                            if (candidatesView.getVisibility() == View.GONE) {
                                candidatesView.setVisibility(View.VISIBLE);
                            }
                            buttonSubmit.setEnabled(true);

                        }

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
        pDialog.setMessage("Uploading...");
        showDialog();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        buttonSubmit.setEnabled(true);
                        Log.d("Upload image", s);
                        Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
                        // Clear the spinner
                        streamSpinner.setAdapter(null);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideDialog();
                buttonSubmit.setEnabled(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = getStringImage(bitmap);

                Map<String, String> params = new HashMap<>();
                params.put("image", image);
                params.put("name", pollStId + "_" + timeStamp);
                params.put("poll_station_id", pollStId);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }

    private void pushToTabeleOne(final String countyStr, final String constStr, final String wardStr, final String pollStStr,
                                 final String streamStr) {
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
                params.put("stream", streamStr);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    private void pushToTableTwo(final String pollStId, final String railaStr, final String uhuruStr, final String spoiltVotesStr, final String total) {
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
                params.put("spoilt_votes", spoiltVotesStr);
                params.put("total", total);
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
