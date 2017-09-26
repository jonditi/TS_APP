package com.pathways_international.ts.ui.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;

import java.util.HashMap;

/**
 * Created by android-dev on 9/26/17.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {
    SQLiteHandler sqLiteHandler;
    SessionManager sessionManager;
    public static final String SMS_BUNDLE = "ts_sms";
    HashMap<String, String> user;
    String phone;


    @Override
    public void onReceive(Context context, Intent intent) {
        sqLiteHandler = new SQLiteHandler(context);
        sessionManager = new SessionManager(context);
        user = sqLiteHandler.getUserDetails();
        if (!user.isEmpty()) {
            phone = user.get("phone");
        }

        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String message = "";

            for (int i = 0; i < sms.length; i++) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();

                if (address.contains(phone)) {
                    sessionManager.setMessage(smsBody);
                    abortBroadcast();
                }

            }
        }
    }
}
