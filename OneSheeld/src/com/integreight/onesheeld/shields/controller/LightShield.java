package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class LightShield extends ControllerParent<LightShield> implements
		SensorEventListener {
	public static final byte LIGHT_VALUE=0x01;
	private SensorManager mSensorManager;
	private Sensor mLight;
	private LightEventHandler eventHandler;
	private ShieldFrame frame;
	Handler handler;
	int PERIOD = 1000;
	boolean flag = false;
	boolean isHandlerLive = false;

	private final Runnable processSensors = new Runnable() {
		@Override
		public void run() {
			// Do work with the sensor values.

			flag = true;
			// The Runnable is posted to run again here:
			handler.postDelayed(this, PERIOD);
		}
	};

	public LightShield() {
	}

	public LightShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<LightShield> setTag(String tag) {
		mSensorManager = (SensorManager) getApplication().getSystemService(
				Context.SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

		return super.setTag(tag);
	}

	public void setLightEventHandler(LightEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
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
		// TODO Auto-generated method stub
		if (flag) {
			frame = new ShieldFrame(UIShield.LIGHT_SHIELD.getId(), LIGHT_VALUE);
			frame.addIntegerArgument(3, false, Math.round(event.values[0]));
			activity.getThisApplication().getAppFirmata()
					.sendShieldFrame(frame);

			Log.d("Sensor Data of X", event.values[0] + "");
			eventHandler.onSensorValueChangedFloat(event.values[0]+"");
			//
			flag = false;
		}

	}

	// Register a listener for the sensor.
	public void registerSensorListener() {
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			// Success! There's sensor.
			if (!isHandlerLive) {
				handler = new Handler();
				mSensorManager.registerListener(this, mLight,
						SensorManager.SENSOR_DELAY_NORMAL);
				handler.post(processSensors);
				eventHandler.isDeviceHasSensor(true);
				isHandlerLive = true;
			} else {
				Log.d("Your Sensor is registered", "Light");
			}
		} else {
			// Failure! No sensor.
			Log.d("Device dos't have Sensor ", "Light");
			eventHandler.isDeviceHasSensor(false);

		}
	}

	// Unregister a listener for the sensor .
	public void unegisterSensorListener() {
		// mSensorManager.unregisterListener(this);
		if (mSensorManager != null && handler != null && mLight != null) {

			mSensorManager.unregisterListener(this, mLight);
			mSensorManager.unregisterListener(this);
			handler.removeCallbacks(processSensors);
			handler.removeCallbacksAndMessages(null);
			isHandlerLive = false;

		}
	}

	public static interface LightEventHandler {

		void onSensorValueChangedFloat(String value);

		void onSensorValueChangedByte(String value);

		void isDeviceHasSensor(Boolean hasSensor);

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		this.unegisterSensorListener();

	}

}
