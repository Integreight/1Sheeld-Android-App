package com.integreight.onesheeld.shields;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.R;

public class NotificationShield {
	private ArduinoFirmata firmata;
	private NotificationEventHandler eventHandler;
	private String lastNotificationText;
	private Activity activity;
	private static final byte NOTIFICATION_COMMAND = (byte) 0x34;
	private static final byte NOTIFY_PHONE_METHOD_ID = (byte) 0x01;

	public String getLastNotificationText() {
		return lastNotificationText;
	}

	public NotificationShield(ArduinoFirmata firmata, Activity activity) {
		this.firmata = firmata;
		this.activity = activity;
	}

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub
				if (data.length < 2)
					return;
				byte command = data[0];
				byte methodId = data[1];
				int n = data.length - 2;
				byte[] newArray = new byte[n];
				System.arraycopy(data, 2, newArray, 0, n);
				if (command == NOTIFICATION_COMMAND) {
					String notificationText = new String(newArray);
					lastNotificationText = notificationText;
					if (methodId == NOTIFY_PHONE_METHOD_ID) {
						showNotification(notificationText);
						eventHandler.onNotificationReceive(notificationText);
					}

				}

			}
		});
	}

	protected void showNotification(String notificationText) {
		// TODO Auto-generated method stub
		Notification.Builder build=new Notification.Builder(activity);
		build.setSmallIcon(R.drawable.ic_launcher);
		build.setContentTitle(notificationText);
		build.setContentText("Notification received from 1Sheeld!");
		build.setTicker(notificationText);
//		build.setContentInfo("");
		build.setWhen(System.currentTimeMillis());
		Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
//		PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, OneSheeldService.class), 0);
		
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );
//		Intent notificationIntent = new Intent(activity, MainActivity.class);
//		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//	            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//	    PendingIntent intent = PendingIntent.getActivity(this, 0,
//	            notificationIntent, 0);
		
//		build.setContentIntent(intent);
		Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
		Notification notification=build.build();
		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(2, notification);
	}

	public void setFacebookEventHandler(NotificationEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public interface NotificationEventHandler {
		void onNotificationReceive(String notificationText);
	}

}
