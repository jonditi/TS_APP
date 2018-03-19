package com.pathways_international.ts.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.model.ConstituencyTallyingCenter;
import com.pathways_international.ts.ui.model.ConstituencyUploads;
import com.pathways_international.ts.ui.model.TurnoutImages;
import com.pathways_international.ts.ui.model.VoterTurnout;
import com.pathways_international.ts.ui.utils.ImageManager;
import com.pathways_international.ts.ui.utils.Urls;
import com.squareup.okhttp.OkHttpClient;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CTC extends AppCompatActivity implements IPickResult {

    private static final String LOG_TAG = CTC.class.getSimpleName();

    @BindView(R.id.submit_button)
    Button buttonSubmit;
    @BindView(R.id.imageview_container)
    ImageView imageViewContainer;

    @BindView(R.id.constituency_name)
    TextView constituencyName;
    @BindView(R.id.county_name)
    TextView countyName;

    @BindView(R.id.raila_total)
    TextInputEditText railaTotal;
    @BindView(R.id.uhuru_total)
    TextInputEditText uhuruTotal;

    @BindView(R.id.poll_station_code)
    TextInputEditText pollStationCode;
    @BindView(R.id.registered_voters)
    TextInputEditText registeredVoters;

    @BindView(R.id.rejected_ballot)
    TextInputEditText rejectedBallot;
    @BindView(R.id.poll_station_name)
    TextInputEditText pollStationName;
    @BindView(R.id.total_votes)
    TextInputEditText totalVotes;

    @BindView(R.id.candidates_view)
    LinearLayout candidatesView;

    private ProgressDialog pDialog;

    String railaStr, uhuruStr, pollStationNameStr, pollStationCodeStr;
    String registeredVotersStr;
    String rejectedBallotStr;
    String rejectedObjectedStr;
    String validVotesStr;

    Bitmap bitmap;
    private Uri imageUri;

    private SQLiteHandler sqLiteHandler;
    private SessionManager sessionManager;

    String constName, constCode, wardName, wardCode, county;

    MobileServiceClient mobileServiceClient;
    MobileServiceTable<ConstituencyTallyingCenter> constituencyTallyingCenterMobileServiceTable;
    MobileServiceTable<ConstituencyUploads> uploadsMobileServiceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctc);
        ButterKnife.bind(this);
        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (bitmap == null) {
            candidatesView.setVisibility(View.GONE);
            buttonSubmit.setEnabled(false);
        }

        if (sessionManager.isLoggedIn()) {
            HashMap<String, String> user = sqLiteHandler.getUserDetails();
            constCode = user.get("constituency_code");
            constName = user.get("constituency_name");
            county = user.get("county_name");
            wardName = user.get("ward_name");
            wardCode = user.get("ward_code");

            Log.d(LOG_TAG, constName + "||" + wardName + "||" + county);

            countyName.setText(county);
            constituencyName.setText(constName);

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
            constituencyTallyingCenterMobileServiceTable = mobileServiceClient.getTable(ConstituencyTallyingCenter.class);
            uploadsMobileServiceTable = mobileServiceClient.getTable(ConstituencyUploads.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        PickImageDialog.build(new PickSetup()).show(this);
    }

    @OnClick(R.id.submit_button)
    void submitButton() {
        railaStr = railaTotal.getText().toString();
        uhuruStr = uhuruTotal.getText().toString();
        pollStationNameStr = pollStationName.getText().toString();
        pollStationCodeStr = pollStationCode.getText().toString();
        registeredVotersStr = registeredVoters.getText().toString();
        rejectedBallotStr = rejectedBallot.getText().toString();
        validVotesStr = totalVotes.getText().toString();


        if (!railaStr.isEmpty() && !uhuruStr.isEmpty() && !pollStationNameStr.isEmpty() && !pollStationCodeStr.isEmpty() && !rejectedBallotStr.isEmpty()
                && !registeredVotersStr.isEmpty() && !validVotesStr.isEmpty()) {
            buttonSubmit.setEnabled(false);


            county = county.replace("'", "\\'");
            wardName = wardName.replace("'", "\\'");
            constName = constName.replace("'", "\\'");
            pollStationNameStr = pollStationNameStr.replace("'", "\\'");

            Log.d(LOG_TAG, county + "||" + constName + "||" + wardName + "||" + pollStationNameStr + "||" + railaStr + "||" + uhuruStr + "||" + pollStationCodeStr);
            final AlertDialog.Builder builder = new AlertDialog.Builder(CTC.this);

            LayoutInflater inflater = LayoutInflater.from(CTC.this);
            View view = inflater.inflate(R.layout.result_fields, null);
            builder.setView(view);
            final EditText raila, uhuru, registered, rejectedBallotInDialog, pollStationNameInDialog, pollStationCodeInDialog, validCast;

            raila = (EditText) view.findViewById(R.id.raila_total);
            uhuru = (EditText) view.findViewById(R.id.uhuru_total);
            registered = (EditText) view.findViewById(R.id.registered_voters);
            rejectedBallotInDialog = (EditText) view.findViewById(R.id.rejected_ballot);
            pollStationNameInDialog = (EditText) view.findViewById(R.id.poll_station_name);
            pollStationCodeInDialog = (EditText) view.findViewById(R.id.poll_station_code);
            validCast = (EditText) view.findViewById(R.id.total_votes);

            raila.setText(railaStr);
            uhuru.setText(uhuruStr);
            registered.setText(registeredVotersStr);
            rejectedBallotInDialog.setText(rejectedBallotStr);
            pollStationNameInDialog.setText(pollStationNameStr);
            pollStationCodeInDialog.setText(pollStationCodeStr);
            validCast.setText(validVotesStr);

            raila.setEnabled(false);
            uhuru.setEnabled(false);
            registered.setEnabled(false);
            rejectedBallotInDialog.setEnabled(false);
            pollStationNameInDialog.setEnabled(false);
            pollStationCodeInDialog.setEnabled(false);
            validCast.setEnabled(false);

            builder.setTitle("Post data");
            builder.setMessage("Confirm posting of the data as it is");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String BLOB_BASE_URL = "https://tsazure.blob.core.windows.net/tsimages/";
                    final String timeStamp = getTimeStamp();
                    final String imageName = BLOB_BASE_URL + constCode + "_" + timeStamp;


                    ConstituencyTallyingCenter tallyingCenter =
                            new ConstituencyTallyingCenter(pollStationCodeStr, pollStationNameStr, railaStr,
                                    uhuruStr, registeredVotersStr, rejectedBallotStr,
                            validVotesStr, county, constName);

                    ConstituencyUploads constituencyUploads = new ConstituencyUploads(imageName, constCode);


//                    pushToTallyingCenterAzure(pollStationCodeStr, pollStationNameStr, railaStr, uhuruStr, registeredVotersStr, rejectedBallotStr,
//                            validVotesStr, county, constName);

//                    uploadImageClient(constCode);
//                    uploadConstImageAzure(constCode, timeStamp);
//                    pushToConstUploadsAzure(imageName, constCode);
                    pushDataToAzure(tallyingCenter, constituencyUploads, constCode, timeStamp);
                    candidatesView.setVisibility(View.GONE);
                    railaTotal.setText("");
                    uhuruTotal.setText("");
                    pollStationCode.setText("");
                    rejectedBallot.setText("");
                    pollStationName.setText("");
                    registeredVoters.setText("");
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
            registeredVoters.setError("Please fill in this field");
            rejectedBallot.setError("Please fill in this field");
            pollStationName.setError("Please fill in this field");
            pollStationCode.setError("Please fill in this field");
            totalVotes.setError("Please fill in this field");
        }

    }

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            imageViewContainer.setImageBitmap(pickResult.getBitmap());
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

    public ConstituencyUploads addItemInConstUploads(ConstituencyUploads uploads) throws ExecutionException, InterruptedException {
        return uploadsMobileServiceTable.insert(uploads).get();
    }

    public ConstituencyTallyingCenter addItemInTallyingCenter(ConstituencyTallyingCenter tallyingCenter) throws ExecutionException, InterruptedException {
        return constituencyTallyingCenterMobileServiceTable.insert(tallyingCenter).get();
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

    private void uploadImageClient(final String constCode) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        pDialog.setMessage("Uploading...");
        pDialog.show();
        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_CONSTITUENCY_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        pDialog.dismiss();
                        buttonSubmit.setEnabled(false);
                        Log.d("Upload image", s);
//                        Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();

                        if (!s.contains("error")) {
                            // Show dialogbox
                            AlertDialog.Builder builder = new AlertDialog.Builder(CTC.this);
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
                        } else {
                            Toast.makeText(getApplicationContext(), "An error occured while uploading the image", Toast.LENGTH_LONG).show();
                        }



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
                params.put("name", constCode + "_" + timeStamp);
                params.put("constituency_code", constCode);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }


    private void pushToConstituencyTallying(final String pollStationCode, final String pollStationNameStr, final String railaStr, final String uhuruStr, final String registeredVoters,
                                            final String rejectedBallotPapersStr, final String validVotesStr, final String county,
                                            final String constituency) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.PUSH_CONSTITUENCY_TALLY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Server Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("polling_station_code", pollStationCode);
                params.put("polling_station_name", pollStationNameStr);
                params.put("raila", railaStr);
                params.put("uhuru", uhuruStr);
                params.put("registered_voters", registeredVoters);
                params.put("rejected_ballot", rejectedBallotPapersStr);
                params.put("valid_votes", validVotesStr);
                params.put("county", county);
                params.put("constituency", constituency);


                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }


    private void uploadConstImageAzure(final String constCode, final String timeStamp) {
        if (!pDialog.isShowing()) {
            pDialog.setMessage("Uploading Image");
            pDialog.show();
        }
        final String idTimeSuffix = constCode + "_" + timeStamp;
        Log.d("Image upload to Azure", "started");


        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final int imageLength = imageStream.available();

            final Handler handler = new Handler();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String imageName = ImageManager.uploadConstituencyImage(imageStream, imageLength, idTimeSuffix);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pDialog.dismiss();
//                                Toast.makeText(CTC.this, imageName + " uploaded to azure", Toast.LENGTH_SHORT).show();
                                Log.d("Image upload to Azure", "Success");

                            }
                        });
                    } catch (Exception e) {
                        final String exceptionMessage = e.getMessage();
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(CTC.this, exceptionMessage, Toast.LENGTH_SHORT).show();
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

    private void pushToTallyingCenterAzure(final String pollStationCode, final String pollStationNameStr, final String railaStr, final String uhuruStr, final String registeredVoters,
                                           final String rejectedBallotPapersStr, final String validVotesStr, final String county,
                                           final String constituency) {
        if (mobileServiceClient == null) {
            return;
        }

        final ConstituencyTallyingCenter tallyingCenter = new ConstituencyTallyingCenter();
        tallyingCenter.setPollingStationCode(pollStationCode);
        tallyingCenter.setPollingStationName(pollStationNameStr);
        tallyingCenter.setRaila(railaStr);
        tallyingCenter.setUhuru(uhuruStr);
        tallyingCenter.setRegisteredVoters(registeredVoters);
        tallyingCenter.setRejectedBallot(rejectedBallotPapersStr);
        tallyingCenter.setValidVotesCast(validVotesStr);
        tallyingCenter.setCounty(county);
        tallyingCenter.setConstituency(constituency);

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
//                    final VoterTurnout item = addItemInVoterTurnoutTable(voterTurnout);
                    final ConstituencyTallyingCenter item = addItemInTallyingCenter(tallyingCenter);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(CTC.this, item.getPollingStationCode(), Toast.LENGTH_SHORT).show();
                            Log.d("Success", "Azure Tallying Table");
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
//                pDialog.dismiss();
            }
        };

        runAsyncTask(asyncTask);
    }

    private void pushToConstUploadsAzure(final String imageName, final String constCode) {
        if (mobileServiceClient == null) {
            return;
        }

        final ConstituencyUploads constituencyUploads = new ConstituencyUploads();
        constituencyUploads.setImage(imageName);
        constituencyUploads.setConstituencyCode(constCode);

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
//                    final VoterTurnout item = addItemInVoterTurnoutTable(voterTurnout);
                    final ConstituencyUploads item = addItemInConstUploads(constituencyUploads);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(CTC.this, item.getImage(), Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(CTC.this);
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
                            Log.d("Success", "Azure ConstUploads Table");
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

    private void pushDataToAzure(final ConstituencyTallyingCenter tallyingCenter, final ConstituencyUploads cUploads,
                                 final String constCode, final String timeStamp) {
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
                    final ConstituencyTallyingCenter item = addItemInTallyingCenter(tallyingCenter);
                    final ConstituencyUploads entity = addItemInConstUploads(cUploads);

                    final String idTimeSuffix = constCode + "_" + timeStamp;
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                    final int imageLength = imageStream.available();
                    final String imageName = ImageManager.uploadConstituencyImage(imageStream, imageLength, idTimeSuffix);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CTC.this);
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
                            Log.d("TallyinCenter", item.getConstituency());
                            Log.d("Constituency Upload", entity.getImage());

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

    private String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
