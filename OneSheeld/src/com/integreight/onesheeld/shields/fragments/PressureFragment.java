package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.PressureShield;
import com.integreight.onesheeld.shields.controller.PressureShield.PressureEventHandler;
import com.integreight.onesheeld.utils.Log;

public class PressureFragment extends ShieldFragmentParent<PressureFragment> {
	TextView pressure_float, pressure_byte;
	TextView devicehasSensor;
	Button stoplistening_bt, startlistening_bt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.pressure_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		((PressureShield) getApplication().getRunningShields().get(
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
		Log.d("Pressure Sheeld::OnActivityCreated()", "");

		pressure_float = (TextView) v.findViewById(R.id.pressure_float_txt);
		pressure_byte = (TextView) v.findViewById(R.id.pressure_byte_txt);

		devicehasSensor = (TextView) v
				.findViewById(R.id.device_not_has_sensor_text);
		stoplistening_bt = (Button) v.findViewById(R.id.stop_listener_bt);
		startlistening_bt = (Button) v.findViewById(R.id.start_listener_bt);

		startlistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((PressureShield) getApplication().getRunningShields().get(
						getControllerTag())).registerSensorListener(true);
				pressure_float.setVisibility(View.VISIBLE);
				pressure_byte.setVisibility(View.VISIBLE);

			}
		});

		stoplistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				((PressureShield) getApplication().getRunningShields().get(
						getControllerTag())).unegisterSensorListener();

				pressure_float.setVisibility(View.INVISIBLE);
				pressure_byte.setVisibility(View.INVISIBLE);

			}
		});

	}

	private PressureEventHandler pressureEventHandler = new PressureEventHandler() {

		@Override
		public void onSensorValueChangedFloat(final String value) {
			// TODO Auto-generated method stub

			// set data to UI
			pressure_float.post(new Runnable() {

				@Override
				public void run() {
					if (canChangeUI()) {
						pressure_float.setVisibility(View.VISIBLE);
						pressure_float.setText("" + value);
					}
				}
			});

		}

		@Override
		public void onSensorValueChangedByte(final String value) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {

				// set data to UI
				pressure_byte.post(new Runnable() {

					@Override
					public void run() {
						pressure_byte.setVisibility(View.VISIBLE);
						pressure_byte.setText("Pressure in Byte = " + value);
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
																 * run() {
																 * 
																 * if
																 * (!hasSensor)
																 * {
																 * devicehasSensor
																 * .setText(
																 * "Your Device not have The Sensor"
																 * );
																 * Toast.makeText
																 * ( activity ,
																 * "Device dosen't have This Sensor !"
																 * , Toast.
																 * LENGTH_SHORT
																 * ).show(); }
																 * else {
																 * pressure_float
																 * .
																 * setVisibility
																 * (
																 * View.VISIBLE)
																 * ;
																 * pressure_byte
																 * .
																 * setVisibility
																 * (
																 * View.VISIBLE)
																 * ;
																 * stoplistening_bt
																 * .
																 * setVisibility
																 * (
																 * View.VISIBLE)
																 * ; } } });
																 * 
																 * }
																 */
		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new PressureShield(activity, getControllerTag()));

		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((PressureShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setPressureEventHandler(pressureEventHandler);

	}
}
