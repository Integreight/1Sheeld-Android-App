package com.integreight.onesheeld.utils;

import com.actionbarsherlock.app.SherlockFragment;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;

import android.os.Bundle;

public abstract class ShieldFragmentParent<T extends ShieldFragmentParent<?>>
		extends SherlockFragment {
	private String controllerTag = "";

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
							((T) ShieldFragmentParent.this)
									.doOnServiceConnected();
						}

						@Override
						public void onFailure() {
							
						}
					});
		} else
			((T) ShieldFragmentParent.this).doOnServiceConnected();
		super.onStart();
	}

	public void doOnServiceConnected() {

	}

	public String getControllerTag() {
		return controllerTag;
	}

	public void setControllerTag(String controllerTag) {
		this.controllerTag = controllerTag;
	};

	public boolean canChangeUI() {
		return (getActivity() != null && getApplication().getRunningSheelds()
				.get(getControllerTag()).isHasForgroundView());
	}
}
