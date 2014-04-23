package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.GpsShield;
import com.integreight.onesheeld.shields.controller.GpsShield.GpsEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class GpsFragment extends ShieldFragmentParent<GpsFragment> {
	TextView Latit, Longit;
	Button startGps, stopGps;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.gps_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		((GpsShield) getApplication().getRunningShields().get(
				getControllerTag())).isGooglePlayServicesAvailableWithDialog();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Gps Sheeld::OnActivityCreated()", "");

		Latit = (TextView) getView().findViewById(R.id.lat_value_txt);
		Longit = (TextView) getView().findViewById(R.id.lang_value_txt);
		startGps = (Button) getView().findViewById(R.id.start_listener_bt);
		stopGps = (Button) getView().findViewById(R.id.stop_listener_bt);

		startGps.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((GpsShield) getApplication().getRunningShields().get(
						getControllerTag()))
						.isGooglePlayServicesAvailableWithDialog();

			}
		});

		stopGps.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((GpsShield) getApplication().getRunningShields().get(
						getControllerTag())).stopGps();

			}
		});

	}

	private GpsEventHandler gpsEventHandler = new GpsEventHandler() {

		@Override
		public void onLatChanged(String lat) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Latit.setText(lat);
		}

		@Override
		public void onLangChanged(String lang) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Longit.setText(lang);

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new GpsShield(getActivity(), getControllerTag()));

		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((GpsShield) getApplication().getRunningShields().get(
				getControllerTag())).setGpsEventHandler(gpsEventHandler);

	}

}
