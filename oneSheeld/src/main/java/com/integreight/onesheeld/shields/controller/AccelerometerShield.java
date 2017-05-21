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

public class AccelerometerShield extends ControllerParent<AccelerometerShield> {
    public static final byte ACCELEROMETER_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelecrometer;
    private AccelerometerEventHandler eventHandler;
    private ShieldFrame frame;
    Handler handler;
    public boolean isLinearExist = false;
    private boolean linearActive = false;
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
            mLinearAccelecrometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        invalidateAccelerometer(isToastable);
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

    private AccelerometerFragment.LinearLisenter linearLisenter = new AccelerometerFragment.LinearLisenter() {
        @Override
        public void isLinearActive(Boolean isLinearActive) {
            linearActive = isLinearActive;
            if (!isLinearActive && !isLinearExist)
                invalidateLinear(true);
            if (!linearActive) {
                mSensorManager.unregisterListener(sensorEventListener, mLinearAccelecrometer);
                mSensorManager.registerListener(sensorEventListener, mAccelerometer,
                        SensorManager.SENSOR_DELAY_GAME);
            } else if (linearActive) {
                mSensorManager.unregisterListener(sensorEventListener, mAccelerometer);
                mSensorManager.registerListener(sensorEventListener, mLinearAccelecrometer,
                        SensorManager.SENSOR_DELAY_GAME);
            }
        }
    };


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            android.util.Log.d("Accelerometer", "onSensorChanged: " + sensor.getName());
            frame = new ShieldFrame(UIShield.ACCELEROMETER_SHIELD.getId(),
                    ACCELEROMETER_VALUE);
            if (flag
                    && (oldInput_x != event.values[0]
                    || oldInput_y != event.values[1]
                    || oldInput_z != event.values[2] || isFirstTime)) {
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
                flag = false;
            }
        }
    };

    public void invalidateLinear(boolean isToastable) {
        // check on mSensorManager and sensor != null
        if (mLinearAccelecrometer == null) {
            mLinearAccelecrometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            isLinearExist = true;
            // Success! There's sensor.
            if (!isHandlerLive) {
                handler = new Handler();
                if (mLinearAccelecrometer != null)
                    mSensorManager.registerListener(sensorEventListener, mLinearAccelecrometer,
                            SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)
                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Linear Acceleration");
            }
        } else {
            isLinearExist = false;
            // Failure! No sensor.
            Log.d("Device doesn't have Sensor ", "Linear Acceleration");
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast(R.string.general_toasts_device_doesnt_support_this_sensor_toast);

            if (eventHandler != null)
                eventHandler.isDeviceHasSensor(false);

        }
    }

    public void invalidateAccelerometer(boolean isToastable) {
        // check on mSensorManager and sensor != null
        if (mSensorManager == null | mAccelerometer == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Success! There's sensor.
            if (!isHandlerLive) {
                handler = new Handler();
                if (mAccelerometer != null)
                    mSensorManager.registerListener(sensorEventListener, mAccelerometer,
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
        if (mSensorManager != null && handler != null && (mAccelerometer != null || mLinearAccelecrometer != null)) {
            mSensorManager.unregisterListener(sensorEventListener, mAccelerometer);
            mSensorManager.unregisterListener(sensorEventListener, mLinearAccelecrometer);
            mSensorManager.unregisterListener(sensorEventListener);
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
