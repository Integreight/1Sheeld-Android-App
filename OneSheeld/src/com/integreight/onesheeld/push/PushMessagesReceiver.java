package com.integreight.onesheeld.push;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.RemoteOneSheeldShield;
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
	public static final String KeyValueFloatPushMessageAction = "com.integreight.onesheeld.push.KeyValueFloatPushMessage";
	public static final String KeyValueStringPushMessageAction = "com.integreight.onesheeld.push.KeyValueStringPushMessage";
	private static long lastNotifiedTimeStamp;
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			String channel = intent.getExtras().getString("com.parse.Channel");
			JSONObject json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));
			Log.d(TAG, "got action " + action + " on channel " + channel
					+ " with:");
			
			OneSheeldApplication app = (OneSheeldApplication) context
					.getApplicationContext();
			Iterator<?> itr = json.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				Log.d(TAG, "..." + key + " => " + json.getString(key));
			}
			String from =json.getString("from");
			if (app.getAppFirmata() == null
					|| (app.getAppFirmata() != null && !app.getAppFirmata()
							.isOpen())) {
				if(lastNotifiedTimeStamp+60<System.nanoTime()/1000000000||lastNotifiedTimeStamp==0){
				showNotification(context,"Incoming connection!");
				lastNotifiedTimeStamp=System.nanoTime()/1000000000;
				}
				return;
			}
			
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
				sendPushMessage(from, digitalReadResponseJson);

			} else if (action.equals(DigitalReadResponsePushMessageAction)) {
				ShieldFrame frame= new ShieldFrame(UIShield.REMOTEONESHEELD_SHIELD.getId(), RemoteOneSheeldShield.DIGITAL_READ_RESPONSE);
				frame.addStringArgument(from);
				frame.addByteArgument((byte)json.getInt("pin"));
				frame.addBooleanArgument(json.getBoolean("value"));
				app.getAppFirmata().sendShieldFrame(frame);

			} else if (action.equals(AnalogWritePushMessageAction)) {
				app.getAppFirmata().pinMode(json.getInt("pin"),
						ArduinoFirmata.OUTPUT);
				app.getAppFirmata().analogWrite(json.getInt("pin"),
						json.getInt("value"));
			}
			else if (action.equals(KeyValueFloatPushMessageAction)) {
				ShieldFrame frame= new ShieldFrame(UIShield.REMOTEONESHEELD_SHIELD.getId(), RemoteOneSheeldShield.FLOAT_MESSAGE_RESPONSE);
				frame.addStringArgument(from);
				frame.addStringArgument(json.getString("key"));
				frame.addFloatArgument((float) json.getDouble("value"));
				app.getAppFirmata().sendShieldFrame(frame);
			}
			else if (action.equals(KeyValueStringPushMessageAction)) {
				ShieldFrame frame= new ShieldFrame(UIShield.REMOTEONESHEELD_SHIELD.getId(), RemoteOneSheeldShield.STRING_MESSAGE_RESPONSE);
				frame.addStringArgument(from);
				frame.addStringArgument(json.getString("key"));
				frame.addStringArgument(json.getString("value"));
				app.getAppFirmata().sendShieldFrame(frame);
			}else {
				return;
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
		push.setQuery(query);
		push.setData(json);// push.setMessage(json.toString());
		push.setExpirationTimeInterval(10);// 10 seconds timeout
		push.sendInBackground();
	}
	
	protected void showNotification(Context context,String notificationText) {
		// TODO Auto-generated method stub
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				context);
		build.setSmallIcon(R.drawable.white_ee_icon);
		build.setContentTitle(notificationText);
		build.setContentText("Someone is trying to control your 1Sheeld");
//		Uri alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//		build.setSound(alertSound);
		build.setTicker(notificationText);
		build.setWhen(System.currentTimeMillis());
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		build.setContentIntent(intent);
		Notification notification = build.build();
		notification.flags=Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL |Notification.DEFAULT_SOUND;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, notification);
	}
}