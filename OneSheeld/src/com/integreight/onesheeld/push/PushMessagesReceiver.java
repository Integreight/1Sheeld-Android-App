package com.integreight.onesheeld.push;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.utils.Log;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

public class PushMessagesReceiver extends BroadcastReceiver {
	private static final String TAG = "PushMessagesReceiver";
	public static final String PinModePushMessageAction = "com.integreight.onesheeld.push.PinModePushMessage";
	public static final String DigitalWritePushMessageAction = "com.integreight.onesheeld.push.DigitalWritePushMessage";
	public static final String DigitalReadRequestPushMessageAction = "com.integreight.onesheeld.push.DigitalReadRequestPushMessage";
	public static final String DigitalReadResponsePushMessageAction = "com.integreight.onesheeld.push.DigitalReadResponsePushMessage";
	public static final String AnalogWritePushMessageAction = "com.integreight.onesheeld.push.AnalogWritePushMessage";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			OneSheeldApplication app = (OneSheeldApplication) context
					.getApplicationContext();
			if (app.getAppFirmata() == null
					|| (app.getAppFirmata() != null && !app.getAppFirmata()
							.isOpen())) {
				return;
			}
			String action = intent.getAction();
			String channel = intent.getExtras().getString("com.parse.Channel");
			JSONObject json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));
			Log.d(TAG, "got action " + action + " on channel " + channel
					+ " with:");
			if (action.equals(PinModePushMessageAction)) {
				app.getAppFirmata().pinMode(
						json.getInt("pin"),
						(json.getInt("mode") == 0) ? ArduinoFirmata.INPUT
								: ArduinoFirmata.OUTPUT);
			} else if (action.equals(DigitalWritePushMessageAction)) {
				app.getAppFirmata().pinMode(json.getInt("pin"),
						ArduinoFirmata.OUTPUT);
				app.getAppFirmata().digitalWrite(json.getInt("pin"),
						json.getBoolean("value"));
			} else if (action.equals(DigitalReadRequestPushMessageAction)) {

				JSONObject digitalReadResponseJson = new JSONObject();
				digitalReadResponseJson
						.put("action",
								PushMessagesReceiver.DigitalReadResponsePushMessageAction);
				digitalReadResponseJson.put("pin", json.getInt("pin"));
				digitalReadResponseJson.put("value", app.getAppFirmata()
						.digitalRead(json.getInt("pin")));
				sendPushMessage(json.getString("from"), digitalReadResponseJson);

			} else if (action.equals(DigitalReadResponsePushMessageAction)) {

			} else if (action.equals(AnalogWritePushMessageAction)) {
				app.getAppFirmata().pinMode(json.getInt("pin"),
						ArduinoFirmata.OUTPUT);
				app.getAppFirmata().analogWrite(json.getInt("pin"),
						json.getInt("value"));
			} else {
				return;
			}
			Iterator<?> itr = json.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				Log.d(TAG, "..." + key + " => " + json.getString(key));
			}
		} catch (JSONException e) {
			Log.d(TAG, "JSONException: " + e.getMessage());
		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.getMessage());
			Crashlytics.logException(e);
		}
	}

	private void sendPushMessage(String installationId, JSONObject json) {
		try {
			json.put("from", ParseInstallation.getCurrentInstallation()
					.getInstallationId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereEqualTo("installationId", installationId);
		ParsePush push = new ParsePush();
		push.setExpirationTimeInterval(10);// 10 seconds timeout
		push.setQuery(query);
		push.setData(json);// push.setMessage(json.toString());
		push.sendInBackground();
	}
}