package com.integreight.onesheeld.shields;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class LcdShield {
	private ArduinoFirmata firmata;
	private static LcdEventHandler eventHandler;
	private Activity activity;
	private String lcdText = "";
	private static final byte LCD_COMMAND = (byte) 0x31;

	// Method ids
	private static final byte PRINT = (byte) 0x01;
	// private static final byte BEGIN = (byte) 0x02;
	private static final byte CLEAR = (byte) 0x03;
	private static final byte HOME = (byte) 0x04;
	private static final byte NO_DISPLAY = (byte) 0x05;
	private static final byte DISPLAY = (byte) 0x06;
	// private static final byte NO_BLINK = (byte) 0x07;
	// private static final byte BLINK = (byte) 0x08;
	// private static final byte NO_CURSOR = (byte) 0x09;
	// private static final byte CURSOR = (byte) 0x0A;
	private static final byte SCROLL_DISPLAY_LEFT = (byte) 0x0B;
	private static final byte SCROLL_DISPLAY_RIGHT = (byte) 0x0C;
	private static final byte LEFT_TO_RIGHT = (byte) 0x0D;
	private static final byte RIGHT_TO_LEFT = (byte) 0x0E;
	// private static final byte CREATE_CHAR = (byte) 0x0F;
	private static final byte SET_CURSOR = (byte) 0x10;
	private static final byte WRITE = (byte) 0x11;
	private static final byte NO_AUTO_SCROLL = (byte) 0x12;
	private static final byte AUTO_SCROLL = (byte) 0x13;

	public String getLcdText() {
		return lcdText;
	}

	public LcdShield(ArduinoFirmata firmata, Activity activity) {
		this.firmata = firmata;
		this.activity = activity;
	}

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub
				if (data.length < 2)
					return;
				byte command = data[0];
				byte methodId = data[1];
				int n = data.length - 2;
				byte[] newArray = new byte[n];
				System.arraycopy(data, 2, newArray, 0, n);
				if(command==LCD_COMMAND)processInput(methodId, newArray);

			}
		});
	}

	public void setLcdEventHandler(LcdEventHandler eventHandler) {
		LcdShield.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public static interface LcdEventHandler {
		void onTextChange(String text);

		void onLcdError(String error);
	}

	private void processInput(byte methodId, byte[] data) {
		switch (methodId) {
		case CLEAR:

			break;
		case HOME:

			break;
		case NO_DISPLAY:

			break;
		case DISPLAY:

			break;

		case SCROLL_DISPLAY_LEFT:

			break;

		case SCROLL_DISPLAY_RIGHT:

			break;

		case LEFT_TO_RIGHT:

			break;

		case RIGHT_TO_LEFT:

			break;
		case SET_CURSOR:

			break;
		case WRITE:

			break;
		case PRINT:

			break;

		case NO_AUTO_SCROLL:

			break;

		case AUTO_SCROLL:

			break;

		default:
			break;
		}
	}

}
