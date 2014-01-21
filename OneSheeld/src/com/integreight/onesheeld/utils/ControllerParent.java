package com.integreight.onesheeld.utils;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.model.ArduinoConnectedPin;

public abstract class ControllerParent<T extends ControllerParent<?>> {
	public MainActivity activity;
	private boolean hasForgroundView = false;

	public ControllerParent() {
		// TODO Auto-generated constructor stub
	}

	public ControllerParent(Activity activity) {
		setActivity((MainActivity) activity);
	}

	public void setConnected(ArduinoConnectedPin... pins) {
		for (int i = 0; i < pins.length; i++) {
			activity.getThisApplication().getAppFirmata()
					.pinMode(pins[i].getPinID(), pins[i].getPinMode());
		}
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

	public ControllerParent<T> setActivity(MainActivity activity) {
		this.activity = activity;
		setFirmataEventHandler();
		return this;
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
						((T) ControllerParent.this).onSysex(command, data);
					}

					@Override
					public void onDigital(int portNumber, int portData) {
						((T) ControllerParent.this).onDigital(portNumber,
								portData);
					}

					@Override
					public void onAnalog(int pin, int value) {
						((T) ControllerParent.this).onAnalog(pin, value);
					}

					@Override
					public void onUartReceive(byte[] data) {
						((T) ControllerParent.this).onUartReceive(data);
					}
				});
	}

}
