package com.integreight.onesheeld.shields;

import java.util.HashMap;
import java.util.Map;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class SevenSegmentShield {
	private ArduinoFirmata firmata;
	private SevenSegmentsEventHandler eventHandler;
	private Map<Segment, Boolean> segmentsStatus;
	private Map<Segment, Integer> segmentsConnectedPins;

	public SevenSegmentShield(ArduinoFirmata firmata) {
		this.firmata = firmata;
		segmentsStatus = new HashMap<Segment, Boolean>();
		segmentsConnectedPins = new HashMap<Segment, Integer>();
		for (Segment segment : Segment.values()) {
			segmentsStatus.put(segment, false);
			segmentsConnectedPins.put(segment, null);
		}
	}
	
	public void connectSegmentWithPin(Segment segment,int pin){
		segmentsConnectedPins.put(segment, pin);
	}

	public Map<Segment, Boolean> getSegmentsStatus() {
		return segmentsStatus;
	}

	public Map<Segment, Boolean> refreshSegments() {

		updateSegmentsStatusFromFirmata();
		return segmentsStatus;
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
				updateSegmentsStatusFromFirmata();
				if (eventHandler != null) {
					eventHandler.onSegmentsChange(segmentsStatus);
				}

			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void setSevenSegmentsEventHandler(
			SevenSegmentsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		for (Integer connectedPin : this.segmentsConnectedPins.values()) {
			if(connectedPin!=null)firmata.pinMode(connectedPin, ArduinoFirmata.INPUT);
		}
		updateSegmentsStatusFromFirmata();
		setFirmataEventHandler();
	}

	public static interface SevenSegmentsEventHandler {
		void onSegmentsChange(Map<Segment, Boolean> segmentsStatus);
	}

	private void updateSegmentsStatusFromFirmata() {
		for (Segment segment : Segment.values()) {
			Integer connectedPin = segmentsConnectedPins.get(segment);
			if (connectedPin != null)
				segmentsStatus.put(segment, firmata.digitalRead(connectedPin));
		}
	}

	public static enum Segment {
		A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G"), DOT("DOT");

		String name;

		Segment(String name) {
			this.name = name;
		}

		 String getName() {
			return name;
		}

		public static CharSequence[] getSegmentsNames() {
			CharSequence[] temp = new CharSequence[Segment.values().length];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = Segment.values()[i].getName();
			}
			return temp;
		}
		
		public static Segment getSegment(int position){
			switch (position) {
			case 0:return A;
			case 1:return B;
			case 2:return C;
			case 3:return D;
			case 4:return E;
			case 5:return F;
			case 6:return G;
			case 7:return DOT;
			default:return null;
			}
			
		}
	}
}
