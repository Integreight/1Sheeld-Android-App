package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;

@SuppressWarnings("unchecked")
public abstract class ShieldFragmentParent<T extends ShieldFragmentParent<?>>
		extends Fragment {
	private String controllerTag = "";

	public MainActivity getAppActivity() {
		return (MainActivity) super.getActivity();
	}

	public OneSheeldApplication getApplication() {
		return getAppActivity().getThisApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
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
		return (getActivity() != null && getApplication().getRunningShields()
				.get(getControllerTag()).isHasForgroundView());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
}
