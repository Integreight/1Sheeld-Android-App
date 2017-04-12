package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.fragments.AccelerometerFragment;
import com.integreight.onesheeld.utils.Log;

public class AccelerometerShield extends ControllerParent<AccelerometerShield>
        implements SensorEventListener {
    public static final byte ACCELEROMETER_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private AccelerometerEventHandler eventHandler;
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

    public AccelerometerShield() {
    }

    public AccelerometerShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<AccelerometerShield> init(String tag) {
        AccelerometerFragment.setLinearLisenter(linearLisenter);
        return super.init(tag);
    }

    @Override
    public ControllerParent<AccelerometerShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        mSensorManager = (SensorManager) getApplication().getSystemService(
                Context.SENSOR_SERVICE);
        if (!AccelerometerFragment.isLinearActive)
            mAccelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        else
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        registerSensorListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void setAccelerometerEventHandler(
            AccelerometerEventHandler eventHandler) {
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

    private AccelerometerFragment.LinearLisenter linearLisenter = new AccelerometerFragment.LinearLisenter() {
        @Override
        public void isLinearActive(Boolean isLinearActive) {
            if (!isLinearActive)
                mAccelerometer = mSensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            else
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            android.util.Log.d("Accelerometer", "LinearLisenter: " + mAccelerometer.getName());
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (flag
                && (oldInput_x != event.values[0]
                || oldInput_y != event.values[1]
                || oldInput_z != event.values[2] || isFirstTime)) {
            frame = new ShieldFrame(UIShield.ACCELEROMETER_SHIELD.getId(),
                    ACCELEROMETER_VALUE);
            isFirstTime = false;
            oldInput_x = event.values[0];
            oldInput_y = event.values[1];
            oldInput_z = event.values[2];
            // frame.addByteArgument((byte) Math.round(event.values[0]));
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
        if (mSensorManager == null | mAccelerometer == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            if (!AccelerometerFragment.isLinearActive)
                mAccelerometer = mSensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            else
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        android.util.Log.d("Accelerometer", "registerSensorListener: " + mAccelerometer.getName());
        if (mAccelerometer != null) {
            // Success! There's sensor.
            if (!isHandlerLive) {
                handler = new Handler();
                if (mAccelerometer != null)
                    mSensorManager.registerListener(this, mAccelerometer,
                            SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)
                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Accelerometer");
            }
        } else {
            // Failure! No sensor.
            Log.d("Device doesn't have Sensor ", "Accelerometer");
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
        if (mSensorManager != null && handler != null && mAccelerometer != null) {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this);
            if (processSensors != null)
                handler.removeCallbacks(processSensors);
            handler.removeCallbacksAndMessages(null);
            isHandlerLive = false;
        }
    }

    public static interface AccelerometerEventHandler {

        void onSensorValueChangedFloat(float[] value);

        void isDeviceHasSensor(Boolean hasSensor);

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        this.unegisterSensorListener();

    }

}
