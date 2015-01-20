package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.integreight.onesheeld.OneSheeldApplication;

/**
 * Created by Saad on 1/20/15.
 */
public class CameraTempService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent1 = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
