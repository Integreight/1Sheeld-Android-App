package com.integreight.onesheeld.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.Log;
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
	public String shieldName = "";
	public MainActivity activity;

	@Override
	public void onAttach(Activity activity) {
		this.activity = (MainActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		// activity = null;
		super.onDetach();
	}

	public MainActivity getAppActivity() {
		return activity;
	}

	public OneSheeldApplication getApplication() {
		return activity.getThisApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onActivityCreated(savedInstanceState);
	}

	public String getClassName() {
		return ((T) this).getClass().getName();
	}

	@Override
	public void onStart() {
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
				Log.e("TAG", "Exception", e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				Log.e("TAG", "Exception", e);
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
		activity.findViewById(R.id.settingsFixedHandler).setVisibility(
				hasSettings ? View.VISIBLE : View.GONE);
		activity.findViewById(R.id.pinsFixedHandler)
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
		String tagFromApp = OneSheeldApplication.shieldsFragmentsTags
				.get(((T) this).getClass().getName());
		controllerTag = (tagFromApp != null ? tagFromApp
				: controllerTag != null ? controllerTag
						: (getArguments() != null && getArguments().getString(
								"tag") != null) ? getArguments().getString(
								"tag") : getTag());
		if (controllerTag == null)
			Crashlytics
					.log("ControllerTag = null" + ((T) (this)) != null ? ((T) (this))
							.getClass().getName() : "");
		Log.d("TAG", controllerTag);
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
		((ToggleButton) activity.findViewById(R.id.shieldStatus))
				.setChecked(getApplication().getRunningShields().get(
						getControllerTag()).isInteractive);
		getApplication().getGaTracker().send(
				MapBuilder.createAppView()
						.set(Fields.SCREEN_NAME, getControllerTag()).build());
		Crashlytics.setString("Current View", getTag());
		super.onResume();
	}

	public boolean canChangeUI() {
		if (uiHandler == null)
			uiHandler = new Handler();
		return (getActivity() != null
				&& getControllerTag() != null
				&& getApplication().getRunningShields().get(getControllerTag()) != null && getApplication()
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
