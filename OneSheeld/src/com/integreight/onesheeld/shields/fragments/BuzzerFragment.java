package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield.SpeakerEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class BuzzerFragment extends ShieldFragmentParent<BuzzerFragment> {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.buzzer_shield_fragment_layout,
				container, false);
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
		super.onActivityCreated(savedInstanceState);
		Log.d("BuzeerFragment::OnActivityCreated()", "");

	}

	private SpeakerEventHandler speakerEventHandler = new SpeakerEventHandler() {

		@Override
		public void onSpeakerChange(boolean isOn) {

			if (canChangeUI()) {
			}

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new SpeakerShield(getActivity(), getControllerTag()));
		}

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

	@Override
	public void onResume() {
		((SpeakerShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setSpeakerEventHandler(speakerEventHandler);
		((SpeakerShield) getApplication().getRunningShields().get(
				getControllerTag())).doOnResume();
		super.onResume();
	}
}
