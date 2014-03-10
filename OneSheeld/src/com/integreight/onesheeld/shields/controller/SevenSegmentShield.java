package com.integreight.onesheeld.shields.controller;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.utils.BitsUtils;
import com.integreight.onesheeld.utils.ControllerParent;

public class SevenSegmentShield extends ControllerParent<SevenSegmentShield> {
	private SevenSegmentsEventHandler eventHandler;
	public Hashtable<String, Boolean> pinsStatus = new Hashtable<String, Boolean>();

	public SevenSegmentShield() {
		super();
		requiredPinsIndex = 0;
		shieldPins = new String[] { "  A  ", "  B  ", "  C  ", "  D  ",
				"  E  ", "  F  ", "  G  ", " DOT " };
		for (int i = 0; i < shieldPins.length; i++) {
			pinsStatus.put(shieldPins[i], false);
		}
	}

	public SevenSegmentShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<SevenSegmentShield> setTag(String tag) {
		return super.setTag(tag);
	}

	public Hashtable<String, Boolean> getSegmentsStatus() {
		return pinsStatus;
	}

	public Hashtable<String, Boolean> refreshSegments() {

		updateSegmentsStatusFromFirmata();
		return pinsStatus;
	}

	@Override
	public void onDigital(int portNumber, int portData) {
		updateSegmentsStatusFromFirmata();
		if (eventHandler != null) {
			eventHandler.onSegmentsChange(pinsStatus);
		}
		CommitInstanceTotable();
		super.onDigital(portNumber, portData);
	}

	public void setSevenSegmentsEventHandler(
			SevenSegmentsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		updateSegmentsStatusFromFirmata();
	}

	public static interface SevenSegmentsEventHandler {
		void onSegmentsChange(Hashtable<String, Boolean> segmentsStatus);
	}

	private synchronized void updateSegmentsStatusFromFirmata() {
		for (Entry<String, ArduinoPin> entry : matchedShieldPins.entrySet()) {
			pinsStatus.put(entry.getKey(), getApplication().getAppFirmata()
					.digitalRead(entry.getValue().microHardwarePin));
		}
		CommitInstanceTotable();
	}

	private synchronized String getSegment(int pinId) {
		for (Entry<String, ArduinoPin> entry : matchedShieldPins.entrySet()) {

			// THIS IS THE IMPORTANT LINE
			if (entry.getValue().microHardwarePin == pinId) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		for (int i = 0; i < shieldPins.length; i++) {
			// pinsStatus.put(shieldPins[i], BitsUtils.)
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
