package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.FoursquareShield;
import com.integreight.onesheeld.shields.controller.SkypeShield;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.controller.FoursquareShield.FoursquareEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class FoursquareFragment extends
		ShieldFragmentParent<FoursquareFragment> {

	TextView userName;
	TextView lastCheckin;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.foursquare_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		userName = (TextView) getView().findViewById(
				R.id.foursquare_shield_username_textview);
		lastCheckin = (TextView) getView().findViewById(
				R.id.foursquare_shield_last_checkin_textview);
		userName.setVisibility(View.VISIBLE);

		userName.setText(((FoursquareShield) getApplication()
				.getRunningShields().get(getControllerTag())).getUserName());
		lastCheckin.setText(((FoursquareShield) getApplication()
				.getRunningShields().get(getControllerTag())).getLastcheckin());
		Log.d("Foursquare Sheeld::OnActivityCreated()", "");

	}

	FoursquareEventHandler foursquareEventHandler = new FoursquareEventHandler() {

		@Override
		public void onPlaceCheckin(String placeName) {
			// TODO Auto-generated method stub
			lastCheckin.setText(placeName);
			Toast.makeText(getActivity(), "Your Last place Checkin !",
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onForsquareLoggedIn(String user) {
			// TODO Auto-generated method stub
			userName.setText(user);
		}

		@Override
		public void onForsquareLogout() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onForsquareError() {
			// TODO Auto-generated method stub

		}

	};

	@Override
	public void onStart() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(false);
		super.onStop();
	}

	private void initializeFirmata(ArduinoFirmata firmata) {

		if (getApplication().getRunningShields().get(getControllerTag()) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new FoursquareShield(getActivity(), getControllerTag()));

		((FoursquareShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setFoursquareEventHandler(foursquareEventHandler);

		((FoursquareShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.startFoursquare();

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata(getApplication().getAppFirmata());
	}

}
