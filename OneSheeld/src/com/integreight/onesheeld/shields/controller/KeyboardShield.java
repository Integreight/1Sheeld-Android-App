package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.text.Editable;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.fragments.KeyboardFragment.KeyboardEventHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class KeyboardShield extends ControllerParent<KeyboardShield> {
	private ShieldFrame frame;
	private static final byte CLOCK_VALUE = (byte) 0x01;

	@Override
	public ControllerParent<KeyboardShield> setTag(String tag) {
		// TODO Auto-generated method stub

		return super.setTag(tag);
	}

	public KeyboardShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public KeyboardShield() {
		super();
	}

	private KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler() {

		@Override
		public void onDonePressed(Editable myText) {
			if (myText.length() > 0) {
				for (int i = 0; i < myText.length(); i++) {

					frame = new ShieldFrame(UIShield.KEYBOARD_SHIELD.getId(),
							CLOCK_VALUE);
					frame.addCharArgument(myText.charAt(i));
					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);

				}
			}
		}
	};

	public KeyboardEventHandler getKeyboardEventHandler() {
		return keyboardEventHandler;
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		keyboardEventHandler = null;
	}

}
