package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.utils.ControllerParent;

public class LcdShield extends ControllerParent<LcdShield> {
	private static LcdEventHandler eventHandler;
	// private Activity activity;
	private static short rows = 2;
	private static short columns = 16;
	private static final byte LCD_COMMAND = (byte) 0x32;

	private short cursorXLocation = 0;
	private short cursorYLocation = 0;

	private String[] lcdText;
	private char[][] rawText;

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

	public String[] getLcdText() {
		return lcdText;
	}

	public LcdShield() {
		super();
	}

	public LcdShield(Activity activity, String tag) {
		super(activity, tag);
		// this.activity = activity;
		rawText = new char[rows][columns];
		lcdText = new String[rows];
		resetPins();
	}

	@Override
	public ControllerParent<LcdShield> setTag(String tag) {
		rawText = new char[rows][columns];
		lcdText = new String[rows];
		resetPins();
		return super.setTag(tag);
	}

	@Override
	public void onUartReceive(byte[] data) {
		if (data.length < 2)
			return;
		byte command = data[0];
		byte methodId = data[1];
		int n = data.length - 2;
		byte[] newArray = new byte[n];
		System.arraycopy(data, 2, newArray, 0, n);
		if (command == LCD_COMMAND)
			processInput(methodId, newArray);
		super.onUartReceive(data);
	}

	public void setLcdEventHandler(LcdEventHandler eventHandler) {
		LcdShield.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface LcdEventHandler {
		void onTextChange(String[] text);

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
			_setCursor(data);
			break;
		case WRITE:

			break;
		case PRINT:
			_print(data);
			break;

		case NO_AUTO_SCROLL:

			break;

		case AUTO_SCROLL:

			break;

		default:
			break;
		}
	}

	private void _print(byte[] data) {
		String temp = new String(data);
		convertStringToRawText(temp);
		for (int i = 0; i < lcdText.length; i++) {
			lcdText[i] = new String(rawText[i], 0, 16);
		}
		if (eventHandler != null)
			eventHandler.onTextChange(lcdText);
	}

	private void _setCursor(byte[] data) {
		if (data.length < 2)
			return;
		cursorYLocation = (short) (data[0] - 1);
		cursorXLocation = (short) (data[1] - 1);
	}

	private void convertStringToRawText(String text) {
		int charPosition = 0;
		for (short i = cursorXLocation; charPosition < text.length()
				&& i < columns; i++, charPosition++) {
			rawText[cursorYLocation][i] = text.charAt(charPosition);
			cursorXLocation = (short) (i + 1);

		}
		CommitInstanceTotable();
		// if (charPosition < text.length()) {
		// cursorYLocation = 1;
		// cursorXLocation = 0;
		// for (int i = cursorXLocation; charPosition < text.length() && i <
		// COLUMNS; i++,charPosition++) {
		// tempRawText[1][i] = text.charAt(charPosition);
		// cursorXLocation = (short) i;
		// }
		//
		// }

	}

	public void resetPins() {
		cursorYLocation = 0;
		cursorXLocation = 0;
		for (int i = 0; i < rawText.length; i++) {
			for (int j = 0; j < rawText[i].length; j++) {
				rawText[i][j] = ' ';
			}
		}

		for (int i = 0; i < lcdText.length; i++) {
			lcdText[i] = new String(rawText[i], 0, 16);
		}
		CommitInstanceTotable();
		// if (eventHandler != null)
		// eventHandler.onTextChange(lcdText);
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
