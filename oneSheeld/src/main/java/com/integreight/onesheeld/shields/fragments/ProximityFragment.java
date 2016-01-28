package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.ProximityShield;
import com.integreight.onesheeld.shields.controller.ProximityShield.ProximityEventHandler;
import com.integreight.onesheeld.utils.Log;

public class ProximityFragment extends ShieldFragmentParent<ProximityFragment> {
    TextView distance_float;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.proximity_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((ProximityShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setProximityEventHandler(proximityEventHandler);
        ((ProximityShield) getApplication().getRunningShields().get(
                getControllerTag())).registerSensorListener(true);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        distance_float = (TextView) v.findViewById(R.id.distance_float_txt);
    }

    private ProximityEventHandler proximityEventHandler = new ProximityEventHandler() {

        @Override
        public void onSensorValueChangedFloat(final String value) {
            // TODO Auto-generated method stub

            // set data to UI
            distance_float.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        distance_float.setVisibility(View.VISIBLE);
                        distance_float.setText("" + value);
                    }
                }
            });

        }

        @Override
        public void isDeviceHasSensor(final Boolean hasSensor) {
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new ProximityShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
