package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.observer.OneSheeldServiceHandler;

@SuppressWarnings("unchecked")
public abstract class ShieldFragmentParent<T extends ShieldFragmentParent<?>>
		extends Fragment {
	private String controllerTag = "";
	public boolean hasSettings = false;
	public Handler uiHandler = new Handler();

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
		((ToggleButton) getActivity().findViewById(R.id.shieldStatus))
				.setChecked(getApplication().getRunningShields().get(
						getControllerTag()).isInteractive);
		uiHandler = new Handler();
		if (getApplication().getRunningShields().get(getControllerTag()) != null)
			getApplication().getRunningShields().get(getControllerTag())
					.setHasForgroundView(true);
		else {
			ControllerParent<?> type = null;
			UIShield shield = UIShield.valueOf(getControllerTag());
			try {
				type = shield.getShieldType().newInstance();
			} catch (java.lang.InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (type != null) {
				type.setActivity(getAppActivity()).setTag(shield.name());
				getApplication().getRunningShields().get(getControllerTag())
						.setHasForgroundView(true);
			}
		}
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
		getActivity().findViewById(R.id.settingsFixedHandler).setVisibility(
				hasSettings ? View.VISIBLE : View.GONE);
		getActivity()
				.findViewById(R.id.pinsFixedHandler)
				.setVisibility(
						getApplication().getRunningShields().get(
								getControllerTag()) == null
								|| getApplication().getRunningShields().get(
										getControllerTag()).requiredPinsIndex == -1 ? View.GONE
								: View.VISIBLE);
		super.onStart();
	}

	@Override
	public void onStop() {
		if (getApplication().getRunningShields().get(getControllerTag()) != null)
			getApplication().getRunningShields().get(getControllerTag())
					.setHasForgroundView(false);
		uiHandler.removeCallbacksAndMessages(null);
		super.onStop();
	}

	public void doOnServiceConnected() {

	}

	public String getControllerTag() {
		controllerTag = (controllerTag == null ? getTag() : controllerTag);
		return controllerTag;
	}

	public void setControllerTag(String controllerTag) {
		this.controllerTag = controllerTag;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onResume() {
		MainActivity.currentShieldTag = getControllerTag();
		super.onResume();
	}

	public boolean canChangeUI() {
		if (uiHandler == null)
			uiHandler = new Handler();
		return (getActivity() != null && getControllerTag() != null && getApplication()
				.getRunningShields().get(getControllerTag())
				.isHasForgroundView());
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
