package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.shields.controller.GamepadShield.Pin;
import com.integreight.onesheeld.utils.Key;
import com.integreight.onesheeld.utils.Key.KeyTouchEventListener;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;

public class GamepadFragment extends ShieldFragmentParent<GamepadFragment> {

	MenuItem connectButton;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;

	private Key upArrowKey;
	private Key downArrowKey;
	private Key leftArrowKey;
	private Key rightArrowKey;
	private Key yellowArrowKey;
	private Key greenArrowKey;
	private Key blueArrowKey;
	private Key redArrowKey;

	KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

		@Override
		public void onPressed(Key k) {
			// TODO Auto-generated method stub
			switch (k.getId()) {
			case R.id.gamepad_up_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.UP_ARROW.getName(), Pin.UP_ARROW.getId());
				break;
			case R.id.gamepad_down_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.DOWN_ARROW.getName(), Pin.DOWN_ARROW.getId());
				break;
			case R.id.gamepad_right_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.RIGHT_ARROW.getName(), Pin.RIGHT_ARROW.getId());
				break;

			case R.id.gamepad_left_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.LEFT_ARROW.getName(), Pin.LEFT_ARROW.getId());
				break;

			case R.id.gamepad_yellow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.YELLOW_BUTTON.getName(), Pin.YELLOW_BUTTON.getId());
				break;
			case R.id.gamepad_green_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.GREEN_BUTTON.getName(), Pin.GREEN_BUTTON.getId());
				break;
			case R.id.gamepad_red_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.RED_BUTTON.getName(), Pin.RED_BUTTON.getId());
				break;
			case R.id.gamepad_blue_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToHigh(
						Pin.BLUE_BUTTON.getName(), Pin.BLUE_BUTTON.getId());
				break;

			default:
				break;
			}

		}

		@Override
		public void onReleased(Key k) {
			// TODO Auto-generated method stub
			switch (k.getId()) {
			case R.id.gamepad_up_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.UP_ARROW.getName(), Pin.UP_ARROW.getId());
				break;
			case R.id.gamepad_down_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.DOWN_ARROW.getName(), Pin.DOWN_ARROW.getId());
				break;
			case R.id.gamepad_right_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.RIGHT_ARROW.getName(), Pin.RIGHT_ARROW.getId());
				break;

			case R.id.gamepad_left_arrow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.LEFT_ARROW.getName(), Pin.LEFT_ARROW.getId());
				break;

			case R.id.gamepad_yellow_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.YELLOW_BUTTON.getName(), Pin.YELLOW_BUTTON.getId());
				break;
			case R.id.gamepad_green_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.GREEN_BUTTON.getName(), Pin.GREEN_BUTTON.getId());
				break;
			case R.id.gamepad_red_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.RED_BUTTON.getName(), Pin.RED_BUTTON.getId());
				break;
			case R.id.gamepad_blue_key:
				((GamepadShield) getApplication().getRunningShields().get(
						getControllerTag())).setPinToLow(
						Pin.BLUE_BUTTON.getName(), Pin.BLUE_BUTTON.getId());
				break;

			default:
				break;
			}

		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.gamepad_shield_fragment_layout,
				container, false);
		upArrowKey = (Key) v.findViewById(R.id.gamepad_up_arrow_key);
		downArrowKey = (Key) v.findViewById(R.id.gamepad_down_arrow_key);
		leftArrowKey = (Key) v.findViewById(R.id.gamepad_left_arrow_key);
		rightArrowKey = (Key) v.findViewById(R.id.gamepad_right_arrow_key);
		yellowArrowKey = (Key) v.findViewById(R.id.gamepad_yellow_key);
		redArrowKey = (Key) v.findViewById(R.id.gamepad_red_key);
		greenArrowKey = (Key) v.findViewById(R.id.gamepad_green_key);
		blueArrowKey = (Key) v.findViewById(R.id.gamepad_blue_key);

		upArrowKey.setEventListener(touchEventListener);
		downArrowKey.setEventListener(touchEventListener);
		leftArrowKey.setEventListener(touchEventListener);
		rightArrowKey.setEventListener(touchEventListener);
		yellowArrowKey.setEventListener(touchEventListener);
		redArrowKey.setEventListener(touchEventListener);
		greenArrowKey.setEventListener(touchEventListener);
		blueArrowKey.setEventListener(touchEventListener);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		ConnectingPinsView.getInstance().reset(
				getApplication().getRunningShields().get(getControllerTag()),
				new OnPinSelectionListener() {

					@Override
					public void onSelect(ArduinoPin pin) {
						if (pin != null) {
							((GamepadShield) getApplication()
									.getRunningShields()
									.get(getControllerTag()))
									.setConnected(new ArduinoConnectedPin(
											pin.microHardwarePin,
											ArduinoFirmata.OUTPUT));
						}

					}
				});
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
		initializeKeysEventHandler((ViewGroup) getView());

	}

	private void initializeKeysEventHandler(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			ViewGroup keypadRow = (ViewGroup) viewGroup.getChildAt(i);
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
					new GamepadShield(getActivity(), getControllerTag()));
		toggleMenuButtons();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.pin_shields_menu, menu);
		connectButton = (MenuItem) menu
				.findItem(R.id.connect_shield_pins_menuitem);
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
		case R.id.connect_shield_pins_menuitem:
			showPinsSelectionMenus();
			return true;
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

	private void showPinsSelectionMenus() {

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
