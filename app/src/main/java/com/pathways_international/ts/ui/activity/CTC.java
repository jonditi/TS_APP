package com.pathways_international.ts.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.LocationSharedPrefs;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.utils.Urls;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    EditText railaTotal;
    @BindView(R.id.uhuru_total)
    EditText uhuruTotal;

    @BindView(R.id.poll_station_code)
    EditText pollStationCode;
    @BindView(R.id.registered_voters)
    EditText registeredVoters;

    @BindView(R.id.rejected_ballot)
    EditText rejectedBallot;
    @BindView(R.id.poll_station_name)
    EditText pollStationName;
    @BindView(R.id.total_votes)
    EditText totalVotes;

    @BindView(R.id.candidates_view)
    LinearLayout candidatesView;

    private ProgressDialog pDialog;

    String railaStr, uhuruStr, pollStationNameStr, pollStationCodeStr;
    String registerdVoters;
    String rejectedBallotStr;
    String rejectedObjectedStr;
    String validVotesStr;

    Bitmap bitmap;

    private SQLiteHandler sqLiteHandler;
    private SessionManager sessionManager;

    String constName, constCode, wardName, wardCode, county;

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
    }

    @OnClick(R.id.imageview_container)
    void imageViewContainer() {
        PickImageDialog.build(new PickSetup()).show(this);
    }

    // TODO: Handle submit button click.

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            imageViewContainer.setImageBitmap(pickResult.getBitmap());
            bitmap = pickResult.getBitmap();
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
