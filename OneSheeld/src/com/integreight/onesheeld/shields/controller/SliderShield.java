package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class SliderShield extends ControllerParent<SliderShield> {
	private int connectedPin;
	private int sliderValue;

	public SliderShield() {
		super();
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
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		
	}

}
