package com.integreight.onesheeld.utils;

import com.actionbarsherlock.app.SherlockFragment;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;

import android.os.Bundle;

public abstract class ShieldFragmentParent extends SherlockFragment {

	public MainActivity getAppActivity() {
		return (MainActivity) super.getActivity();
	}

	public OneSheeldApplication getApplication() {
		return getAppActivity().getThisApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		if (getApplication().getAppFirmata() == null) {
			getApplication().addServiceEventHandler(
					new OneSheeldServiceHandler() {

						@Override
						public void onSuccess(ArduinoFirmata firmate) {
							doOnServiceConnected();
						}

						@Override
						public void onFailure() {
							// TODO Auto-generated method stub

						}
					});
		} else
			doOnServiceConnected();
		super.onStart();
	}

	public abstract void doOnServiceConnected();
}
