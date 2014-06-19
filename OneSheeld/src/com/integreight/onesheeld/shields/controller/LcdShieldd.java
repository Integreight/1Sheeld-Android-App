package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class LcdShieldd extends ControllerParent<LcdShieldd> {
	private LcdEventHandler eventHandler = new LcdEventHandler() {

		@Override
		public void updateLCD(char[] arrayToUpdate) {
			// TODO Auto-generated method stub

		}

		@Override
		public void noBlink() {
			// TODO Auto-generated method stub

		}

		@Override
		public void blink() {
			// TODO Auto-generated method stub

		}
	};
	// private Activity activity;
	public int rows = 2;
	public int columns = 16;
	public char[] chars;
	public int currIndx = 0;
	// Method ids
	private static final byte PRINT = (byte) 0x11;
	private static final byte BEGIN = (byte) 0x01;
	private static final byte CLEAR = (byte) 0x02;
	private static final byte HOME = (byte) 0x03;
	// private static final byte NO_DISPLAY = (byte) 0x05;
	// private static final byte DISPLAY = (byte) 0x06;
	private static final byte NO_BLINK = (byte) 0x04;
	private static final byte BLINK = (byte) 0x05;
	private static final byte NO_CURSOR = (byte) 0x06;
	private static final byte CURSOR = (byte) 0x07;
	private static final byte SCROLL_DISPLAY_LEFT = (byte) 0x08;
	private static final byte SCROLL_DISPLAY_RIGHT = (byte) 0x09;
	// private static final byte LEFT_TO_RIGHT = (byte) 0x0A;
	// private static final byte RIGHT_TO_LEFT = (byte) 0x0B;
	// private static final byte CREATE_CHAR = (byte) 0x0F;
	private static final byte SET_CURSOR = (byte) 0x0E;
	private static final byte WRITE = (byte) 0x0F;

	// private static final byte NO_AUTO_SCROLL = (byte) 0x0D;
	// private static final byte AUTO_SCROLL = (byte) 0x0C;
	public int lastScrollLeft = 0;
	public boolean isBlinking = false;

	public LcdShieldd() {
		super();
	}

	public LcdShieldd(Activity activity, String tag) {
		super(activity, tag);
		setChars(new char[rows * columns]);
	}

	@Override
	public ControllerParent<LcdShieldd> setTag(String tag) {
		setChars(new char[columns * rows]);
		return super.setTag(tag);
	}

	public void setLcdEventHandler(LcdEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public void write(char ch) {
		try {
			chars[currIndx] = ch;
			changeCursor(currIndx + 1);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	public void changeCursor(int indx) {
		currIndx = indx;
	}

	public void scrollDisplayLeft() {
		lastScrollLeft += 1;
		char[] tmp = new char[chars.length];
		for (int i = 0; i < tmp.length; i++) {
			try {
				tmp[i] = chars[i + lastScrollLeft];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		eventHandler.noBlink();
		eventHandler.updateLCD(tmp);
		if (isBlinking)
			eventHandler.blink();
	}

	public void scrollDisplayRight() {
		lastScrollLeft -= 1;
		char[] tmp = new char[chars.length];
		for (int i = 0; i < tmp.length; i++) {
			try {
				if (i >= lastScrollLeft * -1)
					tmp[i] = chars[i + lastScrollLeft];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		eventHandler.noBlink();
		eventHandler.updateLCD(tmp);
		if (isBlinking)
			eventHandler.blink();
	}

	public static interface LcdEventHandler {
		public void updateLCD(char[] arrayToUpdate);

		public void blink();

		public void noBlink();
	}

	private void processInput(ShieldFrame frame) {
		if (eventHandler != null)
			switch (frame.getFunctionId()) {
			case CLEAR:
				eventHandler.noBlink();
				lastScrollLeft = 0;
				chars = new char[columns * rows];
				eventHandler.updateLCD(chars);
				changeCursor(0);
				if (isBlinking)
					eventHandler.blink();
				break;
			case HOME:
				eventHandler.noBlink();
				changeCursor(0);
				if (isBlinking)
					eventHandler.blink();
				break;
			case BLINK:
				eventHandler.blink();
				isBlinking = true;
				break;
			case NO_BLINK:
				eventHandler.noBlink();
				isBlinking = false;
				break;

			case SCROLL_DISPLAY_LEFT:
				eventHandler.noBlink();
				scrollDisplayLeft();
				eventHandler.blink();
				break;

			case SCROLL_DISPLAY_RIGHT:
				eventHandler.noBlink();
				scrollDisplayRight();
				if (isBlinking)
					eventHandler.blink();
				break;

			case BEGIN:
				eventHandler.noBlink();
				changeCursor(0);
				if (isBlinking)
					eventHandler.blink();
				break;
			case SET_CURSOR:
				eventHandler.noBlink();
				int row = (int) frame.getArgument(0)[0];
				int col = (int) frame.getArgument(1)[0];
				changeCursor((row * columns) + col);
				if (isBlinking)
					eventHandler.blink();
				break;
			case WRITE:
				write(frame.getArgumentAsString(0).charAt(0));
				eventHandler.updateLCD(chars);
				break;
			case PRINT:// //
				eventHandler.noBlink();
				lastScrollLeft = 0;
				chars = new char[columns * rows];
				eventHandler.updateLCD(chars);
				changeCursor(0);
				if (isBlinking)
					eventHandler.blink();
				// /
				eventHandler.noBlink();
				lastScrollLeft = 0;
				String txt = frame.getArgumentAsString(0);
				for (int i = 0; i < txt.length(); i++) {
					try {
						chars[currIndx] = txt.charAt(i);
						changeCursor(currIndx + 1);
					} catch (Exception e) {
					}
				}
				eventHandler.updateLCD(chars);
				if (isBlinking)
					eventHandler.blink();
				break;

			case CURSOR:
				eventHandler.blink();
				isBlinking = true;
				break;

			case NO_CURSOR:
				eventHandler.noBlink();
				isBlinking = false;
				break;

			default:
				break;
			}
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.LCD_SHIELD.getId())
			processInput(frame);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	public char[] getChars() {
		return chars;
	}

	public void setChars(char[] chars) {
		this.chars = chars;
	}

}
