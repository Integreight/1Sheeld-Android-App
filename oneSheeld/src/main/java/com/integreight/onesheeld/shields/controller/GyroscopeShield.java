package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class GyroscopeShield extends ControllerParent<GyroscopeShield>
        implements SensorEventListener {
    public static final byte GYROSCOPE_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mGyroscope;
    private GyroscopeEventHandler eventHandler;
    private ShieldFrame frame;
    Handler handler;
    int PERIOD = 100;
    boolean flag = false;
    boolean isHandlerLive = false;
    float oldInput_x = 0, oldInput_y = 0, oldInput_z = 0;
    boolean isFirstTime = true;

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            // Do work with the sensor values.

            flag = true;
            // The Runnable is posted to run again here:
            if (handler != null)
                handler.postDelayed(this, PERIOD);
        }
    };

    public GyroscopeShield() {
    }

    public GyroscopeShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<GyroscopeShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<GyroscopeShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        mSensorManager = (SensorManager) getApplication().getSystemService(
                Context.SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        registerSensorListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void setGyroscopeEventHandler(GyroscopeEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (flag
                && (oldInput_x != event.values[0]
                || oldInput_y != event.values[1]
                || oldInput_z != event.values[2] || isFirstTime)) {
            // TODO Auto-generated method stub
            frame = new ShieldFrame(UIShield.GYROSCOPE_SHIELD.getId(),
                    GYROSCOPE_VALUE);
            isFirstTime = false;
            oldInput_x = event.values[0];
            oldInput_y = event.values[1];
            oldInput_z = event.values[2];
            frame.addArgument(event.values[0]);
            frame.addArgument(event.values[1]);
            frame.addArgument(event.values[2]);
            sendShieldFrame(frame);
            if (eventHandler != null)
                eventHandler.onSensorValueChangedFloat(event.values);

            Log.d("Sensor Data of X", event.values[0] + "");
            Log.d("Sensor Data of Y", event.values[1] + "");
            Log.d("Sensor Data of Z", event.values[2] + "");

            //
            flag = false;
        }

    }

    // Register a listener for the sensor.
    public void registerSensorListener(boolean isToastable) {
        // check on mSensorManager and sensor != null
        if (mSensorManager == null | mGyroscope == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // Success! There's sensor.
            if (!isHandlerLive && mGyroscope != null) {
                handler = new Handler();
                mSensorManager.registerListener(this, mGyroscope,
                        SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)

                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Gyroscope");
            }
        } else {
            // Failure! No sensor.
            Log.d("Device dos't have Sensor ", "Gyroscope");
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast(R.string.general_toasts_device_doesnt_support_this_sensor_toast);
            if (eventHandler != null)
                eventHandler.isDeviceHasSensor(false);

        }
    }

    // Unregister a listener for the sensor .
    public void unegisterSensorListener() {
        // mSensorManager.unregisterListener(this);
        if (mSensorManager != null && handler != null && mGyroscope != null) {

            mSensorManager.unregisterListener(this, mGyroscope);
            mSensorManager.unregisterListener(this);
            if (processSensors != null)
                handler.removeCallbacks(processSensors);
            handler.removeCallbacksAndMessages(null);
            isHandlerLive = false;
        }
    }

    public static interface GyroscopeEventHandler {

        void onSensorValueChangedFloat(float[] value);

        void isDeviceHasSensor(Boolean hasSensor);

    }
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        this.unegisterSensorListener();

    }

}
