package com.pathways_international.ts.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.LocationSharedPrefs;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.model.LocationModel;
import com.pathways_international.ts.ui.model.TurnoutImages;
import com.pathways_international.ts.ui.model.VoterTurnout;
import com.pathways_international.ts.ui.utils.CropImage;
import com.pathways_international.ts.ui.utils.CropImageView;
import com.pathways_international.ts.ui.utils.ImageManager;
import com.pathways_international.ts.ui.utils.ImagePicker;
import com.pathways_international.ts.ui.utils.Urls;
import com.squareup.okhttp.OkHttpClient;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class VoterTurnoutActivity extends AppCompatActivity implements IPickResult {

    private static final String LOG_TAG = VoterTurnoutActivity.class.getSimpleName();

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
    TextInputEditText voterTurnoutTotalEditText;

    String totalString;

    Bitmap bitmap;
    private Uri imageUri;

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

    MobileServiceClient mobileServiceClient;
    MobileServiceTable<VoterTurnout> voterTurnoutMobileServiceTable;
    MobileServiceTable<TurnoutImages> turnoutImagesMobileServiceTable;

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

        // Hookup Azure mobile service
        try {
            mobileServiceClient = AppController.getInstance().getmClient();
            // Extend timeout
            mobileServiceClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public com.squareup.okhttp.OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // get the tables
            voterTurnoutMobileServiceTable = mobileServiceClient.getTable(VoterTurnout.class);
            turnoutImagesMobileServiceTable = mobileServiceClient.getTable(TurnoutImages.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
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


        imagePicker.setCropImage(false);
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        position = 1;
        if (pollStStr.isEmpty()) {
            Toast.makeText(VoterTurnoutActivity.this, "Select a poll station first", Toast.LENGTH_SHORT).show();
        } else {
//            startChooser();
            // To remove image cropping
            PickImageDialog.build(new PickSetup()).show(this);
        }

    }

    @OnClick(R.id.submit_button)
    void submitButton() {
        totalString = voterTurnoutTotalEditText.getText().toString();

        if (!totalString.isEmpty()) {
            buttonSubmit.setEnabled(false);
            Log.d(LOG_TAG, constName + "||" + wardName + "||" + pollStStr + "||" + totalString);

            final String iD = pollStationId.get(pollStationStreamList.indexOf(streamSpinner.getSelectedItem().toString()));

            countyStr = countyStr.replace("'", "\\'");
            wardName = wardName.replace("'", "\\'");
            constName = constName.replace("'", "\\'");
            pollStStr = pollStStr.replace("'", "\\'");

            AlertDialog.Builder builder = new AlertDialog.Builder(VoterTurnoutActivity.this);
            builder.setTitle("Post data");
            builder.setMessage("Proceed with posting of data");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String BLOB_BASE_URL = "https://tsazure.blob.core.windows.net/tsimages/";
                    final String timeStamp = getTimeStamp();
                    final String imageName = BLOB_BASE_URL + iD + "_" + timeStamp.replace(":", "%3A") + "_" + totalString;

                    VoterTurnout voterTurnout = new VoterTurnout(iD, totalString, timeStamp);
                    TurnoutImages turnoutImages = new TurnoutImages(imageName, iD);


//                    pushToVoterTurnoutAzure(iD, totalString, timeStamp);
//
//                    uploadTurnoutImageAzure(iD, timeStamp);
//                    pushToTurnoutImagesAzure(imageName, iD);

                    pushDataToAzure(voterTurnout, turnoutImages, iD, timeStamp);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            voterTurnoutTotalEditText.setError("Please fill in this field");

        }

        voterTurnoutTotalEditText.setText("");
        imageViewContainer.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));


    }

    public VoterTurnout addItemInVoterTurnoutTable(VoterTurnout turnout) throws ExecutionException, InterruptedException {
        return voterTurnoutMobileServiceTable.insert(turnout).get();
    }

    public TurnoutImages addItemInTurnoutImageTable(TurnoutImages turnoutImages) throws ExecutionException, InterruptedException {
        return turnoutImagesMobileServiceTable.insert(turnoutImages).get();
    }

    /**
     * Run an ASync task on the corresponding executor
     *
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
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
        pDialog.show();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_TURNOUT_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        buttonSubmit.setEnabled(true);
                        Log.d("Upload image", s);
                        // Show dialogbox
                        AlertDialog.Builder builder = new AlertDialog.Builder(VoterTurnoutActivity.this);
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
                        // Clear the spinners
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

    private void pushToVoterTurnoutAzure(final String pollStId, final String totalString, final String timeOnDevice) {
        if (mobileServiceClient == null) {
            return;
        }

        final VoterTurnout voterTurnout = new VoterTurnout();
        voterTurnout.setPollStationId(pollStId);
        voterTurnout.setTotalString(totalString);
        voterTurnout.setTimeOnDevice(timeOnDevice);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage("Posting Data");
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    final VoterTurnout item = addItemInVoterTurnoutTable(voterTurnout);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(VoterTurnoutActivity.this, item.getPollStationId(), Toast.LENGTH_SHORT).show();
                            Log.d("Success", "Azure VoterTurnout Table");
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };

        runAsyncTask(asyncTask);
    }

    private void pushToTurnoutImagesAzure(final String imageName, final String pollStId) {
        if (mobileServiceClient == null) {
            return;
        }

        final TurnoutImages turnoutImages = new TurnoutImages();
        turnoutImages.setmImage(imageName);
        turnoutImages.setmPollStationId(pollStId);

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage("Posting Data");
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    final TurnoutImages item = addItemInTurnoutImageTable(turnoutImages);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(VoterTurnoutActivity.this, item.getmImage(), Toast.LENGTH_SHORT).show();
                            Log.d("Success", "Azure TurnoutImages Table");
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


        };

        runAsyncTask(asyncTask);
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
                        constAdapter = new ArrayAdapter<>(VoterTurnoutActivity.this, android.R.layout.simple_spinner_dropdown_item, cosntituencies);
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

                        wardAdapter = new ArrayAdapter<>(VoterTurnoutActivity.this, android.R.layout.simple_spinner_dropdown_item, wardsList);
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
                        pollAdapter = new ArrayAdapter<>(VoterTurnoutActivity.this, android.R.layout.simple_spinner_dropdown_item, pollStationList);
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
                        pollStreamAdapter = new ArrayAdapter<>(VoterTurnoutActivity.this, android.R.layout.simple_spinner_dropdown_item, pollStationStreamList);
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

    private void uploadTurnoutImageAzure(final String pollStationId, final String timeStamp) {
        final String idTimeSuffix = pollStationId + "_" + timeStamp;
        Log.d("Image upload to Azure", "started");


        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final int imageLength = imageStream.available();

            final Handler handler = new Handler();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String imageName = ImageManager.uploadTurnoutImage(imageStream, imageLength, idTimeSuffix);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
//                                Toast.makeText(VoterTurnoutActivity.this, imageName + " uploaded to azure", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(VoterTurnoutActivity.this);
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
                                Log.d("Image upload to Azure", "Success");

                            }
                        });
                    } catch (Exception e) {
                        final String exceptionMessage = e.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(VoterTurnoutActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
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

    private void pushDataToAzure(final VoterTurnout voterTurnout, final TurnoutImages turnoutImages,
                                 final String pollStationId, final String timeStamp) {
        if (mobileServiceClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage("Posting Data");
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    final VoterTurnout item = addItemInVoterTurnoutTable(voterTurnout);
                    final TurnoutImages entity = addItemInTurnoutImageTable(turnoutImages);
                    final String idTimeSuffix = pollStationId + "_" + timeStamp;
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final int imageLength = imageStream.available();

                    final String imageName = ImageManager.uploadTurnoutImage(imageStream, imageLength, idTimeSuffix);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(MainActivity.this, entity.getRaila(), Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(VoterTurnoutActivity.this);
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
                            Log.d("Sucess", " Data saved in Azure Tables");
                            Log.d("Image Uploaded", imageName);
                            Log.d("VoterTurnout", item.getTotalString());
                            Log.d("Turnout Image", entity.getmImage());

                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pDialog.dismiss();
            }
        };

        runAsyncTask(asyncTask);
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
            imageUri = pickResult.getUri();

            buttonSubmit.setEnabled(true);
        }
    }

    private String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


}
