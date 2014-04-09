package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.text.Editable;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.fragments.KeyboardFragment.KeyboardEventHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class KeyboardShield extends ControllerParent<KeyboardShield> {
	private ShieldFrame frame;
	private static final byte KEYBOARD_VALUE = (byte) 0x01;

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
		public void onKeyPressed(String myText) {

			if (isNotNullNotEmpty(myText)) {
				for (int i = 0; i < myText.length(); i++) {

					frame = new ShieldFrame(UIShield.KEYBOARD_SHIELD.getId(),
							KEYBOARD_VALUE);
					frame.addCharArgument(myText.charAt(0));
					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);

				}
			}
		}

		@Override
		public void onEnterOrbspacepressed(char myChar) {
			frame = new ShieldFrame(UIShield.KEYBOARD_SHIELD.getId(),
					KEYBOARD_VALUE);
			frame.addCharArgument(myChar);
			activity.getThisApplication().getAppFirmata()
					.sendShieldFrame(frame);

		}
	};

	public static boolean isNotNullNotEmpty(final String string) {
		return string != null && !string.isEmpty();
	}

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
		frame = null;
	}

}
