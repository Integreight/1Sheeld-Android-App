package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.SensorUtil;
import com.integreight.onesheeld.utils.ControllerParent;

public class GravityShield extends ControllerParent<GravityShield> implements
		SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mGravity;
	private GravityEventHandler eventHandler;
	private ShieldFrame frame;

	public GravityShield() {
	}

	public GravityShield(Activity activity, String tag) {
		super(activity, tag);
		getApplication().getAppFirmata().initUart();

	}

	@Override
	public ControllerParent<GravityShield> setTag(String tag) {
		getApplication().getAppFirmata().initUart();

		mSensorManager = (SensorManager) getApplication().getSystemService(
				Context.SENSOR_SERVICE);
		mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

		return super.setTag(tag);
	}

	public void setGravityEventHandler(GravityEventHandler eventHandler) {
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
		frame = new ShieldFrame(UIShield.PROXIMITY_SHIELD.getId(), (byte) 0,
				ShieldFrame.DATA_SENT);
		// frame.addByteArgument((byte) Math.round(event.values[0]));
		frame.addFloatArgument(event.values[0]);
		frame.addFloatArgument(event.values[1]);
		frame.addFloatArgument(event.values[2]);
		activity.getThisApplication().getAppFirmata().sendShieldFrame(frame);
		eventHandler.onSensorValueChangedFloat(event.values);
	}

	// Register a listener for the sensor.
	public void registerSensorListener() {
		String sensorName = PackageManager.FEATURE_SENSOR_PROXIMITY;
		if (SensorUtil.isDeviceHasSensor(sensorName, activity.getApplication())) {
			mSensorManager.registerListener(this, mGravity,
					5000000);
			eventHandler.isDeviceHasSensor(true);

		} else {
			Log.d("Device dos't have Sensor ",
					PackageManager.FEATURE_SENSOR_PROXIMITY);
			eventHandler.isDeviceHasSensor(false);
		}
	}

	// Unregister a listener for the sensor .
	public void unegisterSensorListener() {
		if (mSensorManager != null) {
			// mSensorManager.unregisterListener(this);
			mSensorManager.unregisterListener(this, mGravity);
		}
	}

	public static interface GravityEventHandler {

		void onSensorValueChangedFloat(float[] value);

		void isDeviceHasSensor(Boolean hasSensor);

	}

}
