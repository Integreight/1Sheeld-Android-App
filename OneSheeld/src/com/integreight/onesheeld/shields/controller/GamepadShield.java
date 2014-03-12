package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.BitsUtils;
import com.integreight.onesheeld.utils.ControllerParent;

public class GamepadShield extends ControllerParent<GamepadShield> {
	ShieldFrame sf;
	byte buttonByte = 0;
	private static final byte DATA_IN = 0x01;

	public GamepadShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<GamepadShield> setTag(String tag) {
		return super.setTag(tag);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		// TODO Auto-generated method stub
		super.setConnected(pins);
	}

	public void initPins() {
		CommitInstanceTotable();
	}

	public GamepadShield() {
		super();
		requiredPinsIndex = 0;
		shieldPins = new String[] { "Up Arrow", "Right Arrow", "Down Arrow",
				"Left Arrow", "Yellow Button", "Red Button", "Green Button",
				"Blue Button" };
	}

	public void setPinToHigh(String pinName, int pinId) {
		ArduinoPin columnPincolumnPin = matchedShieldPins.get(pinName);
		if (columnPincolumnPin != null) {
			getApplication().getAppFirmata().digitalWrite(
					columnPincolumnPin.microHardwarePin, ArduinoFirmata.HIGH);
			buttonByte = BitsUtils.setBit(buttonByte, pinId);
		}
		sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
		sf.addByteArgument(buttonByte);
		getApplication().getAppFirmata().sendShieldFrame(sf);
		CommitInstanceTotable();
		// firmata.sendUart(KEYPAD_COMMAND,DATA_IN,new char[]{row,column});
	}

	public void setPinToLow(String pinName, int pinId) {
		ArduinoPin columnPin = matchedShieldPins.get(pinName);

		if (columnPin != null) {
			getApplication().getAppFirmata().digitalWrite(
					columnPin.microHardwarePin, ArduinoFirmata.LOW);
			buttonByte = BitsUtils.setBit(buttonByte, pinId);
		}
		sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
		sf.addByteArgument(buttonByte);
		getApplication().getAppFirmata().sendShieldFrame(sf);
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

		public String getName() {
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

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
