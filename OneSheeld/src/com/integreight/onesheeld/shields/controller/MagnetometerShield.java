package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.SensorUtil;
import com.integreight.onesheeld.utils.ControllerParent;

public class MagnetometerShield extends ControllerParent<MagnetometerShield>
		implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mMagnetometer;
	private MagnetometerEventHandler eventHandler;
	private ShieldFrame frame;
	HandlerThread mHandlerThread;
	Handler handler;

	public MagnetometerShield() {
	}

	public MagnetometerShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<MagnetometerShield> setTag(String tag) {
		mSensorManager = (SensorManager) getApplication().getSystemService(
				Context.SENSOR_SERVICE);
		mMagnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread("sensorThread");
		}
		return super.setTag(tag);
	}

	public void setMagnetometerEventHandler(
			MagnetometerEventHandler eventHandler) {
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
		frame = new ShieldFrame(UIShield.MAGNETOMETER_SHIELD.getId(), (byte) 0,
				ShieldFrame.DATA_SENT);
		// frame.addByteArgument((byte) Math.round(event.values[0]));
		frame.addFloatArgument(event.values[0]);
		frame.addFloatArgument(event.values[1]);
		frame.addFloatArgument(event.values[2]);
		activity.getThisApplication().getAppFirmata().sendShieldFrame(frame);

		final float sensorData[] = event.values;
		OnNewSensorData(sensorData);

		Log.d("Sensor Data of X", event.values[0] + "");
		Log.d("Sensor Data of Y", event.values[1] + "");
		Log.d("Sensor Data of Z", event.values[2] + "");

	}

	// Register a listener for the sensor.
	public void registerSensorListener() {
		String sensorName = PackageManager.FEATURE_SENSOR_COMPASS;
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread("sensorThread");
		}
		if (!mHandlerThread.isAlive()) {

			if (SensorUtil.isDeviceHasSensor(sensorName,
					activity.getApplication())) {
				mHandlerThread.start();
				handler = new Handler(mHandlerThread.getLooper());
				mSensorManager.registerListener(this, mMagnetometer, 1000000,
						handler);
				eventHandler.isDeviceHasSensor(true);
			} else {
				Log.d("Device dos't have Sensor ",
						PackageManager.FEATURE_SENSOR_COMPASS);
				eventHandler.isDeviceHasSensor(false);
			}
		} else {
			Log.d("Your Sensor is registered", sensorName);
		}

	}

	// Unregister a listener for the sensor .
	public void unegisterSensorListener() {
		if (mSensorManager != null && mHandlerThread != null
				&& mHandlerThread.isAlive()) {
			// mSensorManager.unregisterListener(this);
			mSensorManager.unregisterListener(this, mMagnetometer);
			mSensorManager.unregisterListener(this);
			handler.removeCallbacks(mHandlerThread);
			mHandlerThread.interrupt();
			mHandlerThread.getLooper().quit();
			stopThread();
		}
	}

	public void OnNewSensorData(final float data[]) {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				// use data here
				eventHandler.onSensorValueChangedFloat(data);

			}
		});
	}

	public synchronized void stopThread() {
		if (mHandlerThread != null) {
			Thread moribund = mHandlerThread;
			mHandlerThread = null;
			moribund.interrupt();
		}
	}

	public static interface MagnetometerEventHandler {

		void onSensorValueChangedFloat(float[] value);

		void isDeviceHasSensor(Boolean hasSensor);

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		this.unegisterSensorListener();

	}

}
