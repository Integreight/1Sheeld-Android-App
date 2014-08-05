package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.GyroscopeShield;
import com.integreight.onesheeld.shields.controller.GyroscopeShield.GyroscopeEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class GyroscopeFragment extends ShieldFragmentParent<GyroscopeFragment> {
	TextView x, y, z;
	TextView devicehasSensor;
	Button stoplistening_bt, startlistening_bt;
	View v;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		 v = inflater.inflate(R.layout.gyroscope_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		((GyroscopeShield) getApplication().getRunningShields().get(
				getControllerTag())).registerSensorListener(true);

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Gravity Sheeld::OnActivityCreated()", "");

		x = (TextView) v.findViewById(R.id.x_value_txt);
		y = (TextView) v.findViewById(R.id.y_value_txt);
		z = (TextView) v.findViewById(R.id.z_value_txt);

		devicehasSensor = (TextView) v.findViewById(
				R.id.device_not_has_sensor_text);
		stoplistening_bt = (Button) v.findViewById(
				R.id.stop_listener_bt);
		startlistening_bt = (Button) v.findViewById(
				R.id.start_listener_bt);

		startlistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((GyroscopeShield) getApplication().getRunningShields().get(
						getControllerTag())).registerSensorListener(true);

			}
		});

		stoplistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((GyroscopeShield) getApplication().getRunningShields().get(
						getControllerTag())).unegisterSensorListener();

			}
		});

	}

	private GyroscopeEventHandler gyroscopeEventHandler = new GyroscopeEventHandler() {

		@Override
		public void onSensorValueChangedFloat(final float[] value) {

			// TODO Auto-generated method stub
			if (canChangeUI()) {

				// set data to UI
				x.post(new Runnable() {

					@Override
					public void run() {
						x.setText("" + value[0]);
					}
				});
				y.post(new Runnable() {

					@Override
					public void run() {
						y.setText("" + value[1]);
					}
				});
				z.post(new Runnable() {

					@Override
					public void run() {
						z.setText("" + value[2]);

					}
				});
			}

		}

		@Override
		public void isDeviceHasSensor(final Boolean hasSensor) {/*
																 * // TODO
																 * Auto-generated
																 * method stub
																 * if
																 * (canChangeUI
																 * ()) {
																 * 
																 * // set data
																 * to UI
																 * uiHandler.
																 * removeCallbacksAndMessages
																 * (null);
																 * uiHandler
																 * .post(new
																 * Runnable() {
																 * 
																 * @Override
																 * public void
																 * run() { if
																 * (!hasSensor)
																 * {
																 * devicehasSensor
																 * .setText(
																 * "Your Device not have The Sensor"
																 * );
																 * Toast.makeText
																 * (
																 * activity
																 * ,
																 * "Device dosen't have This Sensor !"
																 * , Toast.
																 * LENGTH_SHORT
																 * ).show(); }
																 * else {
																 * 
																 * } } });
																 * 
																 * }
																 */
		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new GyroscopeShield(activity, getControllerTag()));

		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((GyroscopeShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setGyroscopeEventHandler(gyroscopeEventHandler);

	}
}
