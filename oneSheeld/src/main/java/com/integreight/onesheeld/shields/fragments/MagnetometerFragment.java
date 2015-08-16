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
import com.integreight.onesheeld.shields.controller.MagnetometerShield;
import com.integreight.onesheeld.shields.controller.MagnetometerShield.MagnetometerEventHandler;

public class MagnetometerFragment extends
        ShieldFragmentParent<MagnetometerFragment> {
    TextView x, y, z, mf;
    TextView devicehasSensor;
    Button stoplistening_bt, startlistening_bt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.magnetometer_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((MagnetometerShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setMagnetometerEventHandler(magnetometerEventHandler);
        ((MagnetometerShield) getApplication().getRunningShields().get(
                getControllerTag())).registerSensorListener(true);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        x = (TextView) v.findViewById(R.id.x_value_txt);
        y = (TextView) v.findViewById(R.id.y_value_txt);
        z = (TextView) v.findViewById(R.id.z_value_txt);
        mf = (TextView) v.findViewById(R.id.mfValue);
        devicehasSensor = (TextView) v
                .findViewById(R.id.device_not_has_sensor_text);
        stoplistening_bt = (Button) v.findViewById(R.id.stop_listener_bt);
        startlistening_bt = (Button) v.findViewById(R.id.start_listener_bt);

        startlistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((MagnetometerShield) getApplication().getRunningShields().get(
                        getControllerTag())).registerSensorListener(true);

            }
        });

        stoplistening_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((MagnetometerShield) getApplication().getRunningShields().get(
                        getControllerTag())).unegisterSensorListener();

            }
        });
    }

    private MagnetometerEventHandler magnetometerEventHandler = new MagnetometerEventHandler() {

        @Override
        public void onSensorValueChangedFloat(final float[] value) {
            // TODO Auto-generated method stub
            // set data to UI
            x.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        x.setText("" + value[0]);
                }
            });
            y.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        y.setText("" + value[1]);
                }
            });
            z.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        z.setText("" + value[2]);

                }
            });
            mf.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        float magnetic_field = (float) Math
                                .sqrt((value[0] * value[0])
                                        + (value[1] * value[1])
                                        + (value[2] * value[2]));
                        mf.setText("Magnetic Field is "
                                + String.valueOf(magnetic_field).substring(
                                0,
                                String.valueOf(magnetic_field).indexOf(
                                        '.')) + " (uT)");
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
                    new MagnetometerShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
