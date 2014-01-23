package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SliderShield;
import com.integreight.onesheeld.shields.controller.SmsShield;
import com.integreight.onesheeld.shields.controller.SmsShield.SmsEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class SmsFragment extends ShieldFragmentParent<SmsFragment> {

	TextView smsTextTextView;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;

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
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		smsTextTextView = (TextView) getView().findViewById(
				R.id.sms_shield_text_textview);

	}

	private SmsEventHandler smsEventHandler = new SmsEventHandler() {

		@Override
		public void onSmsSent(String smsNumber, String smsText) {
			// TODO Auto-generated method stub
			smsTextTextView.setText(smsText);
			Toast.makeText(getActivity(), "SMS Sent!", Toast.LENGTH_LONG)
					.show();

		}

		@Override
		public void onSmsFail(String error) {
			// TODO Auto-generated method stub
			Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();

		}
	};

	private void initializeFirmata() {
		if ((getApplication().getRunningSheelds().get(getControllerTag())) == null)
			getApplication().getRunningSheelds().put(getControllerTag(),
					new SmsShield(getActivity(), getControllerTag()));
		((SmsShield) getApplication().getRunningSheelds().get(
				getControllerTag())).setSmsEventHandler(smsEventHandler);
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
