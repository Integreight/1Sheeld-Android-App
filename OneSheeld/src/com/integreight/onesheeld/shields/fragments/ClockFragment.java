package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.ClockShield;
import com.integreight.onesheeld.shields.controller.ClockShield.ClockEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class ClockFragment extends ShieldFragmentParent<ClockFragment> {
	TextView time_tx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.clock_shield_fragment_layout,
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
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		time_tx = (TextView) getView().findViewById(R.id.time_txt);
	}

	private ClockEventHandler clockEventHandler = new ClockEventHandler() {

		@Override
		public void onTimeChanged(final String time) {
			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						time_tx.setText(time);
					}
				});
			}

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new ClockShield(getActivity(), getControllerTag()));

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((ClockShield) getApplication().getRunningShields().get(
				getControllerTag())).setClockEventHandler(clockEventHandler);
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

}
