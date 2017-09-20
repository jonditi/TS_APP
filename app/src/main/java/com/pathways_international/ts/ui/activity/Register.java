package com.pathways_international.ts.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class Register extends AppCompatActivity {

    private static final String LOG_TAG = Register.class.getSimpleName();

    @BindView(R.id.name)
    EditText nameEditText;
    @BindView(R.id.email)
    EditText emailEditText;
    @BindView(R.id.last_name)
    EditText lastNameEditText;
    @BindView(R.id.id_number)
    EditText idNumberEditText;
    @BindView(R.id.phone)
    EditText phoneEditText;
    @BindView(R.id.password)
    EditText passwordEditText;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Register.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @OnClick(R.id.btnRegister)
    void register() {
        String userName = nameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String idNumber = idNumberEditText.getText().toString();
        String phoneNumber = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!userName.isEmpty() && !lastName.isEmpty() && !idNumber.isEmpty() && !phoneNumber.isEmpty() && !password.isEmpty()) {
            signUp(userName, lastName, idNumber, phoneNumber, password);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Please enter your details!", Toast.LENGTH_LONG)
                    .show();
        }

    }

    @OnClick(R.id.btnLinkToLoginScreen)
    void openLogin() {
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    /**
     * Register
     */
    private void signUp(final String userName, final String lastName, final String idNumber, final String phoneNumber, final String password) {
        pDialog.setMessage("Registering ...");
        pDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Urls.REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                Log.d(LOG_TAG, response);

//                try {
//                    JSONObject jObj = new JSONObject(response);
//                    boolean error = jObj.getBoolean("error");
//                    if (!error) {
//                        // User successfully stored in MySQL
//                        // Now store the user in sqlite
//                        String uid = jObj.getString("uid");
//
//                        JSONObject user = jObj.getJSONObject("user");
//                        String name = user.getString("name");
//                        String email = user.getString("email");
//                        String created_at = user
//                                .getString("created_at");
//
//                        // Inserting row in users table
//                        db.addUser(name, email, uid, created_at);
//
//                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();
//
//                        // Launch login activity
//                        Intent intent = new Intent(
//                                Register.this,
//                                Login.class);
//                        startActivity(intent);
//                        finish();
//                    } else {
//
//                        // Error occurred in registration. Get the error
//                        // message
//                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(getApplicationContext(),
//                                errorMsg, Toast.LENGTH_LONG).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();

                Log.e(LOG_TAG, "Sign up error: " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_name", userName);
                params.put("last_name", lastName);
                params.put("id_number", idNumber);
                params.put("phone", phoneNumber);
                params.put("password", password);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }


}
