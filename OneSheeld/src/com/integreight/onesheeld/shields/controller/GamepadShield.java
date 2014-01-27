package com.integreight.onesheeld.shields.controller;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.utils.ControllerParent;

public class GamepadShield extends ControllerParent<GamepadShield> {
	private Map<Pin, Integer> connectedPins;

	// private static final char GAMEPAD_COMMAND = (byte) 0x37;
	// private static final char DATA_IN = (byte) 0x01;

	// private static final char NOTHING_PRESSED = (char) 0xFF;

	public GamepadShield(Activity activity, String tag) {
		super(activity, tag);
		getApplication().getAppFirmata().initUart();
		connectedPins = new HashMap<Pin, Integer>();
		for (Pin pin : Pin.values()) {
			connectedPins.put(pin, null);
		}
	}
	@Override
	public ControllerParent<GamepadShield> setTag(String tag) {
		getApplication().getAppFirmata().initUart();
		connectedPins = new HashMap<Pin, Integer>();
		for (Pin pin : Pin.values()) {
			connectedPins.put(pin, null);
		}
		return super.setTag(tag);
	}

	public GamepadShield() {
		super();
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

	public void connectGamepadPinWithArduinoPin(Pin gPin, int pin) {
		connectedPins.put(gPin, pin);
		CommitInstanceTotable();
	}

	public void setPinToHigh(int pinId) {
		if (connectedPins != null
				&& connectedPins.containsKey(Pin.getPin(pinId))
				&& connectedPins.get(Pin.getPin(pinId)) != null) {
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getPin(pinId)), ArduinoFirmata.HIGH);
		}
		CommitInstanceTotable();
		// firmata.sendUart(KEYPAD_COMMAND,DATA_IN,new char[]{row,column});
	}

	public void setPinToLow(int pinId) {
		if (connectedPins != null
				&& connectedPins.containsKey(Pin.getPin(pinId))
				&& connectedPins.get(Pin.getPin(pinId)) != null) {
			getApplication().getAppFirmata().digitalWrite(
					connectedPins.get(Pin.getPin(pinId)), ArduinoFirmata.LOW);
		}
		CommitInstanceTotable();
		// firmata.sendUart(KEYPAD_COMMAND,DATA_IN,new
		// char[]{NOTHING_PRESSED,NOTHING_PRESSED});
	}

	public static enum Pin {
		UP_ARROW(0, "Up Arrow"), RIGHT_ARROW(1, "Right Arrow"), DOWN_ARROW(2,
				"Down Arrow"), LEFT_ARROW(3, "Left Arrow"), YELLOW_BUTTON(4,
				"Yellow Button"), RED_BUTTON(5, "Red Button"), GREEN_BUTTON(6,
				"Green Button"), BLUE_BUTTON(7, "Blue Button");

		String name;
		int id;

		Pin(int id, String name) {
			this.id = id;
			this.name = name;
		}

		String getName() {
			return name;
		}

		public int getId() {
			return id;
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
				return UP_ARROW;
			case 1:
				return RIGHT_ARROW;
			case 2:
				return DOWN_ARROW;
			case 3:
				return LEFT_ARROW;
			case 4:
				return YELLOW_BUTTON;
			case 5:
				return RED_BUTTON;
			case 6:
				return GREEN_BUTTON;
			case 7:
				return BLUE_BUTTON;
			default:
				return null;
			}

		}

	}
}
