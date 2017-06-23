package com.pathways_international.ts.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.fragment.MainFragment;
import com.pathways_international.ts.ui.fragment.SettingsActivity;
import com.pathways_international.ts.ui.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.seat_spinner)
    Spinner seatSpinner;
    SQLiteHandler sqLiteHandler;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteHandler = new SQLiteHandler(getApplicationContext());
//        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_spinner);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        }
        ButterKnife.bind(this);
//        if (sqLiteHandler.getRowCount() <= 0) {
//            sampleQuery();
//        }

        initFragments(new MainFragment());
    }

    private void initFragments(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_body, fragment);
        transaction.commit();
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
}
