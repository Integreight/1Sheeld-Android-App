package com.integreight.onesheeld.shieldsfragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.LcdShield;
import com.integreight.onesheeld.shields.LcdShield.LcdEventHandler;

public class LcdFragment extends SherlockFragment {

	LcdShield lcdShield;
	ShieldsOperationActivity activity;
	TextView lcdTextView;

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
		Typeface font = Typeface.createFromAsset(activity.getAssets(), "lcd_font.ttf");  
		lcdTextView.setTypeface(font); 

	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {
		
		@Override
		public void onTextChange(String[] text) {
			// TODO Auto-generated method stub
			lcdTextView.setText(text[0]+"\n"+text[1]);
			//Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
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

		lcdShield = new LcdShield(firmata, activity);
		lcdShield.setLcdEventHandler(lcdEventHandler);
	}

}
