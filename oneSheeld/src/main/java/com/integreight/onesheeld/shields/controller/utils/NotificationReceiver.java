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

import java.util.ArrayList;

/**
 * Created by Moustafa Nasr on 12/15/2015.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("OverrideAbstract")
public class NotificationReceiver extends NotificationListenerService{

    Context context;

    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }
    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {

        Intent msgrcv = new Intent("NotificationDetailsMessage");
        String pack = sbn.getPackageName();
        String ticker = sbn.getNotification().tickerText.toString();
        msgrcv.putExtra("package", pack);
        msgrcv.putExtra("ticker", ticker);
        msgrcv.putExtra("time", sbn.getPostTime());

        //Log.d("notificationReceiver", "-----Notifaction Start------");
        //Log.d("notificationReceiver", pack);
        //Log.d("notificationReceiver", ticker);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Bundle extras = sbn.getNotification().extras;

            //Log.d("notificationReceiver", "-----------");
            ArrayList<String> extrasArray = new ArrayList<>();
            for (String key : extras.keySet()) {
                switch (key) {
                    case Notification.EXTRA_TEXT:
                    case Notification.EXTRA_TITLE:
                    case Notification.EXTRA_SUB_TEXT:
                    case Notification.EXTRA_INFO_TEXT:
                    case Notification.EXTRA_TITLE_BIG:
                        if (extras.getString(key) != null) {
                            //Log.d("notificationReceiver", key + ":" + extras.getString(key));
                            extrasArray.add(extras.getString(key));
                        }
                        break;
//                case "android.showWhen":
//                    Log.d("notificationReceiver","showWhen:"+extras.get(key).toString());
//                    break;
                    case Notification.EXTRA_BIG_TEXT:
                        //Log.d("notificationReceiver", "bigText:" + ((SpannableString) extras.get(key)).toString());
                        extrasArray.add(((SpannableString) extras.get(key)).toString());
                        break;
                    case Notification.EXTRA_TEXT_LINES:
                        CharSequence[] strings = (CharSequence[]) extras.get(key);
                        for (CharSequence txt : strings) {
                            //Log.d("notificationReceiver", txt.toString());
                            extrasArray.add(txt.toString());
                        }
                        break;
                    default:
                        //Log.d("notificationReceiver",key);
                        break;
                }
            }
            msgrcv.putStringArrayListExtra("extras", extrasArray);
        }
        //Log.d("notificationReceiver","-----Notifaction End------");
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @Override

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("notificationReceiver","Notification Removed");

    }

}
