package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.PhoneShield;
import com.integreight.onesheeld.shields.controller.PhoneShield.PhoneEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class PhoneFragment extends ShieldFragmentParent<PhoneFragment> {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.phone_shield_fragment_layout,
				container, false);
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
		Log.d("PhoneFragment::OnActivityCreated()", "");

	}

	private PhoneEventHandler phoneEventHandler = new PhoneEventHandler() {

		@Override
		public void isRinging(boolean isRinging) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnCall(String phone_number) {
			// TODO Auto-generated method stub

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new PhoneShield(getActivity(), getControllerTag()));
			((PhoneShield) getApplication().getRunningShields().get(
					getControllerTag()))
					.setPhoneEventHandler(phoneEventHandler);
		}

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}
}
