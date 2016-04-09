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

public class PressureShield extends ControllerParent<PressureShield> implements
        SensorEventListener {
    public static final byte PRESSURE_VALUE = 0x01;
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private PressureEventHandler eventHandler;
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

    public PressureShield() {
    }

    public PressureShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<PressureShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<PressureShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        mSensorManager = (SensorManager) getApplication().getSystemService(
                Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        registerSensorListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void setPressureEventHandler(PressureEventHandler eventHandler) {
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
            isFirstTime = false;
            frame = new ShieldFrame(UIShield.PRESSURE_SHIELD.getId(),
                    PRESSURE_VALUE);
            oldInput = event.values[0];
            // frame.addByteArgument((byte) Math.round(event.values[0]));
            frame.addArgument(2, Math.round(event.values[0]));
            sendShieldFrame(frame);
            Log.d("Sensor Data of X", event.values[0] + "");
            if (eventHandler != null)
                eventHandler.onSensorValueChangedFloat(event.values[0] + "");
            //
            flag = false;
        }

    }

    // Register a listener for the sensor.
    public void registerSensorListener(boolean isToastable) {
        // check on mSensorManager and sensor != null
        if (mSensorManager == null | mPressure == null) {
            mSensorManager = (SensorManager) getApplication().getSystemService(
                    Context.SENSOR_SERVICE);
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            // Success! There's sensor.
            if (!isHandlerLive) {
                handler = new Handler();
                if (mPressure != null)
                    mSensorManager.registerListener(this, mPressure,
                            SensorManager.SENSOR_DELAY_GAME);
                handler.post(processSensors);
                if (eventHandler != null)
                    eventHandler.isDeviceHasSensor(true);
                isHandlerLive = true;
                if (selectionAction != null)
                    selectionAction.onSuccess();
            } else {
                Log.d("Your Sensor is registered", "Pressure");
            }
        } else {
            // Failure! No sensor.
            Log.d("Device dos't have Sensor ", "Pressure");
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
        if (mSensorManager != null && handler != null && mPressure != null) {

            mSensorManager.unregisterListener(this, mPressure);
            mSensorManager.unregisterListener(this);
            if (processSensors != null)
                handler.removeCallbacks(processSensors);
            handler.removeCallbacksAndMessages(null);
            isHandlerLive = false;
        }
    }

    public static interface PressureEventHandler {

        void onSensorValueChangedFloat(String value);

        void isDeviceHasSensor(Boolean hasSensor);

    }
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        this.unegisterSensorListener();

    }

}
