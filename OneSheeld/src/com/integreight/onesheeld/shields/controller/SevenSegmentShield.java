package com.integreight.onesheeld.shields.controller;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.utils.ControllerParent;

public class SevenSegmentShield extends ControllerParent<SevenSegmentShield> {
	private SevenSegmentsEventHandler eventHandler;
	private Map<Segment, Boolean> segmentsStatus;
	private Map<Segment, Integer> segmentsConnectedPins;

	public SevenSegmentShield() {
		super();
	}

	public SevenSegmentShield(Activity activity, String tag) {
		super(activity, tag);
		segmentsStatus = new HashMap<Segment, Boolean>();
		segmentsConnectedPins = new HashMap<Segment, Integer>();
		for (Segment segment : Segment.values()) {
			segmentsStatus.put(segment, false);
			segmentsConnectedPins.put(segment, null);
		}
	}

	@Override
	public ControllerParent<SevenSegmentShield> setTag(String tag) {
		segmentsStatus = new HashMap<Segment, Boolean>();
		segmentsConnectedPins = new HashMap<Segment, Integer>();
		for (Segment segment : Segment.values()) {
			segmentsStatus.put(segment, false);
			segmentsConnectedPins.put(segment, null);
		}
		return super.setTag(tag);
	}

	public void connectSegmentWithPin(Segment segment, int pin) {
		segmentsConnectedPins.put(segment, pin);
		CommitInstanceTotable();
	}

	public Map<Segment, Boolean> getSegmentsStatus() {
		return segmentsStatus;
	}

	public Map<Segment, Boolean> refreshSegments() {

		updateSegmentsStatusFromFirmata();
		return segmentsStatus;
	}

	@Override
	public void onDigital(int portNumber, int portData) {
		updateSegmentsStatusFromFirmata();
		if (eventHandler != null) {
			eventHandler.onSegmentsChange(segmentsStatus);
		}
		CommitInstanceTotable();
		super.onDigital(portNumber, portData);
	}

	public void setSevenSegmentsEventHandler(
			SevenSegmentsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		for (Integer connectedPin : this.segmentsConnectedPins.values()) {
			if (connectedPin != null)
				getApplication().getAppFirmata().pinMode(connectedPin,
						ArduinoFirmata.INPUT);
		}
		updateSegmentsStatusFromFirmata();
	}

	public static interface SevenSegmentsEventHandler {
		void onSegmentsChange(Map<Segment, Boolean> segmentsStatus);
	}

	private void updateSegmentsStatusFromFirmata() {
		for (Segment segment : Segment.values()) {
			Integer connectedPin = segmentsConnectedPins.get(segment);
			if (connectedPin != null)
				segmentsStatus.put(segment, getApplication().getAppFirmata()
						.digitalRead(connectedPin));
		}
		CommitInstanceTotable();
	}

	public static enum Segment {
		A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G"), DOT("DOT");

		String name;

		Segment(String name) {
			this.name = name;
		}

		String getName() {
			return name;
		}

		public static CharSequence[] getSegmentsNames() {
			CharSequence[] temp = new CharSequence[Segment.values().length];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = Segment.values()[i].getName();
			}
			return temp;
		}

		public static Segment getSegment(int position) {
			switch (position) {
			case 0:
				return A;
			case 1:
				return B;
			case 2:
				return C;
			case 3:
				return D;
			case 4:
				return E;
			case 5:
				return F;
			case 6:
				return G;
			case 7:
				return DOT;
			default:
				return null;
			}

		}
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
