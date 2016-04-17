package com.integreight.onesheeld.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.utils.WakeLocker;

public class OneSheeldService extends Service {
    public static boolean isBound = false;
    SharedPreferences sharedPrefs;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String  deviceName;
    OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
        @Override
        public void onDisconnect(OneSheeldDevice device) {
            super.onDisconnect(device);
            stopSelf();
        }

        @Override
        public void onConnect(OneSheeldDevice device) {
            super.onConnect(device);
            showNotification();
        }
    };
    OneSheeldErrorCallback errorCallback=new OneSheeldErrorCallback() {
        @Override
        public void onError(OneSheeldDevice device, OneSheeldError error) {
            super.onError(device, error);
            stopSelf();
        }
    };

    OneSheeldApplication app;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        app = (OneSheeldApplication) getApplication();
        sharedPrefs = this.getSharedPreferences("com.integreight.onesheeld",
                Context.MODE_PRIVATE);
        isBound = false;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (intent.getExtras() != null) {
            deviceName = intent.getExtras().getString(
                    ArduinoConnectivityPopup.EXTRA_DEVICE_NAME);
            // Attempt to connect to the device
            OneSheeldSdk.getManager().addConnectionCallback(connectionCallback);
            OneSheeldSdk.getManager().addErrorCallback(errorCallback);
        }
        showNotification();
        WakeLocker.acquire(this);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        isBound = true;
        // return mBinder;
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
       OneSheeldSdk.getManager().disconnectAll();
        hideNotifcation();
        isBound = false;
        WakeLocker.release();
        super.onDestroy();
    }

    private void showNotification() {
        NotificationCompat.Builder build = new NotificationCompat.Builder(this);
        build.setSmallIcon(OneSheeldApplication.getNotificationIcon());
        build.setContentText(getString(R.string.connection_notification_connected_to)+": " + deviceName);
        build.setContentTitle(getString(R.string.connection_notification_1sheeld_is_connected));
        build.setTicker(getString(R.string.connection_notification_1sheeld_is_connected));
        build.setWhen(System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        startForeground(1, notification);
    }

    private void hideNotifcation() {
        stopForeground(true);
    }

}
