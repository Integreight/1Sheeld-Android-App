package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;
import com.integreight.onesheeld.Camera;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.utils.Log;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CameraAidlService extends Service {
    public Queue<CameraShield.CameraCapture> cameraCaptureQueue = new ConcurrentLinkedQueue<>();
    public Queue<CameraShield.CameraCapture> tempQueue = new ConcurrentLinkedQueue<>();
    public static final int SET_REPLYTO = 1;
    public static final int ADD_TO_QUEUE = 2;
    public static final int CRASHED = 3;
    private Messenger replyTo;

    private final Messenger mMesesenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.replyTo != null)
                replyTo = msg.replyTo;
            if (msg.what == SET_REPLYTO) {
                replyTo = msg.replyTo;
                if (msg.getData() != null && msg.getData().getSerializable("queue") != null) {
                    CameraShield.CameraCapture[] captures = Arrays.copyOf(((Object[]) msg.getData().getSerializable("queue")), ((Object[]) msg.getData().getSerializable("queue")).length, CameraShield.CameraCapture[].class);
                    if (cameraCaptureQueue == null)
                        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
                    for (CameraShield.CameraCapture capture : captures) {
                        cameraCaptureQueue.add(capture);
                    }
                    if (!CameraHeadService.isRunning && captures.length > 0) {
                        Intent intent1 = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
                        LocalBroadcastManager.getInstance(CameraAidlService.this).sendBroadcast(intent1);
                    }
                }
            } else if (msg.what == ADD_TO_QUEUE) {
                if (cameraCaptureQueue == null)
                    cameraCaptureQueue = new ConcurrentLinkedQueue<>();
                if (msg.getData() != null && msg.getData().getSerializable("queue") != null) {
                    CameraShield.CameraCapture[] captures = (CameraShield.CameraCapture[]) msg.getData().getSerializable("queue");
                    for (CameraShield.CameraCapture capture : captures) {
                        cameraCaptureQueue.add(capture);
                    }
                }
                cameraCaptureQueue.add((CameraShield.CameraCapture) msg.getData().getSerializable("capture"));//new CameraShield.CameraCapture(msg.getData().getString("flash"), msg.getData().getBoolean("isFront"), msg.getData().getInt("quality"), msg.getData().getLong("tag")));
                if (!CameraHeadService.isRunning) {
                    Intent intent1 = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
                    LocalBroadcastManager.getInstance(CameraAidlService.this).sendBroadcast(intent1);
                }
            }

        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        LocalBroadcastManager.getInstance(
                getApplication().getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME));
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        cameraCaptureQueue = null;
        initCrashlyticsAndUncaughtThreadHandler();
        return mMesesenger.getBinder();
    }

    private void initCrashlyticsAndUncaughtThreadHandler() {
        Thread.UncaughtExceptionHandler myHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, final Throwable arg1) {
                Message msg = Message.obtain(null, CRASHED);
                if (capture != null)
                    cameraCaptureQueue.add(capture);
                Bundle b = new Bundle();
                CameraShield.CameraCapture[] arr = new CameraShield.CameraCapture[]{};
                arr = cameraCaptureQueue.toArray(arr);
                b.putSerializable("queue", arr);
                msg.setData(b);
                try {
                    replyTo.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }

                stopService(new Intent(CameraAidlService.this, CameraHeadService.class));
                CameraHeadService.isRunning = false;
                android.os.Process.killProcess(Process.myPid());
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        if (MainActivity.hasCrashlyticsApiKey(this)) {
            Crashlytics.start(this);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(
                mMessageReceiver);
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        capture = null;
        Intent intent1 = new Intent(getApplication()
                .getApplicationContext(), CameraHeadService.class);
        getApplication().getApplicationContext().stopService(intent1);
        android.os.Process.killProcess(Process.myPid());
        return super.onUnbind(intent);
    }

    CameraShield.CameraCapture capture;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent == null || intent.getExtras() == null || intent.getExtras().get("takenSuccessfuly") == null || intent.getBooleanExtra("takenSuccessfuly", false))
                capture = null;
            else {
                tempQueue = new ConcurrentLinkedQueue<>();
                if (capture != null)
                    tempQueue.add(capture);
                CameraShield.CameraCapture[] arr = new CameraShield.CameraCapture[]{};
                arr = cameraCaptureQueue.toArray(arr);
                for (CameraShield.CameraCapture tempCapture : arr) {
                    tempQueue.add(tempCapture);
                }
                cameraCaptureQueue = new ConcurrentLinkedQueue<>(tempQueue);
            }
            if (!CameraHeadService.isRunning)
                if (!cameraCaptureQueue.isEmpty()) {
                    capture = cameraCaptureQueue.peek();
                    if (capture.isFront()) {
                        sendFrontCaptureImageIntent(cameraCaptureQueue.poll());
                    } else {
                        sendCaptureImageIntent(cameraCaptureQueue.poll());
                    }
                }
        }

    };

    private void sendCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            Intent intent = new Intent(getApplication()
                    .getApplicationContext(), CameraHeadService.class);
            intent.putExtra("FLASH", camCapture.getFlash());
            intent.putExtra("Quality_Mode", camCapture.getQuality());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getApplication().getApplicationContext().startService(intent);
            Log.d("ImageTakin", "OnTakeBack()");
        }
    }

    private void sendFrontCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            Intent front_translucent = new Intent(getApplication()
                    .getApplicationContext(), CameraHeadService.class);
            front_translucent.putExtra("Front_Request", true);
            front_translucent.putExtra("Quality_Mode",
                    camCapture.getQuality());
            getApplication().getApplicationContext().startService(
                    front_translucent);
        }
    }

    public final Camera.Stub binder = new Camera.Stub() {
        @Override
        public void add(String flash, boolean isFront, int quality, long tag) throws RemoteException {
            cameraCaptureQueue.add(new CameraShield.CameraCapture(flash, isFront, quality, tag));
            if (!CameraHeadService.isRunning) {
                Intent intent1 = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
                LocalBroadcastManager.getInstance(CameraAidlService.this).sendBroadcast(intent1);
            }
            Log.d("receiver",
                    "All   " + cameraCaptureQueue.size());
        }

        @Override
        public void setTaken(long tag) throws RemoteException {
        }
    };

}
