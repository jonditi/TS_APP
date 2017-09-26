package com.pathways_international.ts.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;

import java.util.HashMap;

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

    SessionManager sessionManager;
    SQLiteHandler sqLiteHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_key);
        ButterKnife.bind(this);


        if (ActivityCompat.checkSelfPermission(VerifyKey.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            askForPermission(Manifest.permission.READ_SMS, 0);
            return;
        }

        sqLiteHandler = new SQLiteHandler(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            HashMap<String, String> user = sqLiteHandler.getUserDetails();
            String phone = user.get("phone");

            String message = getResources().getString(R.string.hint_verification_message) + " " + phone;

            textView.setText(message);

            if (!sessionManager.getMessage().isEmpty()) {
                verificationEdit.setText(sessionManager.getMessage());
            }
        }

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

}
