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
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.PackageItem;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.NotificationObject;
import com.integreight.onesheeld.shields.controller.utils.NotificationReceiver;
import com.integreight.onesheeld.utils.database.NotificationPackageList;

import java.util.ArrayList;

public class NotificationShield extends ControllerParent<NotificationShield> {
    private NotificationEventHandler eventHandler;
    private String lastNotificationText;
    private static final byte NOTIFY_PHONE_METHOD_ID = (byte) 0x01;
    private SparseArray<NotificationObject> notificationObjectArrayList = new SparseArray<>();
    private ArrayList<PackageItem> packageItems = new ArrayList<PackageItem>();

    public String getLastNotificationText() {
        return lastNotificationText;
    }

    public NotificationShield() {
        super();
    }

    public NotificationShield(Activity activity, String tag) {
        super(activity, tag);
         checkDenyList();
    }

    @Override
    public ControllerParent<NotificationShield> init(String tag) {
        checkDenyList();
        return super.init(tag);
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
            LocalBroadcastManager.getInstance(activity).registerReceiver(onRemoval, new IntentFilter("NotificationRemovalMessage"));
            return true;
        }
    }

    public void stopNotificationReceiver(){
        activity.stopService(new Intent(activity, NotificationReceiver.class));
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(onNotice);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(onRemoval);
    }

    public static final String EXTRAS = "extras",JSON_EXTRAS = "jsonExtras";
    private int keyCounter = 1;
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationObject currentNotification = new NotificationObject(intent.getStringExtra(JSON_EXTRAS));
            int currentKey = getNotificationKey(currentNotification.getPackageName(),currentNotification.getNotificationId());
            if (currentKey == 0) {
                notificationObjectArrayList.put(keyCounter, currentNotification);
                keyCounter++;
            }else{
                notificationObjectArrayList.get(currentKey).fromJsonString(intent.getStringExtra(JSON_EXTRAS));
            }

            if (eventHandler != null ){
                if (currentNotification.getPackageName().equals("com.integreight.onesheeld")) {
                    return;
                }
                for (PackageItem item: packageItems){
                    if (currentNotification.getPackageName().equals(item.name)) {
                        return;
                    }
                }

                if (currentNotification.getPackageName().equals("com.facebook.orca")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getTitle().equals(""))
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                    else if (!currentNotification.getTitle().equals(""))
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getTextLines().get(currentNotification.getTextLines().size()-1));
                }else if (currentNotification.getPackageName().equals("com.whatsapp")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getText().equals(""))
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                    else if (currentNotification.getTextLines().size() > 0)
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getTextLines().get(currentNotification.getTextLines().size()-1));
                }else if (currentNotification.getPackageName().equals("com.google.android.gm")) {
                    if (!currentNotification.getBigText().equals(""))
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getBigText());
                    else if(currentNotification.getTextLines().size() > 0)
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getTextLines().get(currentNotification.getTextLines().size()-1));
                }else if (currentNotification.getPackageName().equals("com.google.android.talk")) {
                    if (currentNotification.getTicker().contains(":"))
                        eventHandler.onNotifiactionArrived(currentNotification.getTicker().split(":")[0]+currentNotification.getTitle());
                }else if (currentNotification.getPackageName().equals("com.slack")) {
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                }else if(currentNotification.getPackageName().equals("jp.naver.line.android")){
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                }else if (currentNotification.getPackageName().equals("org.telegram.messenger")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getText().equals(""))
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                    else if (currentNotification.getTextLines().size() > 0)
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getTextLines().get(0));
                }else if (!currentNotification.getTitle().equals(""))
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle());
                else if (!currentNotification.getTicker().equals(""))
                    eventHandler.onNotifiactionArrived(currentNotification.getTicker());
                else
                    eventHandler.onNotifiactionArrived(currentNotification.getPackageName());
            }
        }
    };

    BroadcastReceiver onRemoval = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationObject currentNotification = new NotificationObject(intent.getStringExtra(JSON_EXTRAS));
            for (int notificationsCounter=0;notificationsCounter<notificationObjectArrayList.size();notificationsCounter++){
                if (currentNotification.equals(notificationObjectArrayList.get(notificationsCounter))){
                    notificationObjectArrayList.remove(notificationsCounter);
                    break;
                }
            }
        }
    };

    public void setNotificationEventHandler(
            NotificationEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private NotificationObject getNotification(String packageNmae,int notificationId){
        NotificationObject currentNotification = new NotificationObject(packageNmae,notificationId,00);
        for (int notificationsCounter=0;notificationsCounter<notificationObjectArrayList.size();notificationsCounter++){
            if (currentNotification.equals(notificationObjectArrayList.get(notificationsCounter))){
                return notificationObjectArrayList.get(notificationsCounter);
            }
        }
        return null;
    }

    private int getNotificationKey(String packageNmae,int notificationId){
        NotificationObject currentNotification = new NotificationObject(packageNmae,notificationId,00);
        for (int notificationsCounter=0;notificationsCounter<notificationObjectArrayList.size();notificationsCounter++){
            if (currentNotification.equals(notificationObjectArrayList.get(notificationsCounter))){
                return notificationObjectArrayList.keyAt(notificationsCounter);
            }
        }
        return 0;
    }

    private void dismissNotification(NotificationObject currentNotification){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationReceiver.getThisInstance().cancelNotification(currentNotification.getPackageName(),currentNotification.getTag(),currentNotification.getNotificationId());
            notificationObjectArrayList.remove(getNotificationKey(currentNotification.getPackageName(),currentNotification.getNotificationId()));
        }
    }

    public void checkDenyList() {
//        if (packageItems == null || (packageItems != null && packageItems.size() == 0)) {
            NotificationPackageList db = new NotificationPackageList(activity);
            db.openToWrite();
            packageItems = db.getPlaylist();
            db.close();
            //init();
//        }
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
