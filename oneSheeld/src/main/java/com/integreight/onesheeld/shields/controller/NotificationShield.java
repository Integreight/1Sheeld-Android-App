package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.NotificationReceiver;

import java.util.ArrayList;

public class NotificationShield extends ControllerParent<NotificationShield> {
    private NotificationEventHandler eventHandler;
    private String lastNotificationText;
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

    protected void showNotification(String notificationText) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                activity);
        build.setSmallIcon(R.drawable.white_ee_icon);
        build.setContentTitle(notificationText);
        build.setContentText("Notification received from 1Sheeld!");
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) activity
                .getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Intent notificationIntent = new Intent(activity, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        NotificationManager notificationManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    public Boolean startNotificationReceiver(){
        ContentResolver contentResolver = activity.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = activity.getPackageName();
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            return false;
        }
        else
        {
            activity.startService(new Intent(activity, NotificationReceiver.class));
            LocalBroadcastManager.getInstance(activity).registerReceiver(onNotice, new IntentFilter("NotificationDetailsMessage"));
            return true;
        }
    }

    public void stopNotificationReceiver(){
        activity.stopService(new Intent(activity, NotificationReceiver.class));
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(onNotice);
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String ticker = intent.getStringExtra("ticker");
            Long time = intent.getLongExtra("time",0);
            ArrayList<String> extras = intent.getStringArrayListExtra("extras");

            StringBuilder text = new StringBuilder("");
            text.append(pack);
            text.append("\n"+ticker);
            if (extras != null &&extras.size() > 0){
                for (String txt:extras){
                    text.append("\n"+txt);
                }
            }

            if (eventHandler != null && !pack.equals("com.integreight.onesheeld")){
                //eventHandler.onNotifiactionArrived(text.toString());

                if (pack.equals("com.facebook.orca"))
                    if (extras.size() == 2)
                    eventHandler.onNotifiactionArrived(extras.get(0).toString()+" :"+extras.get(1).toString());
                    else
                    eventHandler.onNotifiactionArrived(extras.get(0).toString()+" :"+extras.get(extras.size() - 2).toString());
                else if (pack.equals("com.whatsapp"))
                    eventHandler.onNotifiactionArrived(ticker+" :"+extras.get(extras.size() - 1).toString());
                else if (!ticker.equals(""))
                    eventHandler.onNotifiactionArrived(ticker);
                else
                    eventHandler.onNotifiactionArrived(extras.get(0).toString());
            }
        }
    };

    public void setNotificationEventHandler(
            NotificationEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public interface NotificationEventHandler {
        void onNotificationReceive(String notificationText);
        void onNotifiactionArrived(String notificationText);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.NOTIFICATION_SHIELD.getId()) {
            String notificationText = frame.getArgumentAsString(0);
            lastNotificationText = notificationText;
            if (frame.getFunctionId() == NOTIFY_PHONE_METHOD_ID) {
                showNotification(notificationText);
                if (eventHandler != null)
                    eventHandler.onNotificationReceive(notificationText);
            }
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        stopNotificationReceiver();
    }

}
