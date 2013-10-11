package com.integreight.onesheeld.shields;

import java.util.HashMap;
import java.util.Map;

import com.integreight.firmatabluetooth.ArduinoFirmata;

public class KeypadShield {
	private ArduinoFirmata firmata;
	private Map<Pin, Integer> connectedPins;
	private static final char KEYPAD_COMMAND = (byte) 0x33;
	private static final char DATA_IN = (byte) 0x01;
	
	private static final char NOTHING_PRESSED = (char) 0xFF;

	public KeypadShield(ArduinoFirmata firmata) {
		this.firmata = firmata;
		
		firmata.initUart();
		connectedPins = new HashMap<Pin, Integer>();
		for (Pin pin : Pin.values()) {
			connectedPins.put(pin, null);
		}
	}

	public void initPins(){
		if(connectedPins==null)return;
		for (Integer connectedPin : this.connectedPins.values()) {
			if (connectedPin != null)
				firmata.pinMode(connectedPin, ArduinoFirmata.OUTPUT);
		}
	}
	
	public void connectKeypadPinWithArduinoPin(Pin segment, int pin) {
		connectedPins.put(segment, pin);
	}

	public void setRowAndColumn(char row, char column) {
		if (!connectedPins.containsKey(Pin.getRow(row))
				|| !connectedPins.containsKey(Pin.getColumn(column))) {
			firmata.digitalWrite(connectedPins.get(Pin.getRow(row)),
					ArduinoFirmata.HIGH);
			firmata.digitalWrite(connectedPins.get(Pin.getColumn(column)),
					ArduinoFirmata.HIGH);
		}
		firmata.sendUart(KEYPAD_COMMAND,DATA_IN,new char[]{row,column});
	}

	public void resetRowAndColumn(int row, int column) {
		if (!connectedPins.containsKey(Pin.getRow(row))
				|| !connectedPins.containsKey(Pin.getColumn(column))) {
			firmata.digitalWrite(connectedPins.get(Pin.getRow(row)),
					ArduinoFirmata.LOW);
			firmata.digitalWrite(connectedPins.get(Pin.getColumn(column)),
					ArduinoFirmata.LOW);
		}
		firmata.sendUart(KEYPAD_COMMAND,DATA_IN,new char[]{NOTHING_PRESSED,NOTHING_PRESSED});
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
}
