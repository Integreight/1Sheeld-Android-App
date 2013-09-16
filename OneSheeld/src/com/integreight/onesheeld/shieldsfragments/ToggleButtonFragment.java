package com.integreight.onesheeld.shieldsfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.ToggleButtonShield;

public class ToggleButtonFragment extends Fragment {

	ToggleButton toggleButtonButton;
	ToggleButtonShield toggleButtonShield;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.toggle_button_shield_fragment_layout,
				container, false);
		toggleButtonButton = (ToggleButton) v.findViewById(R.id.toggle_button_shield_button_toggle_button);

		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (((ShieldsOperationActivity) getActivity()).getFirmata() == null) {
			((ShieldsOperationActivity) getActivity())
					.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(((ShieldsOperationActivity) getActivity())
					.getFirmata());
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.toggle_button_fragment_connect_button);

		final CharSequence[] items = { "0","1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4", "A5" };

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

	private OneSheeldServiceHandler serviceHandler = new OneSheeldServiceHandler() {

		@Override
		public void onServiceConnected(ArduinoFirmata firmata) {
			// TODO Auto-generated method stub

			initializeFirmata(firmata);

		}

		@Override
		public void onServiceDisconnected() {
			// TODO Auto-generated method stub

		}
	};


	private void initializeFirmata(ArduinoFirmata firmata) {

		if(toggleButtonShield!=null) return;
		toggleButtonShield = new ToggleButtonShield(firmata);
		toggleButtonButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				toggleButtonShield.setButtonOn(isChecked);
				
			}
		});
		

	}

}

