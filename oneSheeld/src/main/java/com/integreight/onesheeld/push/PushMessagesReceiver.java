package com.integreight.onesheeld.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;
import com.parse.ParsePushBroadcastReceiver;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;


public class PushMessagesReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "PushMessagesReceiver";
    public static final String NotificationWithUrlPushMessageAction = "com.integreight.onesheeld.push.NotificationWithUrl";

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
                Iterator<?> itr = json.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    Log.d(TAG, "..." + key + " => " + json.getString(key));
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            CrashlyticsUtils.logException(e);
        }
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