package com.integreight.onesheeld.shields.controller;

import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.fragments.TerminalFragment;

public class TerminalShield extends ControllerParent<TerminalShield> {
	private static final byte WRITE = 0x01;
	private static final byte PRINT = 0x02;
	private static final byte DATA_IN = 0x01;
	private TerminalHandler eventHandler;
	public int[] encodingMths = new int[] { R.id.terminalString, R.id.asci,
			R.id.binary, R.id.hex };
	public int selectedEnMth = 0;
	public CopyOnWriteArrayList<PrintedLine> printedLines, temp;

	public TerminalShield() {
		super();
	}

	public TerminalShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<TerminalShield> setTag(String tag) {
		printedLines = new CopyOnWriteArrayList<TerminalShield.PrintedLine>();
		return super.setTag(tag);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		super.setConnected(pins);
	}

	private ShieldFrame sf;

	public void input(String input) {
		sf = new ShieldFrame(UIShield.TERMINAL_SHIELD.getId(), DATA_IN);
		sf.addStringArgument(input);
		sendShieldFrame(sf);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
	}

	private boolean greaterThanThousand = false;

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.TERMINAL_SHIELD.getId()) {
			String outputTxt = frame.getArgumentAsString(0);
			String date = printedLines.size() == 0
					|| printedLines.get(printedLines.size() - 1).isEndedWithNewLine ? TerminalFragment
					.getTimeAsString() + " [RX] : "
					: "";
			boolean isEndedWithNewLine = outputTxt.length() > 0
					&& outputTxt.charAt(outputTxt.length() - 1) == '\n';
			printedLines.add(new TerminalShield.PrintedLine(date, outputTxt
					.substring(0, isEndedWithNewLine ? outputTxt.length() - 1
							: outputTxt.length()), isEndedWithNewLine));
			greaterThanThousand = printedLines.size() > 1000;
			if (greaterThanThousand) {
				printedLines.remove(0);
			}
			switch (frame.getFunctionId()) {
			case WRITE:
				if (eventHandler != null) {
					eventHandler.onPrint(outputTxt, greaterThanThousand);
				}
				break;
			case PRINT:
				if (eventHandler != null) {
					eventHandler.onPrint(outputTxt, greaterThanThousand);
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void reset() {
		if (printedLines != null)
			printedLines.clear();
		printedLines = null;
	}

	public TerminalHandler getEventHandler() {
		return eventHandler;
	}

	public void setEventHandler(TerminalHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public interface TerminalHandler {
		public void onPrint(String output, final boolean clearBeforeWriting);
	}

	public static class PrintedLine {
		public String date;
		public String print;
		public boolean isEndedWithNewLine;

		public PrintedLine(String date, String print, boolean isEndedWithNewLine) {
			super();
			this.date = date;
			this.print = print;
			this.isEndedWithNewLine = isEndedWithNewLine;
		}

	}

}
