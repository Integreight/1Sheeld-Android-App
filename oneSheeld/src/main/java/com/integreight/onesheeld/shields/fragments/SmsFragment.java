package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SmsShield;
import com.integreight.onesheeld.shields.controller.SmsShield.SmsEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class SmsFragment extends ShieldFragmentParent<SmsFragment> {

    LinearLayout smsTextContainer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sms_shield_fragment_layout, container,
                false);

    }

    @Override
    public void doOnStart() {
        ((SmsShield) getApplication().getRunningShields().get(
                getControllerTag())).setSmsEventHandler(smsEventHandler);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        smsTextContainer = (LinearLayout) v
                .findViewById(R.id.sms_shield_text_container);
    }

    private SmsEventHandler smsEventHandler = new SmsEventHandler() {

        @Override
        public void onSmsSent(final String smsNumber, final String smsText) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        OneSheeldTextView tv = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.sent_sms_details_row,
                                        smsTextContainer, false);
                        tv.setText(activity.getString(R.string.sms_sms_to)+" " + smsNumber + " (" + smsText + ")");
                        smsTextContainer.addView(tv);
                        Toast.makeText(activity, R.string.sms_sms_sent, Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });

        }

        @Override
        public void onSmsFail(String error) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void initializeFirmata() {
        if ((getApplication().getRunningShields().get(getControllerTag())) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new SmsShield(activity, getControllerTag()));
        ((SmsShield) getApplication().getRunningShields().get(
                getControllerTag())).setSmsEventHandler(smsEventHandler);
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
