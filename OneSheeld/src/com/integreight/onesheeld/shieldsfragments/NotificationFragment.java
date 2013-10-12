package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.NotificationShield;
import com.integreight.onesheeld.shields.NotificationShield.NotificationEventHandler;

public class NotificationFragment extends SherlockFragment {

	NotificationShield notificationShield;
	ShieldsOperationActivity activity;
	TextView notificationTextTextView;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	ArduinoFirmata firmata;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.notification_shield_fragment_layout,
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
		notificationTextTextView = (TextView) getView().findViewById(
				R.id.notification_shield_text_textview);

		activity = (ShieldsOperationActivity) getActivity();

	}

	private NotificationEventHandler notificationEventHandler = new NotificationEventHandler() {
		
		@Override
		public void onNotificationReceive(String notificationText) {
			// TODO Auto-generated method stub
			notificationTextTextView.setText(notificationText);
			
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
		if (notificationShield != null)
			return;
		this.firmata = firmata;
		notificationShield = new NotificationShield(firmata, activity);
		notificationShield.setNotificationEventHandler(notificationEventHandler);
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
	
	private void toggleMenuButtons(){
		if(firmata==null)return;
		if (firmata.isUartInit()) {
			disableSerialMenuItem.setVisible(true);
			enableSerialMenuItem.setVisible(false);
		} else {
			disableSerialMenuItem.setVisible(false);
			enableSerialMenuItem.setVisible(true);
		}
	}

}
