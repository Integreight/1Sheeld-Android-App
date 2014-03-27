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
import com.integreight.onesheeld.shields.controller.ProximityShield;
import com.integreight.onesheeld.shields.controller.ProximityShield.ProximityEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class ProximityFragment extends ShieldFragmentParent<ProximityFragment> {
	TextView distance_float, distance_byte;
	TextView devicehasSensor;
	Button stoplistening_bt, startlistening_bt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.proximity_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Proximity Sheeld::OnActivityCreated()", "");

		distance_float = (TextView) getView().findViewById(
				R.id.distance_float_txt);
		distance_byte = (TextView) getView().findViewById(
				R.id.distance_byte_txt);

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
				((ProximityShield) getApplication().getRunningShields().get(
						getControllerTag())).registerSensorListener();
				distance_float.setVisibility(View.VISIBLE);
				distance_byte.setVisibility(View.VISIBLE);

			}
		});

		stoplistening_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ProximityShield) getApplication().getRunningShields().get(
						getControllerTag())).unegisterSensorListener();
				distance_float.setVisibility(View.INVISIBLE);
				distance_byte.setVisibility(View.INVISIBLE);

			}
		});

	}

	private ProximityEventHandler proximityEventHandler = new ProximityEventHandler() {

		@Override
		public void onSensorValueChangedFloat(String value) {
			// TODO Auto-generated method stub

			if (canChangeUI()) {
				distance_float.setVisibility(View.VISIBLE);
				distance_float.setText("Distance in float = " + value);
			}

		}

		@Override
		public void onSensorValueChangedByte(String value) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				distance_byte.setVisibility(View.VISIBLE);
				distance_byte.setText("Distance in Byte = " + value);
			}

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
					distance_float.setVisibility(View.VISIBLE);
					distance_byte.setVisibility(View.VISIBLE);
					stoplistening_bt.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new ProximityShield(getActivity(), getControllerTag()));

		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((ProximityShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setProximityEventHandler(proximityEventHandler);

	}
}
