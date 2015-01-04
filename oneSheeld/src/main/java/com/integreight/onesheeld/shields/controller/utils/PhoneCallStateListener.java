package com.integreight.onesheeld.shields.controller.utils;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.integreight.onesheeld.utils.Log;

public class PhoneCallStateListener extends PhoneStateListener {
    private PhoneRingingEventHandler eventHandler;

    public PhoneCallStateListener() {
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (TelephonyManager.CALL_STATE_RINGING == state) {
            // phone ringing
            Log.d("Phone Number", "RINGING, number: " + incomingNumber);
            eventHandler.isPhoneRinging(true);
            eventHandler.sendIncomingNumber(incomingNumber);

        } else if (TelephonyManager.CALL_STATE_RINGING != state) {
            Log.d("Phone Number", "RINGING is Finish, number: "
                    + incomingNumber);
            eventHandler.isPhoneRinging(false);
        }
    }

    public void setPhoneRingingEventHandler(
            PhoneRingingEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public static interface PhoneRingingEventHandler {
        void isPhoneRinging(boolean isRinging);

        void sendIncomingNumber(String phoneNumber);

    }
}
