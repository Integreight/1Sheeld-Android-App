package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.SmsListener;
import com.integreight.onesheeld.shields.controller.utils.SmsListener.SmsReceiveEventHandler;
import com.integreight.onesheeld.utils.Log;

public class SmsShield extends ControllerParent<SmsShield> {
    private SmsEventHandler eventHandler;
    private String lastSmsText;
    private String lastSmsNumber;
    private static final byte SEND_SMS_METHOD_ID = (byte) 0x01;
    private SmsListener smsListener;
    private ShieldFrame frame;

    public String getLastSmsText() {
        return lastSmsText;
    }

    public String getLastSmsNumber() {
        return lastSmsNumber;
    }

    public SmsShield() {
        super();
    }

    @Override
    public ControllerParent<SmsShield> init(String tag) {
        smsListener = new SmsListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        if (smsReceiveEventHandler != null)
            smsListener.setSmsReceiveEventHandler(smsReceiveEventHandler);
        getApplication().registerReceiver(smsListener, filter);
        return super.init(tag);
    }

    @Override
    public ControllerParent<SmsShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        TelephonyManager tm = (TelephonyManager) getApplication()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            // No calling functionality
            if (this.selectionAction != null) {
                this.selectionAction.onFailure();
                if (isToastable)
                    activity.showToast(activity.getString(R.string.sms_your_device_doesnt_have_a_sim_card));
            }
        } else {
            addRequiredPremission(Manifest.permission.READ_SMS);
            addRequiredPremission(Manifest.permission.SEND_SMS);
            addRequiredPremission(Manifest.permission.RECEIVE_SMS);
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

    public SmsShield(Activity activity, String tag) {
        super(activity, tag);
    }

    protected void sendSms(String smsNumber, String smsText) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(smsNumber, null, smsText, null, null);
            if (eventHandler != null)
                eventHandler.onSmsSent(smsNumber, smsText);
        } catch (Exception e) {
            if (eventHandler != null)
                eventHandler.onSmsFail(e.getMessage());

            Log.e("TAG", "sendSms::sendTextMessage", e);
        }

    }

    public void setSmsEventHandler(SmsEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public interface SmsEventHandler {
        void onSmsSent(String smsNumber, String smsText);

        void onSmsFail(String error);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.SMS_SHIELD.getId()) {
            String smsNumber = frame.getArgumentAsString(0);
            String smsText = frame.getArgumentAsString(1);
            lastSmsText = smsText;
            if (frame.getFunctionId() == SEND_SMS_METHOD_ID) {
                sendSms(smsNumber, smsText);
            }

        }

    }

    private SmsReceiveEventHandler smsReceiveEventHandler = new SmsReceiveEventHandler() {

        @Override
        public void onSmsReceiveSuccess(String mobile_num, String sms_body) {
            // send frame contain SMS body..
            Log.d("SMS::Controller::onSmsReceiveSuccess", sms_body);
            frame = new ShieldFrame(UIShield.SMS_SHIELD.getId(), (byte) 0x01);
            frame.addArgument(mobile_num);
            frame.addArgument(sms_body);

            Log.d("Fram", frame.getArgumentAsString(1));
            sendShieldFrame(frame,true);
        }

        @Override
        public void onSmsReceiveFailed() {
            Log.d("SMS::Controller::onSmsReceiveFailed",
                    "Failed to receive SMS !");
        }
    };

    @Override
    public void reset() {
        try {
            getApplication().unregisterReceiver(smsListener);
        } catch (Exception e) {
        }
        frame = null;
        smsListener = null;
    }

}
