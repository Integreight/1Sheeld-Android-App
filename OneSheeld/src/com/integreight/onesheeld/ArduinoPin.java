package com.integreight.onesheeld;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum ArduinoPin {
	_0(0,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.RX)),
	_1(1,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.TX)),
	_2(2,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	_3(3,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_4(4,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	_5(5,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_6(6,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_7(7,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	_8(8,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	_9(9,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_10(10,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_11(11,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.PWM)),
	_12(12,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	_13(13,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT)),
	A0(14,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG)),
	A1(15,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG)),
	A2(16,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG)),
	A3(17,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG)),
	A4(18,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG)),
	A5(19,EnumSet.of(ArduinoPinCapability.INPUT,ArduinoPinCapability.OUTPUT,ArduinoPinCapability.ANALOG));
	EnumSet<ArduinoPinCapability> capabilitySet;
	int microHardwarePin;
	ArduinoPin(int microHardwarePin,EnumSet<ArduinoPinCapability> set) {
		this.microHardwarePin=microHardwarePin;
		this.capabilitySet=set;
	}
	
	public EnumSet<ArduinoPinCapability> getCapabilitySet(){
		return capabilitySet;//.clone();
	}
	
	public static List<ArduinoPin> getDigitalPins(){
		List<ArduinoPin> arduinoPins=new ArrayList<ArduinoPin>();
		for(ArduinoPin pin:ArduinoPin.values()){
			if(pin.getCapabilitySet().contains(ArduinoPinCapability.INPUT)&&pin.getCapabilitySet().contains(ArduinoPinCapability.OUTPUT))arduinoPins.add(pin);
		}
		return arduinoPins;
	}

}
