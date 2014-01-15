package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.ShieldsOperationActivity;
import com.integreight.onesheeld.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.model.Key;
import com.integreight.onesheeld.model.Key.KeyTouchEventListener;
import com.integreight.onesheeld.shields.controller.KeypadShield;
import com.integreight.onesheeld.shields.controller.KeypadShield.Pin;


public class KeypadFragment extends SherlockFragment {

	KeypadShield keypad;
	ShieldsOperationActivity activity;
	Button connectButton;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	ArduinoFirmata firmata;
	
	KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

		@Override
		public void onReleased(Key k) {
			// TODO Auto-generated method stub
			keypad.resetRowAndColumn((char)k.getRow(), (char)k.getColumn());

		}

		@Override
		public void onPressed(Key k) {
			// TODO Auto-generated method stub
			keypad.setRowAndColumn((char)k.getRow(), (char)k.getColumn());

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
												keypad.connectKeypadPinWithArduinoPin(Pin.getPin(whichSegment), whichArduinoPin);
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
						keypad.initPins();

					}
				});
				builder3.show();

			}

		});
		
		activity = (ShieldsOperationActivity) getActivity();
		
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
		if (keypad != null)
			return;
		this.firmata = firmata;
		keypad = new KeypadShield(firmata);
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
			if(disableSerialMenuItem!=null)disableSerialMenuItem.setVisible(true);
			if(enableSerialMenuItem!=null)enableSerialMenuItem.setVisible(false);
		} else {
			if(disableSerialMenuItem!=null)disableSerialMenuItem.setVisible(false);
			if(enableSerialMenuItem!=null)enableSerialMenuItem.setVisible(true);
		}
	}

}
