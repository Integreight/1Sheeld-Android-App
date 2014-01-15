package com.integreight.onesheeld.shields.controller;

import com.integreight.firmatabluetooth.ArduinoFirmata;

public class ToggleButtonShield {
	private int connectedPin;
	private ArduinoFirmata firmata;
	private boolean isButtonOn;
	
	public ToggleButtonShield(ArduinoFirmata firmata){
		this.firmata = firmata;
	}

	public void setConnectedPin(int connectedPin) {
		this.connectedPin = connectedPin;
		firmata.pinMode(connectedPin, ArduinoFirmata.OUTPUT);
	}

	public boolean isButtonOn() {
		return isButtonOn;
	}
	
	public void setButtonOn(boolean isButtonOn) {
		this.isButtonOn = isButtonOn;
		firmata.digitalWrite(connectedPin, isButtonOn);
	}
	
}
