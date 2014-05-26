package com.integreight.onesheeld.shields.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LcdShield.LcdEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class LcdFragmentPre extends ShieldFragmentParent<LcdFragmentPre> {

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
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

	}

	private LcdEventHandler lcdEventHandler = new LcdEventHandler() {

		@Override
		public void onLcdError(final String error) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void setCursor(int x, int y) {
			// TODO Auto-generated method stub

		}

		@Override
		public void write(char ch) {
			// TODO Auto-generated method stub

		}

		@Override
		public void blink() {
			// TODO Auto-generated method stub

		}

		@Override
		public void noBlink() {
			// TODO Auto-generated method stub

		}

		@Override
		public void cursor() {
			// TODO Auto-generated method stub

		}

		@Override
		public void noCursor() {
			// TODO Auto-generated method stub

		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

		@Override
		public void scrollDisplayLeft() {
			// TODO Auto-generated method stub

		}

		@Override
		public void scrollDisplatRight() {
			// TODO Auto-generated method stub

		}
	};

	private void initializeFirmata() {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new LcdShield(getActivity(), getControllerTag()));
		((LcdShield) getApplication().getRunningShields().get(
				getControllerTag())).setLcdEventHandler(lcdEventHandler);
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

}
