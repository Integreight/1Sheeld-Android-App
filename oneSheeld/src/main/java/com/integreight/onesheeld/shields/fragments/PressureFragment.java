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
import com.integreight.onesheeld.shields.controller.PressureShield;
import com.integreight.onesheeld.shields.controller.PressureShield.PressureEventHandler;

public class PressureFragment extends ShieldFragmentParent<PressureFragment> {
    TextView pressure_float;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pressure_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((PressureShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setPressureEventHandler(pressureEventHandler);
        ((PressureShield) getApplication().getRunningShields().get(
                getControllerTag())).registerSensorListener(true);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        pressure_float = (TextView) v.findViewById(R.id.pressure_float_txt);
    }

    private PressureEventHandler pressureEventHandler = new PressureEventHandler() {

        @Override
        public void onSensorValueChangedFloat(final String value) {
            // TODO Auto-generated method stub

            // set data to UI
            pressure_float.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        pressure_float.setVisibility(View.VISIBLE);
                        pressure_float.setText("" + value);
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
                    new PressureShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
