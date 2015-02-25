package com.integreight.onesheeld.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PushMessagesReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "PushMessagesReceiver";
    public static final String PinModePushMessageAction = "com.integreight.onesheeld.push.PinModePushMessage";
    public static final String DigitalWritePushMessageAction = "com.integreight.onesheeld.push.DigitalWritePushMessage";
    public static final String DigitalReadRequestPushMessageAction = "com.integreight.onesheeld.push.DigitalReadRequestPushMessage";
    public static final String DigitalReadResponsePushMessageAction = "com.integreight.onesheeld.push.DigitalReadResponsePushMessage";
    public static final String SubscribeToDigitalPinPushMessageAction = "com.integreight.onesheeld.push.SubscribeToDigitalPinPushMessage";
    public static final String UnsubscribeToDigitalPinPushMessageAction = "com.integreight.onesheeld.push.UnsubscribeToDigitalPinPushMessage";
    public static final String DigitalPinSubscribtionResponsePushMessageAction = "com.integreight.onesheeld.push.DigitalPinSubscribtionResponsePushMessage";
    public static final String AnalogWritePushMessageAction = "com.integreight.onesheeld.push.AnalogWritePushMessage";
    public static final String KeyValueFloatPushMessageAction = "com.integreight.onesheeld.push.KeyValueFloatPushMessage";
    public static final String KeyValueStringPushMessageAction = "com.integreight.onesheeld.push.KeyValueStringPushMessage";
    public static final String NotificationWithUrlPushMessageAction = "com.integreight.onesheeld.push.NotificationWithUrl";
    private static long lastNotifiedTimeStamp;

    @Override
    public void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        try {
            String channel = intent.getExtras().getString(ParsePushBroadcastReceiver.KEY_PUSH_CHANNEL);
            JSONObject json = new JSONObject(intent.getExtras().getString(
                    ParsePushBroadcastReceiver.KEY_PUSH_DATA));
            if (json.has("action")) {
                String action = json.getString("action");
                Log.d(TAG, "got action " + action + " on channel " + channel
                        + " with:");
                if (action.equals(NotificationWithUrlPushMessageAction)) {
                    String title = json.getString("notification_title");
                    String message = json.getString("notification_message");
                    String url = json.getString("notification_url");
                    showNotificationWithUrl(context, title, message, url);
                    return;
                }
                OneSheeldApplication app = (OneSheeldApplication) context
                        .getApplicationContext();
                Iterator<?> itr = json.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    Log.d(TAG, "..." + key + " => " + json.getString(key));
                }
                String from = json.getString("from");
                String myAddress = ParseInstallation.getCurrentInstallation()
                        .getInstallationId();
                if (from.equals(myAddress))
                    return;
                if (app.getAppFirmata() == null
                        || (app.getAppFirmata() != null && !app.getAppFirmata()
                        .isOpen())) {
                    if (lastNotifiedTimeStamp + 60 < System.nanoTime() / 1000000000
                            || lastNotifiedTimeStamp == 0) {
                        showNotification(context, "Incoming connection!");
                        lastNotifiedTimeStamp = System.nanoTime() / 1000000000;
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
                    JSONArray jsonPinArray = new JSONArray();
                    JSONObject pin = new JSONObject();
                    pin.put("pin", json.getJSONArray("pins").getJSONObject(0)
                            .getInt("pin"));
                    pin.put("value",
                            app.getAppFirmata().digitalRead(
                                    json.getJSONArray("pins").getJSONObject(0)
                                            .getInt("pin")));
                    jsonPinArray.put(pin);
                    digitalReadResponseJson.put("pins", jsonPinArray);
                    app.remoteOneSheeldController.sendPushMessage(PushMessagesReceiver.DigitalReadResponsePushMessageAction, from, digitalReadResponseJson);

                } else if (action.equals(DigitalReadResponsePushMessageAction)) {
                    List<Integer> pins = new ArrayList<Integer>();
                    JSONArray pinsArr = json.getJSONArray("pins");
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            pins.add(pinsArr.getJSONObject(i).getInt("pin"));
                        }
                    }

                    ShieldFrame frame = new ShieldFrame(
                            UIShield.REMOTEONESHEELD_SHIELD.getId(),
                            RemoteOneSheeldShield.DIGITAL_READ_RESPONSE);
                    frame.addStringArgument(from);
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            byte pin = (byte) (pinsArr.getJSONObject(i).getInt(
                                    "pin") & 0xff);
                            byte value = (byte) ((pinsArr.getJSONObject(i)
                                    .getBoolean("value")) ? 1 : 0);
                            byte pinAndValue = (byte) ((pin | (value << 7)) & 0xff);
                            frame.addByteArgument(pinAndValue);
                        }
                    }
                    app.getAppFirmata().sendShieldFrame(frame,true);

                } else if (action.equals(AnalogWritePushMessageAction)) {
                    app.getAppFirmata().pinMode(json.getInt("pin"),
                            ArduinoFirmata.OUTPUT);
                    app.getAppFirmata().analogWrite(json.getInt("pin"),
                            json.getInt("value"));
                } else if (action.equals(KeyValueFloatPushMessageAction)) {
                    ShieldFrame frame = new ShieldFrame(
                            UIShield.REMOTEONESHEELD_SHIELD.getId(),
                            RemoteOneSheeldShield.FLOAT_MESSAGE_RESPONSE);
                    frame.addStringArgument(from);
                    frame.addStringArgument(json.getString("key"));
                    frame.addFloatArgument((float) json.getDouble("value"));
                    app.getAppFirmata().sendShieldFrame(frame,true);
                } else if (action.equals(KeyValueStringPushMessageAction)) {
                    ShieldFrame frame = new ShieldFrame(
                            UIShield.REMOTEONESHEELD_SHIELD.getId(),
                            RemoteOneSheeldShield.STRING_MESSAGE_RESPONSE);
                    frame.addStringArgument(from);
                    frame.addStringArgument(json.getString("key"));
                    frame.addStringArgument(json.getString("value"));
                    app.getAppFirmata().sendShieldFrame(frame,true);
                } else if (action
                        .equals(SubscribeToDigitalPinPushMessageAction)) {
                    List<Integer> pins = new ArrayList<Integer>();
                    JSONArray pinsArr = json.getJSONArray("pins");
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            pins.add(pinsArr.getJSONObject(i).getInt("pin"));
                        }
                    }
                    app.remoteOneSheeldController.subscribeToDigitalPins(from,
                            pins);
                } else if (action
                        .equals(UnsubscribeToDigitalPinPushMessageAction)) {
                    List<Integer> pins = new ArrayList<Integer>();
                    JSONArray pinsArr = json.getJSONArray("pins");
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            pins.add(pinsArr.getJSONObject(i).getInt("pin"));
                        }
                    }
                    app.remoteOneSheeldController.unSubscribeToDigitalPins(
                            from, pins);
                } else if (action
                        .equals(DigitalPinSubscribtionResponsePushMessageAction)) {
                    List<Integer> pins = new ArrayList<Integer>();
                    JSONArray pinsArr = json.getJSONArray("pins");
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            pins.add(pinsArr.getJSONObject(i).getInt("pin"));
                        }
                    }

                    ShieldFrame frame = new ShieldFrame(
                            UIShield.REMOTEONESHEELD_SHIELD.getId(),
                            RemoteOneSheeldShield.DIGITAL_READ_RESPONSE);
                    frame.addStringArgument(from);
                    if (pinsArr != null) {
                        for (int i = 0; i < pinsArr.length(); i++) {
                            byte pin = (byte) (pinsArr.getJSONObject(i).getInt(
                                    "pin") & 0xff);
                            byte value = (byte) ((pinsArr.getJSONObject(i)
                                    .getBoolean("value")) ? 1 : 0);
                            byte pinAndValue = (byte) ((pin | (value << 7)) & 0xff);
                            frame.addByteArgument(pinAndValue);
                        }
                    }
                    app.getAppFirmata().sendShieldFrame(frame,true);
                } else {
                    return;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    protected void showNotification(Context context, String notificationText) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                context);
        build.setSmallIcon(R.drawable.white_ee_icon);
        build.setContentTitle(notificationText);
        build.setContentText("Someone is trying to control your 1Sheeld");
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    protected void showNotificationWithUrl(Context context, String title, String notificationText, String url) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                context);
        build.setSmallIcon(R.drawable.white_ee_icon);
        build.setContentTitle(title);
        build.setContentText(notificationText);
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Intent newIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        newIntent.putExtras(intent.getExtras());
        context.startActivity(newIntent);
    }
}