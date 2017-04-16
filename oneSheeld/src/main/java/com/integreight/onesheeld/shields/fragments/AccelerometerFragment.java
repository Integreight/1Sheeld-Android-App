package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.AccelerometerShield;
import com.integreight.onesheeld.shields.controller.AccelerometerShield.AccelerometerEventHandler;

public class AccelerometerFragment extends
        ShieldFragmentParent<AccelerometerFragment> {
    TextView x, y, z;
    CheckBox enableLlinearAccelerometer;
    public static boolean isLinearActive = false;
    private static LinearLisenter linearLisenter;

    public static void setLinearLisenter(LinearLisenter Lisenter) {
        linearLisenter = Lisenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.accelerometer_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnStart() {
        ((AccelerometerShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setAccelerometerEventHandler(accelerometerEventHandler);
        ((AccelerometerShield) getApplication().getRunningShields().get(
                getControllerTag())).invalidateAccelerometer(true);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        x = (TextView) v.findViewById(R.id.x_value_txt);
        y = (TextView) v.findViewById(R.id.y_value_txt);
        z = (TextView) v.findViewById(R.id.z_value_txt);
        enableLlinearAccelerometer = (CheckBox) v.findViewById(R.id.linear_accelerometer);
        enableLlinearAccelerometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLinearActive = isChecked;
                linearLisenter.isLinearActive(isChecked);
                if (!((AccelerometerShield) getApplication().getRunningShields()
                        .get(getControllerTag())).isLinearExist)
                    enableLlinearAccelerometer.setChecked(false);
            }
        });
    }

    @Override
    public void doOnActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

    }

    private AccelerometerEventHandler accelerometerEventHandler = new AccelerometerEventHandler() {

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

        }

        @Override
        public void isDeviceHasSensor(final Boolean hasSensor) {
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new AccelerometerShield(activity, getControllerTag()));
        }
    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

    @Override
    public void doOnResume() {
        enableLlinearAccelerometer.setChecked(isLinearActive);
        linearLisenter.isLinearActive(isLinearActive);
    }

    public static interface LinearLisenter {
        void isLinearActive(Boolean isLinearActive);
    }
}
