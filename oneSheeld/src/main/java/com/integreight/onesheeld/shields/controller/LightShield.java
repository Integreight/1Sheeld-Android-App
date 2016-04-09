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

public class LightShield extends ControllerParent<LightShield> implements
        SensorEventListener {
    public static final byte LIGHT_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mLight;
    private LightEventHandler eventHandler;
    private ShieldFrame frame;
    Handler handler;
    int PERIOD = 100;
    boolean flag = false;
    boolean isHandlerLive = false;
    float oldInput = 0;
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

    public LightShield() {
    }

    public LightShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<LightShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<LightShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        mSensorManager = (SensorManager) getApplication().getSystemService(
                Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        registerSensorListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void setLightEventHandler(LightEventHandler eventHandler) {
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
        if (flag && (oldInput != event.values[0] || isFirstTime)) {
            Log.d("Sensor Data of X", event.values[0] + "");
            if (eventHandler != null)
                eventHandler.onSensorValueChangedFloat(event.values[0] + "");
            isFirstTime = false;
            frame = new ShieldFrame(UIShield.LIGHT_SHIELD.getId(), LIGHT_VALUE);
            oldInput = event.values[0];
            frame.addArgument(3, Math.round(event.values[0]));
            sendShieldFrame(frame);
            flag = false;
        }
    }

    // Register a listener for the sensor.
    public void registerSensorListener(boolean isToastable) {
        // check on mSensorManager and sensor != null
        if (mSensorManager == null | mLight == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null
                && mLight != null) {
            // Success! There's sensor.
            if (!isHandlerLive) {
                handler = new Handler();
                mSensorManager.registerListener(this, mLight,
                        SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)
                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Light");
            }
        } else {
            // Failure! No sensor.
            Log.d("Device dos't have Sensor ", "Light");
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
        if (mSensorManager != null && handler != null && mLight != null) {

            mSensorManager.unregisterListener(this, mLight);
            mSensorManager.unregisterListener(this);
            if (processSensors != null)
                handler.removeCallbacks(processSensors);
            handler.removeCallbacksAndMessages(null);
            isHandlerLive = false;
        }
    }

    public static interface LightEventHandler {

        void onSensorValueChangedFloat(String value);

        void isDeviceHasSensor(Boolean hasSensor);

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        this.unegisterSensorListener();

    }

}
