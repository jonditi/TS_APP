package com.pathways_international.ts.ui.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.pathways_international.ts.ui.helper.SQLiteHandler;
import com.pathways_international.ts.ui.helper.SessionManager;

import java.util.HashMap;

/**
 * Created by android-dev on 9/26/17.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {
    SQLiteHandler sqLiteHandler;
    SessionManager sessionManager;
    private Listener listener;
    private static SmsListener smsListener;
    public static final String SMS_BUNDLE = "pdus";
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


        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && intent.getAction() != null) {
            Bundle bundle = intent.getExtras();
            String smsBody = "";
            String sender = "";

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                    sender = smsMessage.getDisplayOriginatingAddress();
//                    smsBody += smsMessage.getMessageBody();
//                }
//            } else {
//                if (bundle != null) {
//                    Object[] pdus = (Object[]) bundle.get(SMS_BUNDLE);
//                    if (pdus == null) {
//                        // Display some error to the user
//                        Log.e(SmsBroadcastReceiver.class.getSimpleName(), "SmsBundle had no pdus key");
//                        return;
//                    }
//
//                    SmsMessage[] messages = new SmsMessage[pdus.length];
//                    for (int i = 0; i < messages.length; i++) {
//                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//                        smsBody += messages[i].getMessageBody();
//                    }
//                    sender = messages[0].getOriginatingAddress();
//                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
//                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), smsBody);
//                    sessionManager.setMessage(smsBody);
//                    Intent smsBodyIntent = new Intent("verificationKey");
//                    smsBodyIntent.putExtra("verification_key", smsBody);
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(smsBodyIntent);
//                    if (sender.contains(phone.substring(1))) {
//                        Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
//                        Log.d(SmsBroadcastReceiver.class.getSimpleName(), phone);
//                        smsListener.messageReceived(smsBody);
//                    }
//                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
//                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), phone);
//                    smsListener.messageReceived(smsBody);
//
//                }
//            }
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get(SMS_BUNDLE);
                if (pdus == null) {
                    // Display some error to the user
                    Log.e(SmsBroadcastReceiver.class.getSimpleName(), "SmsBundle had no pdus key");
                    return;
                }

                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    smsBody += messages[i].getMessageBody();
                }
                sender = messages[0].getOriginatingAddress();
                Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
                Log.d(SmsBroadcastReceiver.class.getSimpleName(), smsBody);
                sessionManager.setMessage(smsBody);
                Intent smsBodyIntent = new Intent("verificationKey");
                smsBodyIntent.putExtra("verification_key", smsBody);
                LocalBroadcastManager.getInstance(context).sendBroadcast(smsBodyIntent);
                if (sender.contains(phone.substring(1))) {
                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), phone);
                    smsListener.messageReceived(smsBody);
                }
                Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
                Log.d(SmsBroadcastReceiver.class.getSimpleName(), phone);
                smsListener.messageReceived(smsBody);

            }

            if (sender.contains(phone)) {
                if (listener != null) {
                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), sender);
                    listener.onSmsReceived(smsBody);
                } else if (listener == null) {
                    Log.d(SmsBroadcastReceiver.class.getSimpleName(), "listener is null");
                }
            }
        }


    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSmsReceived(String textMessage);
    }

    public static void bindListener(SmsListener listener) {
        smsListener = listener;
    }
}
