package com.integreight.onesheeld.utils;

import java.util.Arrays;
import java.util.Hashtable;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.firmatabluetooth.ArduinoFirmataShieldFrameHandler;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;

@SuppressWarnings("unchecked")
public abstract class ControllerParent<T extends ControllerParent<?>> {
	public MainActivity activity;
	private boolean hasConnectedPins = false;
	private String tag = "";
	private boolean hasForgroundView = false;
	public Hashtable<String, ArduinoPin> matchedShieldPins = new Hashtable<String, ArduinoPin>();
	public int requiredPinsIndex = -1;
	private boolean isALive = false;
	public String[][] requiredPinsNames = new String[][] {
			{ "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
					"A0", "A1", "A2", "A3", "A4", "A5" },
			{ "3", "5", "6", "9", "10", "11" } };
	public String[] shieldPins = new String[] {};
	public SelectionAction selectionAction;
	public boolean isInteractive = true;

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

	public boolean isHasForgroundView() {
		return hasForgroundView;
	}

	public void setHasForgroundView(boolean hasForgroundView) {
		this.hasForgroundView = hasForgroundView;
		if (hasForgroundView) {
			((T) ControllerParent.this).refresh();
			if (getActivity() != null
					&& getActivity().findViewById(R.id.pinsFixedHandler) != null)
				getActivity().findViewById(R.id.pinsFixedHandler)
						.setVisibility(
								requiredPinsIndex != -1 ? View.VISIBLE
										: View.GONE);
		}

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
	public Handler onDigitalActionHandler = new Handler();

	private void setFirmataEventHandler() {
		((OneSheeldApplication) activity.getApplication()).getAppFirmata()
				.addDataHandler(arduinoFirmataDataHandler);
		((OneSheeldApplication) activity.getApplication()).getAppFirmata()
				.addShieldFrameHandler(arduinoFirmataShieldFrameHandler);
	}

	public ArduinoFirmataDataHandler arduinoFirmataDataHandler = new ArduinoFirmataDataHandler() {

		@Override
		public void onSysex(final byte command, final byte[] data) {
			actionHandler.post(new Runnable() {

				@Override
				public void run() {
					if (isALive)
						actionHandler.post(new Runnable() {

							@Override
							public void run() {
								if (isInteractive)
									((T) ControllerParent.this).onSysex(
											command, data);
							}
						});
				}
			});
		}

		Runnable onDigitalRunnable = new Runnable() {

			@Override
			public void run() {
				if (hasConnectedPins)
					((T) ControllerParent.this).onDigital(portNumber, portData);
			}
		};
		private int portNumber, portData;

		@Override
		public void onDigital(final int portNumber, final int portData) {
			if (isALive && isInteractive) {
				onDigitalActionHandler.removeCallbacks(onDigitalRunnable);
				this.portData = portData;
				this.portNumber = portNumber;
				onDigitalActionHandler.post(onDigitalRunnable);
			}
		}

		@Override
		public void onAnalog(final int pin, final int value) {
			if (isALive && isInteractive)
				actionHandler.post(new Runnable() {

					@Override
					public void run() {
						if (hasConnectedPins)
							((T) ControllerParent.this).onAnalog(pin, value);
					}
				});
		}
	};
	public ArduinoFirmataShieldFrameHandler arduinoFirmataShieldFrameHandler = new ArduinoFirmataShieldFrameHandler() {

		@Override
		public void onNewShieldFrameReceived(final ShieldFrame frame) {
			if (isALive && frame != null && matchedShieldPins.size() == 0)
				actionHandler.post(new Runnable() {

					@Override
					public void run() {
						try {
							if (isInteractive)
								((T) ControllerParent.this)
										.onNewShieldFrameReceived(frame);
						} catch (NullPointerException e) {
							Crashlytics.logException(e);
						}
					}
				});
		}
	};

	public OneSheeldApplication getApplication() {
		return activity.getThisApplication();
	}

	public String getTag() {
		return tag;
	}

	public ControllerParent<T> setTag(String tag) {
		this.tag = tag;
		isALive = true;
		if (getApplication().getRunningShields().get(tag) == null)
			getApplication().getRunningShields().put(tag, this);
		getApplication().getAppFirmata().initUart();
		getApplication().getGaTracker().send(
				MapBuilder.createEvent(Fields.EVENT_ACTION, "start", "", null)
						.set(getTag(), "start").build());
		Crashlytics
				.setString(
						"Number of running shields",
						getApplication().getRunningShields() == null
								|| getApplication().getRunningShields().size() == 0 ? "No Running Shields"
								: ""
										+ getApplication().getRunningShields()
												.size());
		Crashlytics
				.setString(
						"Running Shields",
						getApplication().getRunningShields() != null
								&& getApplication().getRunningShields().size() > 0 ? getApplication()
								.getRunningShields().keySet().toString()
								: "No Running Shields");
		return this;
	}

	public ControllerParent<T> invalidate(SelectionAction selectionAction,
			boolean isToastable) {
		this.selectionAction = selectionAction;
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

	public void resetThis() {
		if (activity != null) {
			if (activity.looperThread == null
					|| (!activity.looperThread.isAlive() || activity.looperThread
							.isInterrupted()))
				activity.initLooperThread();
			activity.backgroundThreadHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					if (matchedShieldPins != null)
						matchedShieldPins.clear();
					isALive = false;
					if (shieldPins != null && shieldPins.length > 0) {
						for (ArduinoPin pin : Arrays.asList(ArduinoPin.values())) {
							for (int i = 0; i < shieldPins.length; i++) {
								if (pin.connectedPins.size() == 0)
									break;
								pin.connectedPins
										.remove(((T) ControllerParent.this)
												.getClass().getName()
												+ shieldPins[i]);
							}
						}
					}
					((T) ControllerParent.this).reset();
					getApplication().getAppFirmata().removeDataHandler(
							arduinoFirmataDataHandler);
					getApplication().getAppFirmata().removeShieldFrameHandler(
							arduinoFirmataShieldFrameHandler);
				}
			});
		}
		getApplication().getGaTracker().send(
				MapBuilder
						.createEvent("Controller Tracker", "end", getTag(),
								null).set(getTag(), "end").build());
		Crashlytics
				.setString(
						"Number of running shields",
						getApplication().getRunningShields() == null
								|| getApplication().getRunningShields().size() == 0 ? "No Running Shields"
								: ""
										+ getApplication().getRunningShields()
												.size());
		Crashlytics
				.setString(
						"Running Shields",
						getApplication().getRunningShields() != null
								&& getApplication().getRunningShields().size() > 0 ? getApplication()
								.getRunningShields().keySet().toString()
								: "No Running Shields");
	}

	public void sendShieldFrame(ShieldFrame frame) {
		if (isInteractive)
			activity.getThisApplication().getAppFirmata()
					.sendShieldFrame(frame);
	}

	public void digitalWrite(int pin, boolean value) {
		if (isInteractive)
			activity.getThisApplication().getAppFirmata()
					.digitalWrite(pin, value);
	}

	public void analogWrite(int pin, int value) {
		if (isInteractive)
			activity.getThisApplication().getAppFirmata()
					.analogWrite(pin, value);
	}

	public abstract void reset();

	public String[] getRequiredPinsNames() {
		return requiredPinsNames[requiredPinsIndex];
	}

	public static interface SelectionAction {
		public void onSuccess();

		public void onFailure();
	}

}
