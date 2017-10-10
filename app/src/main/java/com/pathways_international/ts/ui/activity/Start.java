package com.pathways_international.ts.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.pathways_international.ts.R;
import com.pathways_international.ts.ui.helper.SessionManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Start extends AppCompatActivity {

    @BindView(R.id.constituency_agent)
    Button constituencyAgent;
    @BindView(R.id.poll_station_agent)
    Button pollstationAgent;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ButterKnife.bind(this);
        sessionManager = new SessionManager(getApplicationContext());

        if (sharedPreferences.getBoolean("FirstRun", true)) {
            if (sessionManager.isLoggedIn()) {
                if (sessionManager.getAgentType().equals("constituency agent")) {
                    startActivity(new Intent(getApplicationContext(), CTC.class));
                    finish();
                } else if (sessionManager.getAgentType().equals("polling station agent")) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        }

    }

    @OnClick(R.id.constituency_agent)
    void constituencyAgent() {
        sessionManager.setAgentType("constituency agent");
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    @OnClick(R.id.poll_station_agent)
    void pollAgent() {
        sessionManager.setAgentType("polling station agent");
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}
