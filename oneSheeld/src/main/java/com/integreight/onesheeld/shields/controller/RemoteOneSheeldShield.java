package com.integreight.onesheeld.shields.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.push.PushMessagesReceiver;
import com.integreight.onesheeld.shields.ControllerParent;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;

public class RemoteOneSheeldShield extends
		ControllerParent<RemoteOneSheeldShield> {
	private static final byte PIN_MODE = (byte) 0x01;
	private static final byte DIGITAL_WRITE = (byte) 0x02;
	private static final byte DIGITAL_READ_REQUEST = (byte) 0x03;
	private static final byte ANALOG_WRITE = (byte) 0x04;
	private static final byte FLOAT_MESSAGE = (byte) 0x05;
	private static final byte STRING_MESSAGE = (byte) 0x06;
	private static final byte SUBSCRIBE_TO_DIGITAL_PIN_CHANGES = (byte) 0x07;
	private static final byte UNSUBSCRIBE_TO_DIGITAL_PIN_CHANGES = (byte) 0x08;

	public static final byte DIGITAL_READ_RESPONSE = (byte) 0x01;
	public static final byte FLOAT_MESSAGE_RESPONSE = (byte) 0x02;
	public static final byte STRING_MESSAGE_RESPONSE = (byte) 0x03;

	public static final int MAXIMUM_NOTIFICATION_LIMIT = 1000;

	private boolean isReportedToAnalytics;
	
	private ConcurrentHashMap<String, Integer> digitalPinSubscribtionNotificationsLimit;
	private Map<String, List<Integer>> subscribedDevices;
	private Map<Integer, Boolean> lastSentValues;
	private ConcurrentHashMap<Integer, Boolean> currentValues;
	private ConcurrentHashMap<Integer, Boolean> digitalPinFloatingGuard;
	DigitalPinsChangeChecker digitalPinsChangeCheckerThread;

	public RemoteOneSheeldShield() {
		super();
	}

	public RemoteOneSheeldShield(Activity activity, String tag) {
		super(activity, tag);
		digitalPinSubscribtionNotificationsLimit = new ConcurrentHashMap<String, Integer>();
		subscribedDevices = new HashMap<String, List<Integer>>();
		lastSentValues = new HashMap<Integer, Boolean>();
		currentValues = new ConcurrentHashMap<Integer, Boolean>();
		digitalPinFloatingGuard = new ConcurrentHashMap<Integer, Boolean>();
		digitalPinsChangeCheckerThread = new DigitalPinsChangeChecker();
		isReportedToAnalytics=false;
	}

	@Override
	public void onDigital(int portNumber, int portData) {
		ArduinoFirmata firmata = getApplication().getAppFirmata();

		synchronized (digitalPinFloatingGuard) {
			for (Integer pin : getPinsSubscribers().keySet()) {
				boolean value = firmata.digitalRead(pin);
				if (currentValues.containsKey(pin)
						&& currentValues.get(pin) != value) {
					digitalPinFloatingGuard.put(pin, false);
				}
				synchronized (currentValues) {
					currentValues.put(pin, value);
				}
			}
		}

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.REMOTEONESHEELD_SHIELD.getId()) {
			switch (frame.getFunctionId()) {
			case PIN_MODE:
				processPinModeMessage(frame);
				break;
			case DIGITAL_WRITE:
				processDigitalWriteMessage(frame);
				break;
			case DIGITAL_READ_REQUEST:
				processDigitalReadRequestMessage(frame);
				break;
			case ANALOG_WRITE:
				processAnalogWriteMessage(frame);
				break;
			case FLOAT_MESSAGE:
				processKeyValueFloatMessage(frame);
				break;
			case STRING_MESSAGE:
				processKeyValueStringMessage(frame);
				break;
			case SUBSCRIBE_TO_DIGITAL_PIN_CHANGES:
				processSubscribeToDigitalPinMessage(frame);
				break;
			case UNSUBSCRIBE_TO_DIGITAL_PIN_CHANGES:
				processUnsubscribeToDigitalPinMessage(frame);
				break;
			}
			
			if(!isReportedToAnalytics){
				activity.getThisApplication().getTracker().send(new HitBuilders.EventBuilder()
		        .setCategory("Shields Events")
		        .setAction("Remote OneSheeld Used")
		        .build());
				isReportedToAnalytics=true;
			}
		}
	}

	private void processPinModeMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 3)
			return;
		String address = frame.getArgumentAsString(0);
		int pin = (int) (frame.getArgument(1)[0] & 0xff);
		int mode = (int) (frame.getArgument(2)[0] & 0xff);
		JSONObject json = getPinModeMessage(pin, mode);
		sendPushMessage(PushMessagesReceiver.PinModePushMessageAction,address, json);
	}

	private void processDigitalReadRequestMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 2)
			return;
		String address = frame.getArgumentAsString(0);
		final int pin = (int) (frame.getArgument(1)[0] & 0xff);
		@SuppressWarnings("serial")
		JSONObject json = getSubscribeToDigitalPinMessage(new ArrayList<Integer>(){{add(pin);}});
		sendPushMessage(PushMessagesReceiver.DigitalReadRequestPushMessageAction,address, json);
	}

	private void processDigitalWriteMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 3)
			return;
		String address = frame.getArgumentAsString(0);
		int pin = (int) (frame.getArgument(1)[0] & 0xff);
		boolean value = (int) (frame.getArgument(2)[0] & 0xff) != 0;
		JSONObject json = getDigitalWriteMessage(pin, value);
		sendPushMessage(PushMessagesReceiver.DigitalWritePushMessageAction,address, json);
	}

	private void processAnalogWriteMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 3)
			return;
		String address = frame.getArgumentAsString(0);
		int pin = (int) (frame.getArgument(1)[0] & 0xff);
		int value = (int) (frame.getArgument(2)[0] & 0xff);
		JSONObject json = getAnalogWriteMessage(pin, value);
		sendPushMessage(PushMessagesReceiver.AnalogWritePushMessageAction,address, json);
	}

	private void processKeyValueFloatMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 3)
			return;
		String address = frame.getArgumentAsString(0);
		String key = frame.getArgumentAsString(1);
		float value = frame.getArgumentAsFloat(2);
		JSONObject json = getKeyValueFloatMessage(key, value);
		sendPushMessage(PushMessagesReceiver.KeyValueFloatPushMessageAction,address, json);
	}

	private void processKeyValueStringMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		if (frame.getArguments().size() != 3)
			return;
		String address = frame.getArgumentAsString(0);
		String key = frame.getArgumentAsString(1);
		String value = frame.getArgumentAsString(2);
		JSONObject json = getKeyValueStringMessage(key, value);
		sendPushMessage(PushMessagesReceiver.KeyValueStringPushMessageAction,address, json);
	}

	private void processSubscribeToDigitalPinMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		// if(frame.getArguments().size()!=3)return;
		String address = frame.getArgumentAsString(0);
		List<Integer> pinsArr = new ArrayList<Integer>();
		for (int i = 1; i < frame.getArguments().size(); i++)
			pinsArr.add((int) (frame.getArgument(i)[0] & 0xff));
		JSONObject json = getSubscribeToDigitalPinMessage(pinsArr);
		sendPushMessage(PushMessagesReceiver.SubscribeToDigitalPinPushMessageAction,address, json);
	}

	private void processUnsubscribeToDigitalPinMessage(ShieldFrame frame) {
		if (frame == null)
			return;
		// if(frame.getArguments().size()!=3)return;
		String address = frame.getArgumentAsString(0);
		List<Integer> pinsArr = new ArrayList<Integer>();
		for (int i = 1; i < frame.getArguments().size(); i++)
			pinsArr.add((int) (frame.getArgument(i)[0] & 0xff));
		JSONObject json = getUnsubscribeToDigitalPinMessage(pinsArr);
		sendPushMessage(PushMessagesReceiver.UnsubscribeToDigitalPinPushMessageAction,address, json);
	}

	public void sendPushMessage(String action, String to, JSONObject json) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("to", to);
		params.put("from", ParseInstallation.getCurrentInstallation().getInstallationId());
		params.put("payload", json);
		params.put("action", action);
		ParseCloud.callFunctionInBackground("sendRemote1SheeldPushMessage", params, new FunctionCallback<String>() {
		   public void done(String success, ParseException e) {
		       if (e == null) {
		          // Push sent successfully
		    	   Log.d("Push", "Push Sent!");
		       }
		   }
		});
	}

	private JSONObject getPinModeMessage(int pin, int mode) {
		try {
			JSONObject json = new JSONObject();
			// json.put("type","PIN_MODE");
			json.put("pin", pin);
			json.put("mode", mode);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getDigitalWriteMessage(int pin, boolean value) {
		try {
			JSONObject json = new JSONObject();
			json.put("pin", pin);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getAnalogWriteMessage(int pin, int value) {
		try {
			JSONObject json = new JSONObject();
			json.put("pin", pin);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getKeyValueFloatMessage(String key, float value) {
		try {
			JSONObject json = new JSONObject();
			json.put("key", key);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getKeyValueStringMessage(String key, String value) {
		try {
			JSONObject json = new JSONObject();
			json.put("key", key);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getSubscribeToDigitalPinMessage(List<Integer> pinsArr) {
		try {
			JSONArray pins = new JSONArray();
			for (Integer pin : pinsArr) {
				JSONObject obj = new JSONObject();
				obj.put("pin", pin);
				pins.put(obj);
			}

			JSONObject json = new JSONObject();
			json.put("pins", pins);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getUnsubscribeToDigitalPinMessage(List<Integer> pinsArr) {
		try {
			JSONArray pins = new JSONArray();
			for (Integer pin : pinsArr) {
				JSONObject obj = new JSONObject();
				obj.put("pin", pin);
				pins.put(obj);
			}

			JSONObject json = new JSONObject();
			json.put("pins", pins);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private Map<Integer, List<String>> getPinsSubscribers() {
		HashMap<Integer, List<String>> pinsSubscribers = new HashMap<Integer, List<String>>();
		for (String device : subscribedDevices.keySet()) {
			for (Integer pin : subscribedDevices.get(device)) {
				if (!pinsSubscribers.containsKey(pin)) {
					List<String> deviceList = new ArrayList<String>();
					deviceList.add(device);
					pinsSubscribers.put(pin, deviceList);
				} else {
					List<String> deviceList = pinsSubscribers.get(pin);
					deviceList.add(device);
					pinsSubscribers.put(pin, deviceList);
				}
			}
		}
		return pinsSubscribers;
	}

	public void subscribeToDigitalPins(String address, List<Integer> pins) {
		if (pins.size() <= 0)
			return;
		if (!subscribedDevices.containsKey(address)) {
			subscribedDevices.put(address, pins);
		} else {
			List<Integer> tempList = subscribedDevices.get(address);
			for (Integer pin : pins) {
				if (!tempList.contains(pin)) {
					tempList.add(pin);
					getApplication().getAppFirmata().pinMode(pin,
							ArduinoFirmata.INPUT);
				}
			}
			subscribedDevices.put(address, tempList);
		}
		synchronized (digitalPinSubscribtionNotificationsLimit) {
			digitalPinSubscribtionNotificationsLimit.put(address,
					MAXIMUM_NOTIFICATION_LIMIT);
		}
		Map<Integer,Boolean> initialPinValue=new HashMap<Integer, Boolean>();
		for(Integer pin:pins){
			initialPinValue.put(pin, getApplication().getAppFirmata().digitalRead(pin));
		}
		sendDigitalPinSubscribtionResponse(address,initialPinValue);
	}

	public void unSubscribeToDigitalPins(String address, List<Integer> pins) {
		if (pins.size() <= 0)
			return;
		if (!subscribedDevices.containsKey(address)) {
			return;
		} else {
			List<Integer> tempList = subscribedDevices.get(address);
			for (Integer pin : pins) {
				if (tempList.contains(pin))
					tempList.remove(pin);
			}
			if (tempList.size() <= 0) {
				subscribedDevices.remove(address);
				digitalPinSubscribtionNotificationsLimit.remove(address);
			} else
				subscribedDevices.put(address, tempList);
		}
	}

	private void sendDigitalPinSubscribtionResponse(String to,
			Map<Integer, Boolean> changedPins) {
		try {
			JSONArray pins = new JSONArray();
			for (Integer pin : changedPins.keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("pin", pin);
				obj.put("value", changedPins.get(pin));
				pins.put(obj);
			}
			JSONObject json = new JSONObject();
			json.put("pins", pins);
			json.put("push_limit", MAXIMUM_NOTIFICATION_LIMIT);
			json.put("current_push",
					digitalPinSubscribtionNotificationsLimit.get(to));
			sendPushMessage(PushMessagesReceiver.DigitalPinSubscribtionResponsePushMessageAction,to, json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

	@Override
	public void reset() {
		if (digitalPinsChangeCheckerThread != null) {
			digitalPinsChangeCheckerThread.stopRunning();
		}
		isReportedToAnalytics=false;
	}

	private class DigitalPinsChangeChecker extends Thread {
		public DigitalPinsChangeChecker() {
			// TODO Auto-generated constructor stub
			start();
		}

		private void stopRunning() {
			if (this.isAlive())
				this.interrupt();
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {

				try {
					synchronized (digitalPinFloatingGuard) {
						for (Integer pin : getPinsSubscribers().keySet()) {
							digitalPinFloatingGuard.put(pin, true);
						}
					}
					Thread.sleep(500);

					synchronized (digitalPinFloatingGuard) {
						for (String device : subscribedDevices.keySet()) {
							Map<Integer, Boolean> changedPins = new HashMap<Integer, Boolean>();
							for (Integer pin : subscribedDevices.get(device)) {
								if (!lastSentValues.containsKey(pin)) {
									boolean value = getApplication()
											.getAppFirmata().digitalRead(pin);
									synchronized (currentValues) {
										currentValues.put(pin, value);
									}
									changedPins.put(pin, value);
								} else if (currentValues.containsKey(pin)
										&& digitalPinFloatingGuard.get(pin)
										&& lastSentValues.get(pin) != currentValues
												.get(pin)) {
									changedPins
											.put(pin, currentValues.get(pin));
								}
							}
							if (changedPins.size() > 0
									&& digitalPinSubscribtionNotificationsLimit
											.containsKey(device)
									&& digitalPinSubscribtionNotificationsLimit
											.get(device) > 0) {
								sendDigitalPinSubscribtionResponse(device,
										changedPins);
								for (Integer pin : getPinsSubscribers()
										.keySet()) {
									if (currentValues.containsKey(pin))
										lastSentValues.put(pin,
												currentValues.get(pin));
								}
								synchronized (digitalPinSubscribtionNotificationsLimit) {
									digitalPinSubscribtionNotificationsLimit
											.put(device,
													digitalPinSubscribtionNotificationsLimit
															.get(device) - 1);
								}

							}

						}

					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
}
