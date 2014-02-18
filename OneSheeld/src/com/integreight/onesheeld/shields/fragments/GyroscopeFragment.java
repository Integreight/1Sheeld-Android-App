package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.GravityShield;
import com.integreight.onesheeld.shields.controller.GyroscopeShield;
import com.integreight.onesheeld.shields.controller.GyroscopeShield.GyroscopeEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class GyroscopeFragment extends ShieldFragmentParent<GyroscopeFragment> {
	TextView x, y, z;
	TextView devicehasSensor;
	Button stoplistening_bt, startlistening_bt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.gyroscope_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {

		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);

		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Gravity Sheeld::OnActivityCreated()", "");

		x = (TextView) getView().findViewById(R.id.x_value_txt);
		y = (TextView) getView().findViewById(R.id.y_value_txt);
		z = (TextView) getView().findViewById(R.id.z_value_txt);

		devicehasSensor = (TextView) getView().findViewById(
				R.id.device_not_has_sensor_text);
		stoplistening_bt = (Button) getView().findViewById(
				R.id.stop_listener_bt);
		startlistening_bt = (Button) getView().findViewById(
				R.id.start_listener_bt);

		startlistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((GyroscopeShield) getApplication().getRunningShields().get(
						getControllerTag())).registerSensorListener();

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
		public void onSensorValueChangedFloat(float[] value) {
			// TODO Auto-generated method stub
			x.setText("X = " + value[0]);
			y.setText("Y = " + value[1]);
			z.setText("Z = " + value[2]);

		}

		@Override
		public void isDeviceHasSensor(Boolean hasSensor) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				if (!hasSensor) {
					devicehasSensor.setText("Your Device not have The Sensor");
					Toast.makeText(getActivity(),
							"Device dosen't have This Sensor !",
							Toast.LENGTH_SHORT).show();
				} else {

				}
			}

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new GyroscopeShield(getActivity(), getControllerTag()));

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
