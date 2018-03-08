package com.pathways_international.ts.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.pathways_international.ts.ui.fragment.MainFragment;
import com.pathways_international.ts.ui.fragment.SettingsActivity;
import com.pathways_international.ts.ui.helper.LocationSharedPrefs;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.model.LocationModel;
import com.pathways_international.ts.ui.utils.ImageManager;
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
import java.io.InputStream;
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

public class MainActivity extends AppCompatActivity implements IPickResult {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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
    TextInputEditText railaTotal;


    @BindView(R.id.uhuru_total)
    TextInputEditText uhuruTotal;
    @BindView(R.id.spoilt_votes)
    TextInputEditText spoiltVotes;
    @BindView(R.id.total_votes)
    TextInputEditText totalVotes;
    @BindView(R.id.rejected_ballot)
    TextInputEditText rejectedBallot;
    @BindView(R.id.objected_rejected)
    TextInputEditText objectedRejected;
    @BindView(R.id.disputed_votes)
    TextInputEditText disputed;

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
    String registerdVoters;
    String rejectedBallotPapersStr;
    String rejectedObjectedStr;
    String disputedVotes;
    String validVotesStr;

    Bitmap bitmap;
    private Uri imageUri;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        locationsPreference = new LocationSharedPrefs(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (bitmap == null) {
            candidatesView.setVisibility(View.GONE);
            buttonSubmit.setEnabled(false);
        }

        sessionManager = new SessionManager(getApplicationContext());

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
//                if (railaTotal.getText().toString().isEmpty()) {
//                    railaTotal.setText("0");
//                }
//
//                if (!uhuruTotal.getText().toString().isEmpty() && !spoiltVotes.getText().toString().isEmpty()) {
//                    int raila = Integer.parseInt(railaTotal.getText().toString());
//                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
////                    if (!s.toString().isEmpty()) {
////                        int spoilt = Integer.parseInt(spoiltVotes.getText().toString());
////                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
////                    }
//
//
//                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
////                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
//                    }
//                }

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
//                if (uhuruTotal.getText().toString().isEmpty()) {
//                    uhuruTotal.setText("0");
//
//                }
//
//                if (!railaTotal.getText().toString().isEmpty() && !spoiltVotes.getText().toString().isEmpty()) {
//                    int raila = Integer.parseInt(railaTotal.getText().toString());
//                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
////                    if (!s.toString().isEmpty()) {
////                        int spoilt = Integer.parseInt(spoiltVotes.getText().toString());
////                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
////                    }
////
////
////                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
//////                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
////                    }
//                }
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
//                if (railaTotal.getText().toString().isEmpty()) {
//                    railaTotal.setText("0");
//                }
//                if (uhuruTotal.getText().toString().isEmpty()) {
//                    uhuruTotal.setText("0");
//                }
//                if (spoiltVotes.getText().toString().isEmpty()) {
//                    spoiltVotes.setText("0");
//                }
////                if (!railaTotal.getText().toString().isEmpty() && !uhuruTotal.getText().toString().isEmpty()) {
////                    int raila = Integer.parseInt(railaTotal.getText().toString());
////                    int uhuru = Integer.parseInt(uhuruTotal.getText().toString());
////                    if (!s.toString().isEmpty()) {
////                        int spoilt = Integer.parseInt(s.toString());
////                        totalVotes.setText(String.valueOf(raila + uhuru + spoilt));
////                    }
////
////
////                    if (Integer.parseInt(totalVotes.getText().toString()) > 700) {
//////                        Toast.makeText(getContext(), "Total cannot exceed 700", Toast.LENGTH_SHORT).show();
////                    }
////                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Log.d(LOG_TAG, String.valueOf(new Date()));
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
                        pollAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, pollStationList);
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
                        pollStreamAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, pollStationStreamList);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.turnout_menu_item) {
            startActivity(new Intent(this, VoterTurnout.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        position = 1;
        if (pollStStr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Select a poll station first", Toast.LENGTH_SHORT).show();
        } else {
//            startChooser();
            PickImageDialog.build(new PickSetup()).show(this);
        }

    }

    @OnClick(R.id.submit_button)
    void submitButton() {
        railaStr = railaTotal.getText().toString();
        uhuruStr = uhuruTotal.getText().toString();
        spoiltVotesStr = spoiltVotes.getText().toString();
        total = totalVotes.getText().toString();
        registerdVoters = spoiltVotesStr;
        rejectedBallotPapersStr = rejectedBallot.getText().toString();
        rejectedObjectedStr = objectedRejected.getText().toString();
        disputedVotes = disputed.getText().toString();
        validVotesStr = total;


        if (!railaStr.isEmpty() && !uhuruStr.isEmpty() && !spoiltVotesStr.isEmpty() && !total.isEmpty()) {
            buttonSubmit.setEnabled(false);

            int rejecBal, objRej, dispuV = 0;
            rejecBal = Integer.parseInt(rejectedBallotPapersStr);
            objRej = Integer.parseInt(rejectedObjectedStr);
            dispuV = Integer.parseInt(disputedVotes);
            int spoiltTotal = rejecBal + objRej + dispuV;
            final String spoiltKura = String.valueOf(spoiltTotal);

            countyStr = countyStr.replace("'", "\\'");
            wardName = wardName.replace("'", "\\'");
            constName = constName.replace("'", "\\'");
            pollStStr = pollStStr.replace("'", "\\'");

            Log.d(LOG_TAG, countyStr + "||" + constName + "||" + wardName + "||" + pollStStr + "||" + railaStr + "||" + uhuruStr + "||" + spoiltVotesStr);
            final String iD = pollStationId.get(pollStationStreamList.indexOf(streamSpinner.getSelectedItem().toString()));
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.checkboxes, null);
            builder.setView(view);
            final EditText raila, uhuru, registered, rejectedBallotInDialog, rejectedObjected, disputedInDialog, validCast;

            raila = (EditText) view.findViewById(R.id.raila_total);
            uhuru = (EditText) view.findViewById(R.id.uhuru_total);
            registered = (EditText) view.findViewById(R.id.spoilt_votes);
            rejectedBallotInDialog = (EditText) view.findViewById(R.id.rejected_ballot);
            rejectedObjected = (EditText) view.findViewById(R.id.objected_rejected);
            disputedInDialog = (EditText) view.findViewById(R.id.disputed_votes);
            validCast = (EditText) view.findViewById(R.id.total_votes);

            raila.setText(railaStr);
            uhuru.setText(uhuruStr);
            registered.setText(registerdVoters);
            rejectedBallotInDialog.setText(rejectedBallotPapersStr);
            rejectedObjected.setText(rejectedObjectedStr);
            disputedInDialog.setText(disputedVotes);
            validCast.setText(validVotesStr);

            raila.setEnabled(false);
            uhuru.setEnabled(false);
            registered.setEnabled(false);
            rejectedBallotInDialog.setEnabled(false);
            rejectedObjected.setEnabled(false);
            disputedInDialog.setEnabled(false);
            validCast.setEnabled(false);

            builder.setTitle("Post data");
            builder.setMessage("Confirm posting of the data as it is");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    pushToTabeleOne(countyStr, constName, wardName, pollStStr, streamStr);

                    pushToTableTwo(iD, railaStr, uhuruStr, spoiltKura, total, String.valueOf(new Date()));

                    pushToTableTwoDev(iD, railaStr, uhuruStr, registerdVoters, rejectedBallotPapersStr,
                            rejectedObjectedStr, disputedVotes, validVotesStr, String.valueOf(new Date()));

                    uploadImageClient(iD);
                    uploadImageToAzure(iD);
                    candidatesView.setVisibility(View.GONE);
                    railaTotal.setText("");
                    uhuruTotal.setText("");
                    spoiltVotes.setText("");
                    rejectedBallot.setText("");
                    objectedRejected.setText("");
                    disputed.setText("");
                    totalVotes.setText("");


                    imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    buttonSubmit.setEnabled(true);
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        } else {
            railaTotal.setError("Please fill in this field");
            uhuruTotal.setError("Please fill in this field");
            spoiltVotes.setError("Please fill in this field");
            rejectedBallot.setError("Please fill in this field");
            objectedRejected.setError("Please fill in this field");
            disputed.setError("Please fill in this field");
            totalVotes.setError("Please fill in this field");
        }

    }

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            imageViewContainer.setImageBitmap(pickResult.getBitmap());
//            imageViewContainer.setImageURI(pickResult.getUri());

            bitmap = pickResult.getBitmap();
            imageUri = pickResult.getUri();
            if (bitmap != null) {
                if (candidatesView.getVisibility() == View.GONE) {
                    candidatesView.setVisibility(View.VISIBLE);
                }
                buttonSubmit.setEnabled(true);

            }
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void uploadImageClient(final String pollStId) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        pDialog.setMessage("Uploading...");
        pDialog.show();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        buttonSubmit.setEnabled(false);
                        Log.d("Upload image", s);
//                        Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();

                        // Show dialogbox
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Success!");
                        builder.setMessage("Data saved successfully");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        // Clear the spinner
                        streamSpinner.setAdapter(null);
                        loadPollStreamsRemote(pollStStr);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pDialog.dismiss();
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

    private void uploadImageToAzure(final String pollStationId) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
//        pDialog.setMessage("Uploading...");
//        pDialog.show();
        final String idTimeSufix = pollStationId + "_" + timeStamp;
        Log.d("Image upload to Azure", "started");

        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final int imageLength = imageStream.available();

            final Handler handler = new Handler();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String imageName = ImageManager.uploadImage(imageStream, imageLength, idTimeSufix);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, imageName + " uploaded to azure", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } catch (Exception e) {
                        final String exceptionMessage = e.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

    private void pushToTableTwo(final String pollStId, final String railaStr, final String uhuruStr, final String spoiltVotesStr, final String total,
                                final String timeOnDevice) {
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
                params.put("time_on_device", timeOnDevice);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }

    private void pushToTableTwoDev(final String pollStId, final String railaStr, final String uhuruStr, final String registeredVoters,
                                   final String rejectedBallotPapersStr, final String rejectedObjectedStr, final String disputedVotes,
                                   final String validVotesStr, final String timeOnDevice) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_TO_TABLE_TWO_DEV, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("poll_station_id", pollStId);
                params.put("raila", railaStr);
                params.put("uhuru", uhuruStr);
                params.put("registered_voters", registeredVoters);
                params.put("rejected_ballot", rejectedBallotPapersStr);
                params.put("rejected_objected", rejectedObjectedStr);
                params.put("disputed_votes", disputedVotes);
                params.put("valid_votes", validVotesStr);
                params.put("time_on_device", timeOnDevice);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }
}
