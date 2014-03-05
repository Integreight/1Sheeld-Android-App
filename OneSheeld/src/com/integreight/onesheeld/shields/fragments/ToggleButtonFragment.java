package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.ToggleButtonShield;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.onPinSelectionListener;

public class ToggleButtonFragment extends
		ShieldFragmentParent<ToggleButtonFragment> {

	ToggleButton toggleButtonButton;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater
				.inflate(R.layout.toggle_button_shield_fragment_layout,
						container, false);
		toggleButtonButton = (ToggleButton) v
				.findViewById(R.id.toggle_button_shield_button_toggle_button);
		if ((ToggleButtonShield) getApplication().getRunningShields().get(
				getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new ToggleButtonShield(getActivity(), getControllerTag()));
		}
		toggleButtonButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						((ToggleButtonShield) getApplication()
								.getRunningShields().get(getControllerTag()))
								.setButton(isChecked);

					}
				});
		if ((ToggleButtonShield) getApplication().getRunningShields().get(
				getControllerTag()) != null)
			toggleButtonButton.setEnabled(true);

		return v;

	}

	@Override
	public void onStart() {

		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				ConnectingPinsView.getInstance().reset(
						getApplication().getRunningShields().get(
								getControllerTag()),
						new onPinSelectionListener() {

							@Override
							public void onSelect(ArduinoPin pin) {
								if (pin != null) {
									((ToggleButtonShield) getApplication()
											.getRunningShields().get(
													getControllerTag()))
											.setConnected(new ArduinoConnectedPin(
													pin.microHardwarePin,
													ArduinoFirmata.OUTPUT));
									((ToggleButtonShield) getApplication()
											.getRunningShields().get(
													getControllerTag()))
											.setButton(toggleButtonButton
													.isChecked());
									toggleButtonButton.setEnabled(true);
								}

							}
						});
			}
		});

		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(false);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void doOnServiceConnected() {
	}

}
