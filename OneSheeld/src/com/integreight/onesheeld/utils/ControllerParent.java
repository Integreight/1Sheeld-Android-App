package com.integreight.onesheeld.utils;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.OneSheeldActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.model.ArduinoConnectedPin;

public abstract class ControllerParent {
	private OneSheeldActivity activity;
	private boolean hasForgroundView = false;

	public ControllerParent(OneSheeldActivity activity) {
		setActivity(activity);
		setFirmataEventHandler();
	}

	public void setConnected(ArduinoConnectedPin... pins) {

	}

	public void setShieldHandler(EventHandler handler) {
	}

	public boolean isHasForgroundView() {
		return hasForgroundView;
	}

	public void setHasForgroundView(boolean hasForgroundView) {
		this.hasForgroundView = hasForgroundView;
		refresh();
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(OneSheeldActivity activity) {
		this.activity = activity;
	}

	public abstract void refresh();

	public void onSysex(byte command, byte[] data) {
		// TODO Auto-generated method stub

	}

	public void onDigital(int portNumber, int portData) {
		System.out.println("parent");
	}

	public void onAnalog(int pin, int value) {
		// TODO Auto-generated method stub

	}

	public void onUartReceive(byte[] data) {
		// TODO Auto-generated method stub

	}

	private void setFirmataEventHandler() {
		((OneSheeldApplication) activity.getApplication()).getAppFirmata()
				.addDataHandler(new ArduinoFirmataDataHandler() {

					@Override
					public void onSysex(byte command, byte[] data) {
						ControllerParent.this.onSysex(command, data);
					}

					@Override
					public void onDigital(int portNumber, int portData) {
						ControllerParent.this.onDigital(portNumber, portData);
					}

					@Override
					public void onAnalog(int pin, int value) {
						ControllerParent.this.onAnalog(pin, value);
					}

					@Override
					public void onUartReceive(byte[] data) {
						ControllerParent.this.onUartReceive(data);
					}
				});
	}

}
