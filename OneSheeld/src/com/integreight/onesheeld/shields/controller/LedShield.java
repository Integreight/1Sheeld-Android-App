package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class LedShield extends ControllerParent<LedShield> {
	private int connectedPin;
	private LedEventHandler eventHandler;
	private boolean isLedOn;

	// public Led(ArduinoFirmata firmata, int connectedPin) {
	// this.connectedPin = connectedPin;
	// this.firmata = firmata;
	// setFirmataEventHandler();
	// firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
	// isLedOn = firmata.digitalRead(connectedPin);
	// }
	public LedShield() {
		super();
	}

	public LedShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public boolean isLedOn() {
		return isLedOn;
	}

	public boolean refreshLed() {
		isLedOn = activity.getThisApplication().getAppFirmata()
				.digitalRead(connectedPin);
		CommitInstanceTotable();
		return isLedOn;
	}

	@Override
	public void onDigital(int portNumber, int portData) {
		isLedOn = activity.getThisApplication().getAppFirmata()
				.digitalRead(connectedPin);
		if (eventHandler != null) {
			eventHandler.onLedChange(isLedOn);
		}
		CommitInstanceTotable();
		super.onDigital(portNumber, portData);
	}

	public void setLedEventHandler(LedEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		isLedOn = activity.getThisApplication().getAppFirmata()
				.digitalRead(connectedPin);
		CommitInstanceTotable();
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		this.connectedPin = pins[0].getPinID();
		super.setConnected(pins);
	}

	public static interface LedEventHandler {
		void onLedChange(boolean isLedOn);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		
	}
}
