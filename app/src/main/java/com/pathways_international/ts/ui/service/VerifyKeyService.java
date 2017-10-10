package com.pathways_international.ts.ui.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.ui.activity.VerifyKey;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.utils.Urls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android-dev on 9/26/17.
 */

public class VerifyKeyService extends IntentService {
    private static final String LOG_TAG = VerifyKeyService.class.getSimpleName();

    public VerifyKeyService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String verificationKey = intent.getStringExtra("otp");

        }
    }

    private void verifyKey(final String verificationKey) {
        StringRequest request = new StringRequest(Request.Method.POST, Urls.VERIFY_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, response);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("verification_key", verificationKey);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }
}
