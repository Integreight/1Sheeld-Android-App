package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.shields.controller.NotificationShield.NotificationEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class NotificationFragment extends
		ShieldFragmentParent<NotificationFragment> {

	TextView notificationTextTextView;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	View v;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.notification_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;

	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		notificationTextTextView = (TextView) v
				.findViewById(R.id.notification_shield_text_textview);

	}

	private NotificationEventHandler notificationEventHandler = new NotificationEventHandler() {

		@Override
		public void onNotificationReceive(final String notificationText) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						notificationTextTextView.setText(notificationText);
					}
				});
			}

		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new NotificationShield(activity, getControllerTag()));
		((NotificationShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setNotificationEventHandler(notificationEventHandler);
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
			getApplication().getAppFirmata().initUart();
			toggleMenuButtons();
			return true;
		case R.id.disable_serial_menuitem:
			getApplication().getAppFirmata().disableUart();
			toggleMenuButtons();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleMenuButtons() {
		if (getApplication().getAppFirmata() == null)
			return;
		if (getApplication().getAppFirmata().isUartInit()) {
			if (disableSerialMenuItem != null)
				disableSerialMenuItem.setVisible(true);
			if (enableSerialMenuItem != null)
				enableSerialMenuItem.setVisible(false);
		} else {
			if (disableSerialMenuItem != null)
				disableSerialMenuItem.setVisible(false);
			if (enableSerialMenuItem != null)
				enableSerialMenuItem.setVisible(true);
		}
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

}
