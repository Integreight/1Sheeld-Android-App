package com.integreight.onesheeld.shields.fragments;

import android.graphics.Typeface;
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
import com.integreight.onesheeld.ShieldsOperationActivity;
import com.integreight.onesheeld.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LcdShield.LcdEventHandler;

public class LcdFragment extends SherlockFragment {

	LcdShield lcdShield;
	ShieldsOperationActivity activity;
	TextView lcdTextView;
	MenuItem clearLcdMenuItem;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;
	ArduinoFirmata firmata;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.lcd_shield_fragment_layout,
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
		lcdTextView = (TextView) getView().findViewById(
				R.id.lcd_shield_text_textview);

		activity = (ShieldsOperationActivity) getActivity();
		Typeface font = Typeface.createFromAsset(activity.getAssets(),
				"lcd_font.ttf");
		lcdTextView.setTypeface(font);

	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

		@Override
		public void onTextChange(String[] text) {
			// TODO Auto-generated method stub
			lcdTextView.setText(extractTextFromLcdShield());
			// Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onLcdError(String error) {
			// TODO Auto-generated method stub
			Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
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
		if (lcdShield != null)
			return;
		this.firmata = firmata;
		lcdShield = new LcdShield(firmata, activity);
		lcdShield.setLcdEventHandler(lcdEventHandler);
		toggleMenuButtons();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.lcd_shield_menu, menu);
		clearLcdMenuItem = (MenuItem) menu.findItem(R.id.clear_lcd_menuitem);
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
		case R.id.clear_lcd_menuitem:
			clearLcd();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void clearLcd() {
		lcdShield.reset();
		lcdTextView.setText(extractTextFromLcdShield());
	}

	private String extractTextFromLcdShield() {
		String text = "";
		for (int i = 0; i < lcdShield.getLcdText().length; i++) {
			text += lcdShield.getLcdText()[i];
			if (i == lcdShield.getLcdText().length - 1)
				break;
			text += "\n";
		}
		return text;
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
