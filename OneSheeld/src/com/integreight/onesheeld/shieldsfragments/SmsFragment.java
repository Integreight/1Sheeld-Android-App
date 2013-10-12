package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.SmsShield;
import com.integreight.onesheeld.shields.SmsShield.SmsEventHandler;

public class SmsFragment extends SherlockFragment {

	SmsShield smsShield;
	ShieldsOperationActivity activity;
	TextView smsTextTextView;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	ArduinoFirmata firmata;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.sms_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (activity.getFirmata() == null) {
			activity.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(activity.getFirmata());
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		smsTextTextView = (TextView) getView().findViewById(
				R.id.sms_shield_text_textview);

		activity = (ShieldsOperationActivity) getActivity();

	}

	private SmsEventHandler smsEventHandler = new SmsEventHandler() {

		@Override
		public void onSmsSent(String smsNumber, String smsText) {
			// TODO Auto-generated method stub
			smsTextTextView.setText(smsText);
			Toast.makeText(activity, "SMS Sent!", Toast.LENGTH_LONG).show();

		}

		@Override
		public void onSmsFail(String error) {
			// TODO Auto-generated method stub
			 Toast.makeText(activity,
					 error,
			 Toast.LENGTH_LONG).show();

		}
	};

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
		if (smsShield != null)
			return;
		this.firmata = firmata;
		smsShield = new SmsShield(firmata, activity);
		smsShield.setSmsEventHandler(smsEventHandler);
		toggleMenuButtons();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		enableSerialMenuItem = (MenuItem) menu
				.findItem(R.id.enable_serial_menuitem);
		disableSerialMenuItem = (MenuItem) menu
				.findItem(R.id.disable_serial_menuitem);
		toggleMenuButtons();
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.enable_serial_menuitem:
			firmata.initUart();
			toggleMenuButtons();
			return true;
		case R.id.disable_serial_menuitem:
			firmata.disableUart();
			toggleMenuButtons();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleMenuButtons() {
		if (firmata == null)
			return;
		if (firmata.isUartInit()) {
			disableSerialMenuItem.setVisible(true);
			enableSerialMenuItem.setVisible(false);
		} else {
			disableSerialMenuItem.setVisible(false);
			enableSerialMenuItem.setVisible(true);
		}
	}
}
