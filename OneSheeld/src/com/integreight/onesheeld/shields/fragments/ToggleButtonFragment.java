package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.ToggleButtonShield;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class ToggleButtonFragment extends ShieldFragmentParent {

	ToggleButton toggleButtonButton;
	ToggleButtonShield toggleButtonShield;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater
				.inflate(R.layout.toggle_button_shield_fragment_layout,
						container, false);
		toggleButtonButton = (ToggleButton) v
				.findViewById(R.id.toggle_button_shield_button_toggle_button);
		toggleButtonButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						toggleButtonShield.setButtonOn(isChecked);

					}
				});
		if (toggleButtonShield != null)
			toggleButtonButton.setEnabled(true);

		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.toggle_button_fragment_connect_button);

		final CharSequence[] items = { "0", "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4",
				"A5" };

		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				AlertDialog.Builder builder3 = new AlertDialog.Builder(
						getActivity());
				builder3.setTitle("Connect With").setItems(items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// TODO Auto-generated method stub
								toggleButtonShield.setConnectedPin(which);
								toggleButtonButton.setEnabled(true);

							}

						});

				builder3.show();

			}

		});
	}

	private void initializeFirmata(ArduinoFirmata firmata) {

		if (toggleButtonShield != null)
			return;
		toggleButtonShield = new ToggleButtonShield(firmata);

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata(getApplication().getAppFirmata());
	}

}
