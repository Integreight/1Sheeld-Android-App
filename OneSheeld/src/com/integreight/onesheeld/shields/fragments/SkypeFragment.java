package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SkypeShield;
import com.integreight.onesheeld.shields.controller.SkypeShield.SkypeEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class SkypeFragment extends ShieldFragmentParent<SkypeFragment> {


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.skype_shield_fragment_layout,
				container, false);
		return v;
	}

	@Override
	public void onStart() {

		getApplication().getRunningSheelds().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningSheelds().get(getControllerTag())
				.setHasForgroundView(false);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("Skype Sheeld::OnActivityCreated()", "");

		
	}

	private SkypeEventHandler skypeEventHandler = new SkypeEventHandler() {

		@Override
		public void onVideoCall(String user) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Toast.makeText(getActivity(), user + " Outgoing Video Call",
						Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onSkypeClientNotInstalled(String popMessage) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Toast.makeText(getActivity(), popMessage, Toast.LENGTH_SHORT)
						.show();

		}

		@Override
		public void onError(String error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onChat(String user) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Toast.makeText(getActivity(), user + " Outgoing Chat",
						Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onCall(String user) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Toast.makeText(getActivity(), user + " Outgoing Call",
						Toast.LENGTH_SHORT).show();

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningSheelds().get(getControllerTag()) == null) {
			getApplication().getRunningSheelds().put(getControllerTag(),
					new SkypeShield(getActivity(), getControllerTag()));
			((SkypeShield) getApplication()
					.getRunningSheelds().get(
							getControllerTag()))
					.setSkypeEventHandler(skypeEventHandler);
		}

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}
}
