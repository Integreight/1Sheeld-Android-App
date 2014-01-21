package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.shields.controller.GamepadShield.Pin;
import com.integreight.onesheeld.utils.Key;
import com.integreight.onesheeld.utils.Key.KeyTouchEventListener;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class GamepadFragment extends ShieldFragmentParent<GamepadFragment> {

	GamepadShield gamepad;
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
				gamepad.setPinToHigh(Pin.UP_ARROW.getId());
				break;
			case R.id.gamepad_down_arrow_key:
				gamepad.setPinToHigh(Pin.DOWN_ARROW.getId());
				break;
			case R.id.gamepad_right_arrow_key:
				gamepad.setPinToHigh(Pin.RIGHT_ARROW.getId());
				break;
			case R.id.gamepad_left_arrow_key:
				gamepad.setPinToHigh(Pin.LEFT_ARROW.getId());
				break;
			case R.id.gamepad_yellow_key:
				gamepad.setPinToHigh(Pin.YELLOW_BUTTON.getId());
				break;
			case R.id.gamepad_green_key:
				gamepad.setPinToHigh(Pin.GREEN_BUTTON.getId());
				break;
			case R.id.gamepad_red_key:
				gamepad.setPinToHigh(Pin.RED_BUTTON.getId());
				break;
			case R.id.gamepad_blue_key:
				gamepad.setPinToHigh(Pin.BLUE_BUTTON.getId());
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
				gamepad.setPinToLow(Pin.UP_ARROW.getId());
				break;
			case R.id.gamepad_down_arrow_key:
				gamepad.setPinToLow(Pin.DOWN_ARROW.getId());
				break;
			case R.id.gamepad_right_arrow_key:
				gamepad.setPinToLow(Pin.RIGHT_ARROW.getId());
				break;
			case R.id.gamepad_left_arrow_key:
				gamepad.setPinToLow(Pin.LEFT_ARROW.getId());
				break;
			case R.id.gamepad_yellow_key:
				gamepad.setPinToLow(Pin.YELLOW_BUTTON.getId());
				break;
			case R.id.gamepad_green_key:
				gamepad.setPinToLow(Pin.GREEN_BUTTON.getId());
				break;
			case R.id.gamepad_red_key:
				gamepad.setPinToLow(Pin.RED_BUTTON.getId());
				break;
			case R.id.gamepad_blue_key:
				gamepad.setPinToLow(Pin.BLUE_BUTTON.getId());
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
		super.onStart();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initializeKeysEventHandler((ViewGroup) getView());

	}

	private void initializeKeysEventHandler(ViewGroup viewGroup) {
		ViewGroup keypad = (ViewGroup) ((ViewGroup) viewGroup.getChildAt(0))
				.getChildAt(1);
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
		if (gamepad != null)
			return;
		gamepad = new GamepadShield(getApplication().getAppFirmata());
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

		final CharSequence[] arduinoPins = { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3",
				"A4", "A5" };

		final AlertDialog.Builder builder3 = new AlertDialog.Builder(
				getActivity());
		builder3.setTitle("Choose pin to connect").setItems(Pin.getPinsNames(),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							final int whichSegment) {

						// TODO Auto-generated method stub
						AlertDialog.Builder builder2 = new AlertDialog.Builder(
								getActivity());
						builder2.setTitle("Connect With").setItems(arduinoPins,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int whichArduinoPin) {
										gamepad.connectGamepadPinWithArduinoPin(
												Pin.getPin(whichSegment),
												whichArduinoPin);
										builder3.show();
									}

								});

						builder2.show();

					}

				});
		builder3.setPositiveButton("Done!", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				gamepad.initPins();

			}
		});
		builder3.show();

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
