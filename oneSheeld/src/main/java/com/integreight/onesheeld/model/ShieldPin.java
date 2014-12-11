package com.integreight.onesheeld.model;

import com.integreight.onesheeld.enums.ArduinoPin;

public class ShieldPin {
	public String name;
	public ArduinoPin pin;

	public ShieldPin() {
		// TODO Auto-generated constructor stub
	}

	public ShieldPin(String name, ArduinoPin pin) {
		this.name = name;
		this.pin = pin;
	}
}
