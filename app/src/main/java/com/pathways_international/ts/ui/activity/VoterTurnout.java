package com.pathways_international.ts.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.LocationSharedPrefs;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.model.LocationModel;
import com.pathways_international.ts.ui.utils.CropImage;
import com.pathways_international.ts.ui.utils.CropImageView;
import com.pathways_international.ts.ui.utils.ImagePicker;
import com.pathways_international.ts.ui.utils.Urls;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.view.CameraView;

public class VoterTurnout extends AppCompatActivity implements IPickResult {

    private static final String LOG_TAG = VoterTurnout.class.getSimpleName();

    @BindView(R.id.submit_button)
    Button buttonSubmit;
    @BindView(R.id.imageview_container)
    ImageView imageViewContainer;

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

    @BindView(R.id.voter_turnout_total)
    EditText voterTurnoutTotalEditText;

    String totalString;

    Bitmap bitmap;

    private int position = 0;
    private ImagePicker imagePicker = new ImagePicker();

    private SQLiteHandler sqLiteHandler;
    private SessionManager sessionManager;


    String countyStr, streamStr = "";
    String pollStStr = "";
    String deviceTime;

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

    String constName, constCode, wardName, wardCode;


    boolean isInitialDisplay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter_turnout);
        ButterKnife.bind(this);

        deviceTime = String.valueOf(new Date());

        locationsPreference = new LocationSharedPrefs(getApplicationContext());
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (bitmap == null) {
            buttonSubmit.setEnabled(false);
        }

        ActionBar actionBar = getSupportActionBar();

        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());


        if (sessionManager.isLoggedIn()) {
            HashMap<String, String> user = sqLiteHandler.getUserDetails();
            constCode = user.get("constituency_code");
            constName = user.get("constituency_name");
            wardName = user.get("ward_name");
            wardCode = user.get("ward_code");

            Log.d(LOG_TAG, constName);
            actionBar.setTitle("VOTER TURNOUT");

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


        imagePicker.setCropImage(true);
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        position = 1;
        if (pollStStr.isEmpty()) {
            Toast.makeText(VoterTurnout.this, "Select a poll station first", Toast.LENGTH_SHORT).show();
        } else {
            startChooser();
            // To remove image cropping
//            PickImageDialog.build(new PickSetup()).show(this);
        }

    }

    @OnClick(R.id.submit_button)
    void submitButton() {
        totalString = voterTurnoutTotalEditText.getText().toString();

        if (!totalString.isEmpty()) {
            buttonSubmit.setEnabled(false);
            Log.d(LOG_TAG, constName + "||" + wardName + "||" + pollStStr + "||" + totalString);

            String iD = pollStationId.get(pollStationStreamList.indexOf(streamSpinner.getSelectedItem().toString()));

            countyStr = countyStr.replace("'", "\\'");
            wardName = wardName.replace("'", "\\'");
            constName = constName.replace("'", "\\'");
            pollStStr = pollStStr.replace("'", "\\'");

//          sqLiteHandler.addToTableOne(countyStr, constStr, wardStr, pollStStr);
//          sqLiteHandler.addToTableTwo(pollStId, railaStr, uhuruStr, seat);
            recordTurnout(iD, totalString, String.valueOf(new Date()));
//            Toast.makeText(getApplicationContext(), constName + "||" + wardStr + "||" + pollStStr + "||" + totalString, Toast.LENGTH_LONG).show();
            uploadImageClient(iD, totalString);
        } else {
            voterTurnoutTotalEditText.setError("Please fill in this field");

        }

        voterTurnoutTotalEditText.setText("");
        imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));


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
//                    imageViewContainer.setImageURI(imageUri);
                    try {
                        Log.d(LOG_TAG, imageUri.getPath());
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageViewContainer.setImageBitmap(bitmap);
                        if (bitmap != null) {
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

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    private void uploadImageClient(final String pollStId, final String totalString) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        pDialog.setMessage("Uploading...");
        showDialog();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_TURNOUT_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        buttonSubmit.setEnabled(true);
                        Log.d("Upload image", s);
                        Toast.makeText(VoterTurnout.this, "Data saved", Toast.LENGTH_SHORT).show();
                        // Clear the spinners
//                        wardSpinner.setAdapter(null);
//                        constituencySpinner.setAdapter(null);
//                        pollSpinner.setAdapter(null);
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
                params.put("name", pollStId + "_" + timeStamp + "_" + totalString);
                params.put("poll_station_id", pollStId);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }

    private void recordTurnout(final String pollStId, final String totalString, final String timeOnDevice) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_TO_VOTER_TURNOUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Server response", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Server response", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("poll_station_id", pollStId);
                params.put("votes", totalString);
                params.put("time_on_device", timeOnDevice);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
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
                        constAdapter = new ArrayAdapter<>(VoterTurnout.this, android.R.layout.simple_spinner_dropdown_item, cosntituencies);
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

                        wardAdapter = new ArrayAdapter<>(VoterTurnout.this, android.R.layout.simple_spinner_dropdown_item, wardsList);
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
                        pollAdapter = new ArrayAdapter<>(VoterTurnout.this, android.R.layout.simple_spinner_dropdown_item, pollStationList);
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
        StringRequest request = new StringRequest(Request.Method.GET, Urls.POLL_STREAM_TURNOUT + pollStStr, new Response.Listener<String>() {
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
                        pollStreamAdapter = new ArrayAdapter<>(VoterTurnout.this, android.R.layout.simple_spinner_dropdown_item, pollStationStreamList);
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


    private void pushToVoterTurnout(final String pollStId, final String totalVotes, final String timeOnDevice) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_TO_VOTER_TURNOUT, new Response.Listener<String>() {
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
                params.put("votes", totalVotes);
                params.put("time_on_device", timeOnDevice);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
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

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            imageViewContainer.setImageBitmap(pickResult.getBitmap());

            bitmap = pickResult.getBitmap();

            buttonSubmit.setEnabled(true);
        }
    }

}
