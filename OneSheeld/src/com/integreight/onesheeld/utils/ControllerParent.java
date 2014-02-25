package com.integreight.onesheeld.utils;

import android.app.Activity;
import android.os.Handler;

import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.firmatabluetooth.ArduinoFirmataShieldFrameHandler;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.model.ShieldPin;

@SuppressWarnings("unchecked")
public abstract class ControllerParent<T extends ControllerParent<?>> {
	public MainActivity activity;
	private boolean hasConnectedPins = false;
	private String tag = "";
	private boolean hasForgroundView = false;

	public ControllerParent() {
		// TODO Auto-generated constructor stub
	}

	public ControllerParent(Activity activity, String tag) {
		setActivity((MainActivity) activity);
		setTag(tag);
	}

	public void setConnected(ArduinoConnectedPin... pins) {
		for (int i = 0; i < pins.length; i++) {
			activity.getThisApplication().getAppFirmata()
					.pinMode(pins[i].getPinID(), pins[i].getPinMode());
		}
		this.setHasConnectedPins(true);
		CommitInstanceTotable();
	}

	public void setShieldHandler(EventHandler handler) {
		CommitInstanceTotable();
	}

	public boolean isHasForgroundView() {
		return hasForgroundView;
	}

	public void setHasForgroundView(boolean hasForgroundView) {
		this.hasForgroundView = hasForgroundView;
		if (hasForgroundView)
			((T) ControllerParent.this).refresh();
		CommitInstanceTotable();
	}

	public Activity getActivity() {
		return activity;
	}

	public ControllerParent<T> setActivity(MainActivity activity) {
		this.activity = activity;
		setFirmataEventHandler();
		return this;
	}

	public void refresh() {

	}

	public void onSysex(byte command, byte[] data) {
		// TODO Auto-generated method stub
		CommitInstanceTotable();
	}

	public void onDigital(int portNumber, int portData) {
		System.out.println("parent");
		CommitInstanceTotable();
	}

	public void onAnalog(int pin, int value) {
		// TODO Auto-generated method stub
		CommitInstanceTotable();
	}

	public void onUartReceive(byte[] data) {
		// TODO Auto-generated method stub
		CommitInstanceTotable();
	}

	public abstract void onNewShieldFrameReceived(ShieldFrame frame);

	public Handler actionHandler = new Handler();

	private void setFirmataEventHandler() {
		((OneSheeldApplication) activity.getApplication()).getAppFirmata()
				.addDataHandler(new ArduinoFirmataDataHandler() {

					@Override
					public void onSysex(final byte command, final byte[] data) {
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								((T) ControllerParent.this).onSysex(command,
										data);
							}
						});
					}

					@Override
					public void onDigital(final int portNumber,
							final int portData) {
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								if (hasConnectedPins)
									((T) ControllerParent.this).onDigital(
											portNumber, portData);
							}
						});
					}

					@Override
					public void onAnalog(final int pin, final int value) {
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								if (hasConnectedPins)
									((T) ControllerParent.this).onAnalog(pin,
											value);
							}
						});
					}

					@Override
					public void onUartReceive(final byte[] data) {
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								((T) ControllerParent.this).onUartReceive(data);
							}
						});
					}
				});
		((OneSheeldApplication) activity.getApplication()).getAppFirmata()
				.addShieldFrameHandler(new ArduinoFirmataShieldFrameHandler() {

					@Override
					public void onNewShieldFrameReceived(final ShieldFrame frame) {
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								((T) ControllerParent.this)
										.onNewShieldFrameReceived(frame);
								CommitInstanceTotable();
							}
						});
					}
				});
	}

	public OneSheeldApplication getApplication() {
		return activity.getThisApplication();
	}

	public String getTag() {
		return tag;
	}

	public ControllerParent<T> setTag(String tag) {
		this.tag = tag;
		getApplication().getRunningShields().put(tag, this);
		getApplication().getAppFirmata().initUart();
		return this;
	}

	public void CommitInstanceTotable() {
		// getApplication().getRunningShields().put(tag, this);
	}

	public boolean isHasConnectedPins() {
		return hasConnectedPins;
	}

	public void setHasConnectedPins(boolean hasConnectedPins) {
		this.hasConnectedPins = hasConnectedPins;
	}

	public abstract void reset();
}
