package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class SliderShield extends ControllerParent<SliderShield> {
	private int connectedPin;
	private int sliderValue;
	private ShieldFrame sf;
	private static final byte DATA_IN = 0x01;
	private byte sValue = 0;

	public SliderShield() {
		super();
		requiredPinsIndex = 1;
		shieldPins = new String[] { "Slider" };
	}

	public SliderShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		connectedPin = pins[0].getPinID();
		CommitInstanceTotable();
		super.setConnected(pins);
	}

	public int getSliderValue() {
		return sliderValue;
	}

	public void setSliderValue(int sliderValue) {
		this.sliderValue = sliderValue;
		getApplication().getAppFirmata().analogWrite(connectedPin, sliderValue);
		sValue = (byte) sliderValue;
		sf = new ShieldFrame(UIShield.SLIDER_SHIELD.getId(), DATA_IN);
		sf.addByteArgument(sValue);
		getApplication().getAppFirmata().sendShieldFrame(sf);
		CommitInstanceTotable();

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		sf = null;
	}

}
