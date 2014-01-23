package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.ControllerParent;

public class NotificationShield extends ControllerParent<NotificationShield> {
	private NotificationEventHandler eventHandler;
	private String lastNotificationText;
	private static final byte NOTIFICATION_COMMAND = (byte) 0x34;
	private static final byte NOTIFY_PHONE_METHOD_ID = (byte) 0x01;

	public String getLastNotificationText() {
		return lastNotificationText;
	}

	public NotificationShield() {
		super();
	}

	public NotificationShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void onUartReceive(byte[] data) {
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
		super.onUartReceive(data);
	}

	protected void showNotification(String notificationText) {
		// TODO Auto-generated method stub
		Notification.Builder build = new Notification.Builder(activity);
		build.setSmallIcon(R.drawable.white_ee_icon);
		build.setContentTitle(notificationText);
		build.setContentText("Notification received from 1Sheeld!");
		build.setTicker(notificationText);
		// build.setContentInfo("");
		build.setWhen(System.currentTimeMillis());
		Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
		// PendingIntent pendingIntent = PendingIntent.getService(this, 0, new
		// Intent(this, OneSheeldService.class), 0);

		// build.addAction(R.drawable.action_cancel,
		// "Close Service",pendingIntent );
		// build.addAction(R.drawable.action_cancel,
		// "Close Service",pendingIntent );
		// build.addAction(R.drawable.action_cancel,
		// "Close Service",pendingIntent );
		// Intent notificationIntent = new Intent(activity, MainActivity.class);
		// notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//
		// PendingIntent intent = PendingIntent.getActivity(this, 0,
		// notificationIntent, 0);

		// build.setContentIntent(intent);
		Vibrator v = (Vibrator) activity
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
		Notification notification = build.build();
		NotificationManager notificationManager = (NotificationManager) activity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(2, notification);
	}

	public void setNotificationEventHandler(
			NotificationEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		getApplication().getAppFirmata().initUart();
	}

	public interface NotificationEventHandler {
		void onNotificationReceive(String notificationText);
	}

}
