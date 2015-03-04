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

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.utils.WakeLocker;

public class OneSheeldService extends Service {
    public static boolean isBound = false;
    SharedPreferences sharedPrefs;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String deviceAddress, deviceName;
    private ArduinoFirmataEventHandler arduinoEventHandler = new ArduinoFirmataEventHandler() {

        @Override
        public void onError(String errorMessage) {
            stopSelf();
        }

        @Override
        public void onConnect() {
            showNotification();
        }

        @Override
        public void onClose(boolean closedManually) {
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
            deviceAddress = intent.getExtras().getString(
                    ArduinoConnectivityPopup.EXTRA_DEVICE_ADDRESS);
            deviceName = intent.getExtras().getString(
                    ArduinoConnectivityPopup.EXTRA_DEVICE_NAME);
            BluetoothDevice device = mBluetoothAdapter
                    .getRemoteDevice(deviceAddress);
            // Attempt to connect to the device
            app.getAppFirmata().addEventHandler(arduinoEventHandler);
            app.getAppFirmata().connect(device);
        }
        WakeLocker.acquire(this);
        return START_REDELIVER_INTENT;
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
        while (!app.getAppFirmata().close())
            ;
        hideNotifcation();
        isBound = false;
        WakeLocker.release();
        super.onDestroy();
    }

    private void showNotification() {
        NotificationCompat.Builder build = new NotificationCompat.Builder(this);
        build.setSmallIcon(R.drawable.white_ee_icon);
        build.setContentText("To: " + deviceName);
        build.setContentTitle("1Sheeld is connected! ");
        build.setTicker("1Sheeld is connected!");
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

    public ArduinoFirmata getFirmata() {
        return app.getAppFirmata();
    }

}
