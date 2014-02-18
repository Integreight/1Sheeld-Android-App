package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.KeypadShield;
import com.integreight.onesheeld.shields.controller.KeypadShield.Pin;
import com.integreight.onesheeld.utils.Key;
import com.integreight.onesheeld.utils.Key.KeyTouchEventListener;
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

		connectButton = (Button) getView().findViewById(
				R.id.keypad_fragment_connect_button);

		final CharSequence[] arduinoPins = { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3",
				"A4", "A5" };

		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				final AlertDialog.Builder builder3 = new AlertDialog.Builder(
						getActivity());
				builder3.setTitle("Choose pin to connect").setItems(
						Pin.getPinsNames(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									final int whichSegment) {

								// TODO Auto-generated method stub
								AlertDialog.Builder builder2 = new AlertDialog.Builder(
										getActivity());
								builder2.setTitle("Connect With").setItems(
										arduinoPins,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int whichArduinoPin) {
												((KeypadShield) getApplication()
														.getRunningShields()
														.get(getControllerTag()))
														.connectKeypadPinWithArduinoPin(
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
						((KeypadShield) getApplication().getRunningShields()
								.get(getControllerTag())).initPins();

					}
				});
				builder3.show();

			}

		});

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
