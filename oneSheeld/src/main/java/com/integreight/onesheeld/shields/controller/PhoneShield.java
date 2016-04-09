package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.PhoneCallStateListener;
import com.integreight.onesheeld.shields.controller.utils.PhoneCallStateListener.PhoneRingingEventHandler;
import com.integreight.onesheeld.utils.Log;

public class PhoneShield extends ControllerParent<PhoneShield> {
    private PhoneEventHandler eventHandler;
    private static final byte CALL_METHOD_ID = (byte) 0x01;
    private PhoneCallStateListener phoneListener;
    private TelephonyManager telephonyManager;
    private ShieldFrame frame;

    public PhoneShield() {
    }

    public PhoneShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<PhoneShield> init(String tag) {
        phoneListener = new PhoneCallStateListener();
        phoneListener.setPhoneRingingEventHandler(phoneRingingEventHandler);
        telephonyManager = (TelephonyManager) getApplication()
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,
                PhoneStateListener.LISTEN_CALL_STATE);
        return super.init(tag);
    }

    @Override
    public ControllerParent<PhoneShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        addRequiredPremission(Manifest.permission.CALL_PHONE);
        addRequiredPremission(Manifest.permission.READ_PHONE_STATE);
        TelephonyManager tm = (TelephonyManager) getApplication()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            // No calling functionality
            if (this.selectionAction != null) {
                this.selectionAction.onFailure();
                if (isToastable)
                    activity.showToast(activity.getString(R.string.phone_your_device_doesnt_have_a_sim_card));
            }
        } else {
            if (checkForPermissions()) {
                // calling functionality
                if (this.selectionAction != null) {
                    this.selectionAction.onSuccess();
                }
            }else{
                if (this.selectionAction != null) {
                    this.selectionAction.onFailure();
                }
            }
        }

        return super.invalidate(selectionAction, isToastable);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.PHONE_SHIELD.getId()) {
            String phone_number = frame.getArgumentAsString(0);

            switch (frame.getFunctionId()) {
                case CALL_METHOD_ID:
                    call(phone_number);
                    break;
                default:
                    break;
            }
        }

    }

    public void setPhoneEventHandler(PhoneEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public static interface PhoneEventHandler {
        void OnCall(String phone_number);

        void onReceiveACall(String phoneNumber);

        void isRinging(boolean isRinging);
    }

    private void call(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setPackage("com.android.phone");
            getApplication().startActivity(callIntent);
        } catch (Exception e) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(callIntent);
        }
        if (eventHandler != null)
            eventHandler.OnCall(phoneNumber);
        // Set Handlers to update UI..
    }

    private PhoneRingingEventHandler phoneRingingEventHandler = new PhoneRingingEventHandler() {

        @Override
        public void sendIncomingNumber(String phoneNumber) {
            // send frame contain Incoming Number..
            Log.d("Phone::Controller::SendIncomingNum", phoneNumber);
            frame = new ShieldFrame(UIShield.PHONE_SHIELD.getId(), (byte) 0x02);
            frame.addArgument(phoneNumber);
            sendShieldFrame(frame,true);
            if (eventHandler != null)
                eventHandler.onReceiveACall(phoneNumber);
        }

        @Override
        public void isPhoneRinging(boolean isRinging) {
            // send frame with Ringing state..
            Log.d("Phone::Controller::isPhoneRinging", isRinging + "");
            frame = new ShieldFrame(UIShield.PHONE_SHIELD.getId(), (byte) 0x01);
            if (isRinging) {
                frame.addArgument((byte) 1);
            } else {
                frame.addArgument((byte) 0);
            }
            sendShieldFrame(frame,true);
        }
    };

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        frame = null;
        if (phoneListener != null && telephonyManager != null)
            telephonyManager.listen(phoneListener,
                    PhoneStateListener.LISTEN_NONE);
    }

}
