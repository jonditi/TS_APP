package com.pathways_international.ts.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.receivers.SmsBroadcastReceiver;
import com.pathways_international.ts.ui.receivers.SmsListener;
import com.pathways_international.ts.ui.utils.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerifyKey extends AppCompatActivity {
    @BindView(R.id.verification_key)
    EditText verificationEdit;

    @BindView(R.id.done)
    Button doneButton;

    @BindView(R.id.msg)
    TextView textView;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    SessionManager sessionManager;
    SQLiteHandler sqLiteHandler;
    SmsBroadcastReceiver smsBroadcastReceiver = new SmsBroadcastReceiver();
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_key);
        ButterKnife.bind(this);

        // User should not enter the key manually
        // if it failed the first time, then can login again.
        verificationEdit.setEnabled(false);
        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override
            public void onSmsReceived(String textMessage) {
                Log.d(VerifyKey.class.getSimpleName(), textMessage);
                verificationEdit.setText(textMessage);
                progressBar.setVisibility(View.GONE);
            }
        });

        SmsBroadcastReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                Log.d("SmsBroacastReceiver", messageText);
                verificationEdit.setText(messageText);
                progressBar.setVisibility(View.GONE);

            }
        });

//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                final String key = intent.getStringExtra("verification_key");
//                verificationEdit.setText(key);
//            }
//        };

        if (ActivityCompat.checkSelfPermission(VerifyKey.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            askForPermission(Manifest.permission.READ_SMS, 0);
            return;
        }

        sqLiteHandler = new SQLiteHandler(this);
        sessionManager = new SessionManager(this);

        HashMap<String, String> user = sqLiteHandler.getUserDetails();
        String phone = user.get("phone");

        String message = getResources().getString(R.string.hint_verification_message) + " " + phone;

        textView.setText(message);

        if (!sessionManager.getMessage().isEmpty()) {
            verificationEdit.setText(sessionManager.getMessage());
        }

        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progress_bg), android.graphics.PorterDuff.Mode.SRC_ATOP);


    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(VerifyKey.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(VerifyKey.this, permission)) {
                // Called if the user has denied the permission before
                ActivityCompat.requestPermissions(VerifyKey.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(VerifyKey.this, new String[]{permission}, requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 0:

            }
        }
    }

    @OnClick(R.id.done)
    void doneClicked() {
        if (!verificationEdit.getText().toString().isEmpty()) {
            verifyKeyOnline(verificationEdit.getText().toString());
        }

    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(smsBroadcastReceiver, new IntentFilter("verification_key"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(smsBroadcastReceiver);
    }

    private void verifyKeyOnline(final String key) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, Urls.VERIFY_KEY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(VerifyKey.class.getSimpleName(), response);
                progressBar.setVisibility(View.GONE);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        sessionManager.setLogin(true);

                        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyKey.this);
                        builder.setTitle("Verified");
                        builder.setMessage("Agent verified successfully");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!sessionManager.getAgentType().isEmpty() && sessionManager.getAgentType().equals("constituency agent")) {
                                    // User is already logged in. Take him to main activity
                                    Intent intent = new Intent(VerifyKey.this, CTC.class);
                                    startActivity(intent);
                                    sessionManager.setLogin(true);
                                    finish();
                                } else if (!sessionManager.getAgentType().isEmpty() && sessionManager.getAgentType().equals("polling station agent")) {
                                    // User is already logged in. Take him to main activity
                                    Intent intent = new Intent(VerifyKey.this, MainActivity.class);
                                    startActivity(intent);
                                    sessionManager.setLogin(true);
                                    finish();
                                }
//                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                sessionManager.setLogin(true);
//                                finish();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("verification_key", key);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

}
