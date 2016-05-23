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
import com.integreight.onesheeld.shields.controller.GravityShield;
import com.integreight.onesheeld.shields.controller.GravityShield.GravityEventHandler;

public class GravityFragment extends ShieldFragmentParent<GravityFragment> {
    TextView x, y, z;
    TextView devicehasSensor;
    Button stoplistening_bt, startlistening_bt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gravity_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((GravityShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setGravityEventHandler(gravityEventHandler);
        ((GravityShield) getApplication().getRunningShields().get(
                getControllerTag())).registerSensorListener(true);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        x = (TextView) v.findViewById(R.id.x_value_txt);
        y = (TextView) v.findViewById(R.id.y_value_txt);
        z = (TextView) v.findViewById(R.id.z_value_txt);

        devicehasSensor = (TextView) v
                .findViewById(R.id.device_not_has_sensor_text);
        stoplistening_bt = (Button) v.findViewById(R.id.stop_listener_bt);
        startlistening_bt = (Button) v.findViewById(R.id.start_listener_bt);

        startlistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((GravityShield) getApplication().getRunningShields().get(
                        getControllerTag())).registerSensorListener(true);

            }
        });

        stoplistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((GravityShield) getApplication().getRunningShields().get(
                        getControllerTag())).unegisterSensorListener();

            }
        });
    }

    private GravityEventHandler gravityEventHandler = new GravityEventHandler() {

        @Override
        public void onSensorValueChangedFloat(final float[] value) {
            // TODO Auto-generated method stub
            x.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        x.setText(String.valueOf(value[0]));
                }
            });
            y.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        y.setText(String.valueOf(value[1]));
                }
            });
            z.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        z.setText(String.valueOf(value[2]));

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
                    new GravityShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }
}
