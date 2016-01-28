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
import com.integreight.onesheeld.shields.controller.LightShield;
import com.integreight.onesheeld.shields.controller.LightShield.LightEventHandler;

public class LightFragment extends ShieldFragmentParent<LightFragment> {
    TextView light_float;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.light_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnStart() {
        ((LightShield) getApplication().getRunningShields().get(
                getControllerTag())).setLightEventHandler(lightEventHandler);
        ((LightShield) getApplication().getRunningShields().get(
                getControllerTag())).registerSensorListener(true);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        light_float = (TextView) v.findViewById(R.id.light_float_txt);
    }

    private LightEventHandler lightEventHandler = new LightEventHandler() {

        @Override
        public void onSensorValueChangedFloat(final String value) {
            // set data to UI
            light_float.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        light_float.setVisibility(View.VISIBLE);
                        light_float.setText("" + value);
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
                    new LightShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
