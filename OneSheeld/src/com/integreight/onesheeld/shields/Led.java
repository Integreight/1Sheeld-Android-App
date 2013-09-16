package com.integreight.onesheeld.shields;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class Led {
	private int connectedPin;
	private ArduinoFirmata firmata;
	private LedEventHandler eventHandler;
	private boolean isLedOn;

//	public Led(ArduinoFirmata firmata, int connectedPin) {
//		this.connectedPin = connectedPin;
//		this.firmata = firmata;
//		setFirmataEventHandler();
//		firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
//		isLedOn = firmata.digitalRead(connectedPin);
//	}
	
	public Led(ArduinoFirmata firmata){
		this.firmata = firmata;
	}

	public boolean isLedOn() {
		return isLedOn;
	}

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
				if (eventHandler != null) {
					isLedOn = firmata.digitalRead(connectedPin);
					eventHandler.onLedChange(isLedOn);
				}

			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}
		});
	}

//	public void setLedEventHandler(LedEventHandler eventHandler) {
//		this.eventHandler = eventHandler;
//	}
	
	public void setLedEventHandler(LedEventHandler eventHandler, int connectedPin) {
		this.eventHandler = eventHandler;
		this.connectedPin = connectedPin;
		firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
		isLedOn = firmata.digitalRead(connectedPin);
		setFirmataEventHandler();
	}

	public static interface LedEventHandler {
		void onLedChange(boolean isLedOn);
	}
}
