package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.KeypadShield;
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.KeypadShield.Pin;
import com.integreight.onesheeld.utils.Key;
import com.integreight.onesheeld.utils.Key.KeyTouchEventListener;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class KeypadFragment extends ShieldFragmentParent<KeypadFragment> {

	Button connectButton;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;

	KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

		@Override
		public void onReleased(Key k) {
			// TODO Auto-generated method stub
			((KeypadShield) getApplication().getRunningShields().get(
					getControllerTag())).resetRowAndColumn((char) k.getRow(),
					(char) k.getColumn());

		}

		@Override
		public void onPressed(Key k) {
			// TODO Auto-generated method stub
			((KeypadShield) getApplication().getRunningShields().get(
					getControllerTag())).setRowAndColumn((char) k.getRow(),
					(char) k.getColumn());

		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.keypad_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		ConnectingPinsView.getInstance().reset(
				getApplication().getRunningShields().get(getControllerTag()),
				new OnPinSelectionListener() {

					@Override
					public void onSelect(ArduinoPin pin) {
						if (pin != null) {
							((KeypadShield) getApplication()
									.getRunningShields()
									.get(getControllerTag()))
									.setConnected(new ArduinoConnectedPin(
											pin.microHardwarePin,
											ArduinoFirmata.OUTPUT));
							((KeypadShield) getApplication()
									.getRunningShields()
									.get(getControllerTag()))
									.connectKeypadPinWithArduinoPin(
											Pin.getPin(pin.microHardwarePin),
											pin.microHardwarePin);
						}

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
		initializeKeysEventHandler((ViewGroup) getView());

	}

	private void initializeKeysEventHandler(ViewGroup viewGroup) {
		ViewGroup keypad = (ViewGroup) ((ViewGroup) ((ViewGroup) viewGroup
				.getChildAt(0)).getChildAt(1)).getChildAt(0);
		for (int i = 0; i < keypad.getChildCount(); i++) {
			ViewGroup keypadRow = (ViewGroup) keypad.getChildAt(i);
			for (int j = 0; j < keypadRow.getChildCount(); j++) {
				View key = keypadRow.getChildAt(j);
				if (key instanceof Key) {
					((Key) key).setEventListener(touchEventListener);
				}

			}

		}
	}

	private void initializeFirmata() {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new KeypadShield(getActivity(), getControllerTag()));
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
