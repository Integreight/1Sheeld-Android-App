package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.PhoneShield;
import com.integreight.onesheeld.shields.controller.PhoneShield.PhoneEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class PhoneFragment extends ShieldFragmentParent<PhoneFragment> {
    LinearLayout callsLogContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.phone_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnStart() {
        ((PhoneShield) getApplication().getRunningShields().get(
                getControllerTag())).setPhoneEventHandler(phoneEventHandler);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        callsLogContainer = (LinearLayout) v.findViewById(R.id.callsCont);
    }

    private PhoneEventHandler phoneEventHandler = new PhoneEventHandler() {

        @Override
        public void isRinging(boolean isRinging) {
            // TODO Auto-generated method stub

        }

        @Override
        public void OnCall(final String phone_number) {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        OneSheeldTextView call = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.outgoing_call_item,
                                        callsLogContainer, false);
                        call.setText(activity.getString(R.string.phone_call_to)+" "+ phone_number);
                        callsLogContainer.addView(call);
                    }
                }
            });
        }

        @Override
        public void onReceiveACall(final String phoneNumber) {
            if (canChangeUI()) {
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        OneSheeldTextView call = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.incoming_call_item,
                                        callsLogContainer, false);
                        call.setText(activity.getString(R.string.phone_call_from)+" " + phoneNumber);
                        callsLogContainer.addView(call);
                    }
                });
            }
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new PhoneShield(activity, getControllerTag()));
            ((PhoneShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setPhoneEventHandler(phoneEventHandler);
        }

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
