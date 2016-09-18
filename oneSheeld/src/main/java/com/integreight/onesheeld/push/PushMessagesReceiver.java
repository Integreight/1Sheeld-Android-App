package com.integreight.onesheeld.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.integreight.onesheeld.OneSheeldApplication;


public class PushMessagesReceiver extends FirebaseMessagingService {
    private static final String TAG = "PushMessagesReceiver";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null && remoteMessage.getData() != null && remoteMessage.getData().size() >= 1) {
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();
            String url = remoteMessage.getData().get("url");
            if(title!=null && title.length()>0 && message!=null && message.length()>0 && url!=null && url.length()>0)
            showNotificationWithUrl(this, title, message, url);
        }
    }

    static protected void showNotificationWithUrl(Context context, String title, String notificationText, String url) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                context);
        build.setSmallIcon(OneSheeldApplication.getNotificationIcon());
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
}