package com.pathways_international.ts.ui.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.ui.activity.MainActivity;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.utils.Urls;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by android-dev on 10/9/17.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    private Context context;
    private SQLiteHandler db;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        db = new SQLiteHandler(context);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //if there is an network
        if (networkInfo != null) {
            //if connected to wifi or mobile data
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // get all unsynced values
                Cursor tableOneCursor = db.getUnSyncedTableOne();
                Cursor tableTwoDevCursor = db.getUnsyncedTableTwoDev();
                Cursor uploadsCursor = db.getUnsyncedUploads();
                if (tableOneCursor.moveToFirst()) {
                    do {
                        // TODO: add method to save to server
                    } while (tableOneCursor.moveToNext());
                }

                if (tableTwoDevCursor.moveToFirst()) {
                    do {
                        // TODO: add method to push to server
                    } while (tableTwoDevCursor.moveToNext());
                }

                if (uploadsCursor.moveToFirst()) {
                    do {
                        // TODO: Add method to push to server

                    } while (uploadsCursor.moveToNext());
                }

            }
        }

    }

    /**
     * Save un-synced table one contents to server
     */
    private void saveUnSyncedTableOne(final String countyStr, final String constStr, final String wardStr, final String pollStStr,
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

    /**
     * Save un-synced table two dev values to server
     *
     * @param pollStId
     * @param railaStr
     * @param uhuruStr
     * @param registeredVoters
     * @param rejectedBallotPapersStr
     * @param rejectedObjectedStr
     * @param disputedVotes
     * @param validVotesStr
     * @param timeOnDevice
     */
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

    private void uploadImageClient(final String pollStId, final String image) {
        final String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());

        Log.d("Image upload", "started");
        StringRequest request = new StringRequest(Request.Method.POST, Urls.UPLOAD_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        ;
                        Log.d("Upload image", s);
                        Log.d("Unsynced", "Data saved");
//                        Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("image", image);
                params.put("name", pollStId + "_" + timeStamp);
                params.put("poll_station_id", pollStId);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

    }
}
