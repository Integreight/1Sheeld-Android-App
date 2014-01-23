package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.utils.ControllerParent;

public class SpeakerShield extends ControllerParent<ControllerParent<?>> {
	private int connectedPin;
	private SpeakerEventHandler eventHandler;
	private boolean isSpeakerPinOn;

	// public Led(ArduinoFirmata firmata, int connectedPin) {
	// this.connectedPin = connectedPin;
	// this.firmata = firmata;
	// setFirmataEventHandler();
	// firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
	// isLedOn = firmata.digitalRead(connectedPin);
	// }
	public SpeakerShield() {
		super();
	}

	public SpeakerShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public boolean isSpeakerPinOn() {
		return isSpeakerPinOn;
	}

	// public boolean refreshLed(){
	// isSpeakerPinOn = firmata.digitalRead(connectedPin);
	// return isSpeakerPinOn;
	// }

	@Override
	public void onDigital(int portNumber, int portData) {

		isSpeakerPinOn = getApplication().getAppFirmata().digitalRead(
				connectedPin);
		if (eventHandler != null) {
			eventHandler.onSpeakerPinChange(isSpeakerPinOn);
		}

		super.onDigital(portNumber, portData);
	}

	// public void setLedEventHandler(LedEventHandler eventHandler) {
	// this.eventHandler = eventHandler;
	// }

	public void setSpeakerEventHandler(SpeakerEventHandler eventHandler,
			int connectedPin) {
		this.eventHandler = eventHandler;
		this.connectedPin = connectedPin;
		getApplication().getAppFirmata().pinMode(connectedPin,
				ArduinoFirmata.INPUT);
		isSpeakerPinOn = getApplication().getAppFirmata().digitalRead(
				connectedPin);
	}

	public static interface SpeakerEventHandler {
		void onSpeakerPinChange(boolean isOn);
	}
}
