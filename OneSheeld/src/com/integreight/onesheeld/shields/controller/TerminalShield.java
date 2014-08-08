package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class TerminalShield extends ControllerParent<TerminalShield> {
	private static final byte WRITE = 0x01;
	private static final byte PRINT = 0x02;
	private static final byte DATA_IN = 0x01;
	private TerminalHandler eventHandler;

	public TerminalShield() {
		super();
	}

	public TerminalShield(Activity activity, String tag) {
		super(activity, tag);
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
		CommitInstanceTotable();
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.TERMINAL_SHIELD.getId()) {
			switch (frame.getFunctionId()) {
			case WRITE:
				if (eventHandler != null)
					eventHandler.onPrint(frame.getArgumentAsString(0));
				break;
			case PRINT:
				if (eventHandler != null)
					eventHandler.onPrint(frame.getArgumentAsString(0));
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void reset() {

	}

	public TerminalHandler getEventHandler() {
		return eventHandler;
	}

	public void setEventHandler(TerminalHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public interface TerminalHandler {
		public void onPrint(String output);
	}

}
