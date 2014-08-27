package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SmsShield;
import com.integreight.onesheeld.shields.controller.SmsShield.SmsEventHandler;
import com.integreight.onesheeld.utils.OneShieldTextView;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class SmsFragment extends ShieldFragmentParent<SmsFragment> {

	LinearLayout smsTextContainer;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	Button sendSMS;

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
		smsTextContainer = (LinearLayout) getView().findViewById(
				R.id.sms_shield_text_container);
		// sendSMS = (Button) getView().findViewById(R.id.sendMessage);
		// sendSMS.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// ((SmsShield) getApplication().getRunningShields().get(
		// getControllerTag())).sendSmsToArduino();
		// }
		// });

	}

	private SmsEventHandler smsEventHandler = new SmsEventHandler() {

		@Override
		public void onSmsSent(final String smsNumber, final String smsText) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						OneShieldTextView tv = (OneShieldTextView) activity
								.getLayoutInflater().inflate(
										R.layout.sent_sms_details_row,
										smsTextContainer, false);
						tv.setText("SMS to " + smsNumber + " (" + smsText + ")");
						smsTextContainer.addView(tv);
						Toast.makeText(activity, "SMS Sent!", Toast.LENGTH_LONG)
								.show();
					}
				});
			}

		}

		@Override
		public void onSmsFail(String error) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
			}
		}
	};

	private void initializeFirmata() {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new SmsShield(activity, getControllerTag()));
		((SmsShield) getApplication().getRunningShields().get(
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
