package com.integreight.onesheeld.shields;

import android.util.Log;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class SpeakerShield {
	private int connectedPin;
	private ArduinoFirmata firmata;
	private SpeakerEventHandler eventHandler;
	private boolean isSpeakerPinOn;


//	public Led(ArduinoFirmata firmata, int connectedPin) {
//		this.connectedPin = connectedPin;
//		this.firmata = firmata;
//		setFirmataEventHandler();
//		firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
//		isLedOn = firmata.digitalRead(connectedPin);
//	}
	
	public SpeakerShield(ArduinoFirmata firmata){
		this.firmata = firmata;
	}


	public boolean isSpeakerPinOn() {
		return isSpeakerPinOn;
	}
	
//	public boolean refreshLed(){
//		isSpeakerPinOn = firmata.digitalRead(connectedPin);
//		return isSpeakerPinOn;
//	}
	

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
				
				isSpeakerPinOn = firmata.digitalRead(connectedPin);
				if (eventHandler != null) {
					eventHandler.onSpeakerPinChange(isSpeakerPinOn);
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
	
	public void setSpeakerEventHandler(SpeakerEventHandler eventHandler, int connectedPin) {
		this.eventHandler = eventHandler;
		this.connectedPin = connectedPin;
		firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
		isSpeakerPinOn = firmata.digitalRead(connectedPin);
		setFirmataEventHandler();
	}

	public static interface SpeakerEventHandler {
		void onSpeakerPinChange(boolean isOn);
	}
}
