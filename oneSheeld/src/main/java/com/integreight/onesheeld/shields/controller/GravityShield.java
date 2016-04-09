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

public class GravityShield extends ControllerParent<GravityShield> implements
        SensorEventListener {
    public static final byte GRAVITY_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mGravity;
    private GravityEventHandler eventHandler;
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

    public GravityShield() {
    }

    public GravityShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<GravityShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<GravityShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        mSensorManager = (SensorManager) getApplication().getSystemService(
                Context.SENSOR_SERVICE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        registerSensorListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void setGravityEventHandler(GravityEventHandler eventHandler) {
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
            frame = new ShieldFrame(UIShield.GRAVITY_SHIELD.getId(),
                    GRAVITY_VALUE);
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
    public void registerSensorListener(boolean isToastabel) {
        // check on mSensorManager and sensor != null
        if (mSensorManager == null | mGravity == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            // Success! There's sensor.
            if (!isHandlerLive && mGravity != null) {
                handler = new Handler();
                mSensorManager.registerListener(this, mGravity,
                        SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)
                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Gravity");
            }
        } else {
            // Failure! No sensor.
            Log.d("Device dos't have Sensor ", "Gravity");
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastabel)
                activity.showToast(R.string.general_toasts_device_doesnt_support_this_sensor_toast);
            if (eventHandler != null)
                eventHandler.isDeviceHasSensor(false);

        }
    }

    // Unregister a listener for the sensor .
    public void unegisterSensorListener() {
        // mSensorManager.unregisterListener(this);
        if (mSensorManager != null && handler != null && mGravity != null) {

            mSensorManager.unregisterListener(this, mGravity);
            mSensorManager.unregisterListener(this);
            if (processSensors != null)
                handler.removeCallbacks(processSensors);
            handler.removeCallbacksAndMessages(null);
            isHandlerLive = false;
        }
    }

    public static interface GravityEventHandler {

        void onSensorValueChangedFloat(float[] value);

        void isDeviceHasSensor(Boolean hasSensor);

    }
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        this.unegisterSensorListener();

    }

}
