package com.integreight.onesheeld.shields.controller.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.util.Log;

import com.integreight.onesheeld.shields.controller.NotificationShield;

import java.util.ArrayList;

/**
 * Created by Moustafa Nasr on 12/15/2015.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("OverrideAbstract")
public class NotificationReceiver extends NotificationListenerService{

    Context context;
    static NotificationReceiver thisInstance;

    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        thisInstance = this;
    }

    public static NotificationReceiver getThisInstance() {
        return thisInstance;
    }

    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {

        Intent msgrcv = new Intent("NotificationDetailsMessage");
        NotificationObject currentNotification = null;
        currentNotification = new NotificationObject(sbn.getPackageName(),sbn.getId(),sbn.getNotification().when);
        currentNotification.setTag(sbn.getTag());
        if (sbn.getNotification().tickerText != null)
            currentNotification.setTicker(String.valueOf(sbn.getNotification().tickerText));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Bundle extras = sbn.getNotification().extras;
            ArrayList<String> extrasArray = new ArrayList<>();
            ArrayList<String> textLines = new ArrayList<String>();
            for (String key : extras.keySet()) {
                switch (key) {
                    case Notification.EXTRA_TEXT:
                        if (extras.get(key) != null) {
                            currentNotification.setText(extras.get(key).toString());
                        }
                        break;
                    case Notification.EXTRA_TITLE:
                        if (extras.getString(key) != null) {
                            currentNotification.setTitle(extras.getString(key));
                        }
                        break;
                    case Notification.EXTRA_SUB_TEXT:
                        if (extras.getString(key) != null) {
                            currentNotification.setSubText(extras.getString(key));
                        }
                        break;
                    case Notification.EXTRA_INFO_TEXT:
                        if (extras.getString(key) != null) {
                            currentNotification.setInfoText(extras.getString(key));
                        }
                        break;
                    case Notification.EXTRA_SUMMARY_TEXT:
                        if (extras.getString(key) != null) {
                            currentNotification.setSubText(extras.getString(key));
                        }
                        break;
                    case Notification.EXTRA_TITLE_BIG:
                        if (extras.getString(key) != null) {
                            currentNotification.setBigTitle(extras.getString(key));
                        }
                        break;
                    case Notification.EXTRA_BIG_TEXT:
                        currentNotification.setBigText(((SpannableString) extras.get(key)).toString());
                        break;
                    case Notification.EXTRA_TEXT_LINES:
                        CharSequence[] strings = (CharSequence[]) extras.get(key);
                        for (CharSequence txt : strings) {
                            textLines.add(txt.toString());
                        }
                        currentNotification.setTextLines(textLines);
                        break;
                    default:
                        //Log.d("notificationReceiver",key);
                        break;
                }
            }
        }
        msgrcv.putExtra(NotificationShield.JSON_EXTRAS, currentNotification.toJsonString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @Override

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("notificationReceiver","Notification Removed");
        Intent msgrcv = new Intent("NotificationRemovalMessage");
        NotificationObject currentNotification = null;
        currentNotification = new NotificationObject(sbn.getPackageName(),sbn.getId(),sbn.getPostTime());
        currentNotification.setTag(sbn.getTag());
        msgrcv.putExtra(NotificationShield.JSON_EXTRAS, currentNotification.toJsonString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

}
