package com.integreight.onesheeld.shields.controller;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class KeypadShield extends ControllerParent<KeypadShield> {
	private Map<Pin, Integer> connectedPins;
	private static final char KEYPAD_COMMAND = (byte) 0x33;
	private static final char DATA_IN = (byte) 0x01;

	private static final char NOTHING_PRESSED = (char) 0xFF;

	public KeypadShield(Activity activity, String tag) {
		super(activity, tag);
		connectedPins = new HashMap<Pin, Integer>();
		for (Pin pin : Pin.values()) {
			connectedPins.put(pin, null);
		}
	}

	@Override
	public ControllerParent<KeypadShield> setTag(String tag) {
		connectedPins = new HashMap<Pin, Integer>();
		for (Pin pin : Pin.values()) {
			connectedPins.put(pin, null);
		}
		return super.setTag(tag);
	}

	public KeypadShield() {
		super();
		requiredPinsIndex = 0;
		shieldPins = new String[] { "Row 0", "Row 1", "Row 2", "Row 3",
				"Column 0", "Column 1", "Column 2", "Column 3" };
	}
	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		// TODO Auto-generated method stub
		super.setConnected(pins);
	}
	public void initPins() {
		if (connectedPins == null)
			return;
		for (Integer connectedPin : this.connectedPins.values()) {
			if (connectedPin != null)
				getApplication().getAppFirmata().pinMode(connectedPin,
						ArduinoFirmata.OUTPUT);
		}
		CommitInstanceTotable();
	}

	public void connectKeypadPinWithArduinoPin(Pin segment, int pin) {
		connectedPins.put(segment, pin);
		CommitInstanceTotable();
	}

	public void setRowAndColumn(char row, char column) {
		if (connectedPins != null && connectedPins.containsKey(Pin.getRow(row))
				&& connectedPins.get(Pin.getRow(row)) != null
				&& connectedPins.containsKey(Pin.getColumn(column))
				&& connectedPins.get(Pin.getColumn(column)) != null) {
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getRow(row)), ArduinoFirmata.HIGH);
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getColumn(column)),
					ArduinoFirmata.HIGH);
		}
		// getApplication().getAppFirmata().sendUart(KEYPAD_COMMAND, DATA_IN,
		// new char[] { row, column });
		CommitInstanceTotable();
	}

	public void resetRowAndColumn(int row, int column) {
		if (connectedPins != null && connectedPins.containsKey(Pin.getRow(row))
				&& connectedPins.get(Pin.getRow(row)) != null
				&& connectedPins.containsKey(Pin.getColumn(column))
				&& connectedPins.get(Pin.getColumn(column)) != null) {
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getRow(row)), ArduinoFirmata.LOW);
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getColumn(column)),
					ArduinoFirmata.LOW);
		}
		// getApplication().getAppFirmata().sendUart(KEYPAD_COMMAND, DATA_IN,
		// new char[] { NOTHING_PRESSED, NOTHING_PRESSED });
		CommitInstanceTotable();
	}

	public static enum Pin {
		ROW0("Row 0"), ROW1("Row 1"), ROW2("Row 2"), ROW3("Row 3"), COLUMN0(
				"Column 0"), COLUMN1("Column 1"), COLUMN2("Column 2"), COLUMN3(
				"Column 3");

		String name;

		Pin(String name) {
			this.name = name;
		}

		String getName() {
			return name;
		}

		public static CharSequence[] getPinsNames() {
			CharSequence[] temp = new CharSequence[Pin.values().length];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = Pin.values()[i].getName();
			}
			return temp;
		}

		public static Pin getPin(int position) {
			switch (position) {
			case 0:
				return ROW0;
			case 1:
				return ROW1;
			case 2:
				return ROW2;
			case 3:
				return ROW3;
			case 4:
				return COLUMN0;
			case 5:
				return COLUMN1;
			case 6:
				return COLUMN2;
			case 7:
				return COLUMN3;
			default:
				return null;
			}

		}

		public static Pin getRow(int position) {
			switch (position) {
			case 0:
				return ROW0;
			case 1:
				return ROW1;
			case 2:
				return ROW2;
			case 3:
				return ROW3;
			default:
				return null;
			}
		}

		public static Pin getColumn(int position) {
			switch (position) {
			case 0:
				return COLUMN0;
			case 1:
				return COLUMN1;
			case 2:
				return COLUMN2;
			case 3:
				return COLUMN3;
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
