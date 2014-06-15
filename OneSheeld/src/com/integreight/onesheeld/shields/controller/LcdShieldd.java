package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class LcdShieldd extends ControllerParent<LcdShieldd> {
	private LcdEventHandler eventHandler;
	// private Activity activity;
	public int rows = 2;
	public int columns = 16;
	public char[][] chars;
	public int currRowIndx = 0, currColIndx = 0;
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

	public LcdShieldd() {
		super();
	}

	public LcdShieldd(Activity activity, String tag) {
		super(activity, tag);
		setChars(new char[rows][columns]);
	}

	@Override
	public ControllerParent<LcdShieldd> setTag(String tag) {
		setChars(new char[rows][columns]);
		return super.setTag(tag);
	}

	public void setLcdEventHandler(LcdEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface LcdEventHandler {
		public void setCursor(int x, int y);

		public void write(char ch, boolean moveCursor);

		public void blink();

		public void noBlink();

		public void cursor();

		public void noCursor();

		public void clear();

		public void scrollDisplayLeft();

		public void scrollDisplayRight();

		void onLcdError(String error);
	}

	private void processInput(ShieldFrame frame) {
		if (eventHandler != null)
			switch (frame.getFunctionId()) {
			case CLEAR:
				eventHandler.clear();
				break;
			case HOME:
				eventHandler.setCursor(0, 0);
				break;
			case BLINK:
				eventHandler.blink();
				break;
			case NO_BLINK:
				eventHandler.noBlink();
				break;

			case SCROLL_DISPLAY_LEFT:
				eventHandler.scrollDisplayLeft();
				break;

			case SCROLL_DISPLAY_RIGHT:
				eventHandler.scrollDisplayRight();
				break;

			case BEGIN:
				try {
					eventHandler.setCursor((int) frame.getArgument(0)[0],
							(int) frame.getArgument(1)[0]);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
				break;
			case SET_CURSOR:
				try {
					eventHandler.setCursor((int) frame.getArgument(0)[0],
							(int) frame.getArgument(1)[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case WRITE:
				chars[currRowIndx][currRowIndx] = frame.getArgumentAsString(0)
						.charAt(0);
				eventHandler.write(frame.getArgumentAsString(0).charAt(0),
						false);
				break;
			case PRINT:
				String txt = frame.getArgumentAsString(0);
				for (int i = 0; i < txt.length(); i++) {
					chars[currRowIndx][currRowIndx] = txt.charAt(i);
					eventHandler.write(txt.charAt(i), true);
				}
				break;

			case CURSOR:
				eventHandler.cursor();
				break;

			case NO_CURSOR:
				eventHandler.noCursor();
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

	public int getCurrColIndx() {
		return currColIndx;
	}

	public void setCurrColIndx(int currColIndx) {
		this.currColIndx = currColIndx;
	}

	public int getCurrRowIndx() {
		return currRowIndx;
	}

	public void setCurrRowIndx(int currRowIndx) {
		this.currRowIndx = currRowIndx;
	}

	public char[][] getChars() {
		return chars;
	}

	public void setChars(char[][] chars) {
		this.chars = chars;
	}

}
