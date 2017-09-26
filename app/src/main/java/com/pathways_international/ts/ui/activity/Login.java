package com.pathways_international.ts.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.app.AppController;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;
import com.pathways_international.ts.ui.utils.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Login extends AppCompatActivity {

    private static final String LOG_TAG = Login.class.getSimpleName();

    @BindView(R.id.user_name)
    EditText userName;
    @BindView(R.id.password)
    EditText passwordEdit;
    @BindView(R.id.btnLogin)
    Button loginButton;
    @BindView(R.id.btnLinkToRegisterScreen)
    Button linkToRegister;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasSendSmsPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

//        askForPermission(Manifest.permission.READ_SMS, 0);
        hasSendSmsPermission = permissionsDelegate.hasSendSmsPermission();

        if (!hasSendSmsPermission) {
            permissionsDelegate.requestSendSmsPermission();
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


    }

    @OnClick(R.id.btnLogin)
    void doLogin() {
        String userNamestr = userName.getText().toString();
        String password = passwordEdit.getText().toString();

        // Check for empty data in the form
        if (userNamestr.trim().length() > 0 && password.trim().length() > 0) {
            // login user
            login(userNamestr, password);
        } else {
            // Prompt user to enter credentials
            Toast.makeText(getApplicationContext(),
                    "Please enter the credentials!", Toast.LENGTH_LONG)
                    .show();
        }
    }


    @OnClick(R.id.btnLinkToRegisterScreen)
    void openRegisterActivity() {
        startActivity(new Intent(this, Register.class));
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(Login.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this, permission)) {
                // Called if the user has denied the permission before
                ActivityCompat.requestPermissions(Login.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(Login.this, new String[]{permission}, requestCode);
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
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {

        }
    }

    /**
     * Login
     */
    private void login(final String userName, final String password) {
        pDialog.setMessage("Logging in ...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, Urls.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                Log.d(LOG_TAG, response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("first_name");
                        String lastName = user.getString("last_name");
                        String idNumber = user.getString("id_number");
                        String phone = user.getString("phone");
                        String constName = user.getJSONObject("constituency_code").getString("CONSTITUENCY_NAME");
                        String wardName = user.getJSONObject("ward").getString("CAW_NAME");
                        String verificationKey = user.getJSONObject("verification_key").getString("verification_key");
                        String constCode = user.getString("const_code");
                        String wardCode = user.getString("ward_code");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, lastName, idNumber, phone, uid, constCode, constName, wardName, wardCode, created_at);

                        sendVerificationKeyInSms(phone, verificationKey);

                        // Launch main activity
                        Intent intent = new Intent(Login.this,
                                VerifyKey.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
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
                pDialog.dismiss();
                Log.d(LOG_TAG, "Login error " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_number", userName);
                params.put("password", password);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

    private void sendVerificationKeyInSms(String phoneNumber, String verificationKey) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, verificationKey, null, null);
            Log.d(LOG_TAG, phoneNumber + " " + verificationKey + " Message sent");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
