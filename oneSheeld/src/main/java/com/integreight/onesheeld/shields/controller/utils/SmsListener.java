package com.integreight.onesheeld.shields.controller.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsListener extends BroadcastReceiver {
    private SmsReceiveEventHandler eventHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if (intent.getAction()
                .equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras(); // ---get the SMS message passed
            // in---
            SmsMessage[] msgs = null;
            String msg_from = "";
            String msgBody = "";
            if (bundle != null) {
                // ---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody = msgs[i].getMessageBody();
                    }
                    // Send SMS to Arduino
                    if (!msg_from.isEmpty()) {
                        eventHandler.onSmsReceiveSuccess(msg_from, msgBody);
                    }
                } catch (Exception e) {
                    // Fail to receive message
                    eventHandler.onSmsReceiveFailed();
                }
            }
        }
    }

    public void setSmsReceiveEventHandler(
            SmsReceiveEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public interface SmsReceiveEventHandler {
        void onSmsReceiveSuccess(String mobile_number, String sms_body);

        void onSmsReceiveFailed();

    }

}