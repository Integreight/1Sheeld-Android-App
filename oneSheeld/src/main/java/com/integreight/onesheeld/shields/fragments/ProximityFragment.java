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
    TextView distance_float, distance_byte;
    TextView devicehasSensor;
    Button stoplistening_bt, startlistening_bt;

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
        distance_byte = (TextView) v.findViewById(R.id.distance_byte_txt);

        devicehasSensor = (TextView) v
                .findViewById(R.id.device_not_has_sensor_text);
        stoplistening_bt = (Button) v.findViewById(R.id.stop_listener_bt);
        startlistening_bt = (Button) v.findViewById(R.id.start_listener_bt);

        startlistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((ProximityShield) getApplication().getRunningShields().get(
                        getControllerTag())).registerSensorListener(true);
                distance_float.setVisibility(View.VISIBLE);
                distance_byte.setVisibility(View.VISIBLE);

            }
        });

        stoplistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((ProximityShield) getApplication().getRunningShields().get(
                        getControllerTag())).unegisterSensorListener();
                distance_float.setVisibility(View.INVISIBLE);
                distance_byte.setVisibility(View.INVISIBLE);

            }
        });

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
        public void onSensorValueChangedByte(final String value) {
            // TODO Auto-generated method stub

            // set data to UI
            distance_byte.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        distance_byte.setVisibility(View.VISIBLE);
                        distance_byte.setText("Distance in Byte = " + value);
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
