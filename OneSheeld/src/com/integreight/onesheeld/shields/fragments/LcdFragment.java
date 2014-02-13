package com.integreight.onesheeld.shields.fragments;

import android.graphics.Typeface;
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
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LcdShield.LcdEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class LcdFragment extends ShieldFragmentParent<LcdFragment> {

	TextView lcdTextView;
	MenuItem clearLcdMenuItem;
	MenuItem enableSerialMenuItem;
	MenuItem disableSerialMenuItem;

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
		lcdTextView = (TextView) getView().findViewById(
				R.id.lcd_shield_text_textview);
		Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
				"lcd_font.ttf");
		lcdTextView.setTypeface(font);

	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

		@Override
		public void onTextChange(String[] text) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				lcdTextView.setText(extractTextFromLcdShield());
			// Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onLcdError(String error) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
		}
	};

	private void initializeFirmata() {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new LcdShield(getActivity(), getControllerTag()));
		((LcdShield) getApplication().getRunningShields().get(
				getControllerTag())).setLcdEventHandler(lcdEventHandler);
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
			getApplication().getAppFirmata().initUart();
			toggleMenuButtons();
			return true;
		case R.id.disable_serial_menuitem:
			getApplication().getAppFirmata().disableUart();
			toggleMenuButtons();
			return true;
		case R.id.clear_lcd_menuitem:
			clearLcd();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void clearLcd() {
		((LcdShield) getApplication().getRunningShields().get(
				getControllerTag())).resetPins();
		lcdTextView.setText(extractTextFromLcdShield());
	}

	private String extractTextFromLcdShield() {
		String text = "";
		for (int i = 0; i < ((LcdShield) getApplication().getRunningShields()
				.get(getControllerTag())).getLcdText().length; i++) {
			text += ((LcdShield) getApplication().getRunningShields().get(
					getControllerTag())).getLcdText()[i];
			if (i == ((LcdShield) getApplication().getRunningShields().get(
					getControllerTag())).getLcdText().length - 1)
				break;
			text += "\n";
		}
		return text;
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
