package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class ToggleButtonShield extends ControllerParent<ToggleButtonShield> {
	private int connectedPin;
	private boolean isButtonOn;

	public ToggleButtonShield() {
		super();
	}

	public ToggleButtonShield(Activity activity,String tag) {
		super(activity,tag);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		connectedPin = pins[0].getPinID();
		super.setConnected(pins);
	}

	public boolean isButtonOn() {
		return isButtonOn;
	}

	public void setButtonOn(boolean isButtonOn) {
		this.isButtonOn = isButtonOn;
		activity.getThisApplication().getAppFirmata()
				.digitalWrite(connectedPin, isButtonOn);
		CommitInstanceTotable();
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

}
