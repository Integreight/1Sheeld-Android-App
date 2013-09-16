package com.integreight.onesheeld.shields;

import com.integreight.firmatabluetooth.ArduinoFirmata;

public class SliderShield {
	private int connectedPin;
	private ArduinoFirmata firmata;
	private int sliderValue;
	
	public SliderShield(ArduinoFirmata firmata){
		this.firmata = firmata;
	}

	public void setConnectedPin(int connectedPin) {
		this.connectedPin = connectedPin;
		firmata.pinMode(connectedPin, ArduinoFirmata.PWM);
	}

	public int getSliderValue() {
		return sliderValue;
	}
	
	public void setSliderValue(int sliderValue) {
		this.sliderValue = sliderValue;
		firmata.analogWrite(connectedPin, sliderValue);
	}
	
}
