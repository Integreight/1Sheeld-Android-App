package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

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
    private Messenger mService;
    boolean isCameraBound = false;
    public final static int UNBIND_CAMERA_CAPTURE = 4, BIND_CAMERA_CAPTURE = 5;

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
        Log.d("cameraS", "Bound");
        Intent camIntent = new Intent(this, CameraHeadService.class);
        camIntent.putExtra("isCamera", true);
        if (mConnection != null)
            unbindService(mConnection);
        mConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className,
                                           IBinder binder) {
//            CameraHeadService.MyBinder b = (CameraHeadService.MyBinder) binder;
                mService = new Messenger(binder);
                Message msg = Message.obtain(null, BIND_CAMERA_CAPTURE);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                }
                isCameraBound = true;
                LocalBroadcastManager.getInstance(CameraAidlService.this).sendBroadcast(new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME));
            }

            public void onServiceDisconnected(ComponentName className) {
                isCameraBound = false;
            }
        };
        bindService(camIntent, mConnection,
                Context.BIND_AUTO_CREATE);
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        capture = null;
        LocalBroadcastManager.getInstance(
                getApplication().getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME));
        Log.d("cameraS", "BoundDone");
        return mMesesenger.getBinder();
    }

    private void crashed() {
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
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("cameraS", "UnBound");
        Message msg = Message.obtain(null, UNBIND_CAMERA_CAPTURE);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
        }
        unbindService(mConnection);
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(
                mMessageReceiver);
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        capture = null;
//        Intent intent1 = new Intent(getApplication()
//                .getApplicationContext(), CameraHeadService.class);
//        getApplication().getApplicationContext().stopService(intent1);
//        android.os.Process.killProcess(Process.myPid());
        return super.onUnbind(intent);
    }

    CameraShield.CameraCapture capture;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d("cameraS", "Received Frame " + CameraHeadService.isRunning);
            if (intent != null && intent.getExtras() != null && intent.getBooleanExtra("crashed", false)) {
                crashed();
                return;
            }
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
            if (cameraCaptureQueue == null)
                cameraCaptureQueue = new ConcurrentLinkedQueue<>();
            if (!CameraHeadService.isRunning)
                if (!cameraCaptureQueue.isEmpty()) {
                    capture = cameraCaptureQueue.peek();
                    if (capture.isFront()) {
                        sendFrontCaptureImageIntent(cameraCaptureQueue.poll());
                    } else {
                        sendCaptureImageIntent(cameraCaptureQueue.poll());
                    }
                    CameraHeadService.isRunning = true;
                }
        }

    };

    private static ServiceConnection mConnection;

    private void sendCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        Log.d("cameraS", "Ask to capture " + CameraHeadService.isRunning);
        if (camCapture != null) {
            Log.d("cameraS", "Is Camera Bound " + CameraHeadService.isRunning);
            if (isCameraBound) {
                Log.d("cameraS", "Ask to capture from Service " + CameraHeadService.isRunning);
                Bundle intent = new Bundle();
                intent.putString("FLASH", camCapture.getFlash());
                intent.putInt("Quality_Mode", camCapture.getQuality());
                Message msg = Message.obtain(null, CameraHeadService.CAPTURE_IMAGE);
                msg.setData(intent);
                try {
                    mService.send(msg);
                    Log.d("cameraS", "Sent request " + CameraHeadService.isRunning);
                } catch (RemoteException e) {
                    Log.d("cameraS", "failed to send request " + CameraHeadService.isRunning);
                }
            } else bindService(new Intent(this, CameraHeadService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
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
            if (isCameraBound) {
                Bundle intent = new Bundle();
                intent.putInt("Quality_Mode", camCapture.getQuality());
                intent.putBoolean("Front_Request", true);
                Message msg = Message.obtain(null, CameraHeadService.CAPTURE_IMAGE);
                msg.setData(intent);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                }
//                    service.takeImage(front_translucent);
            } else bindService(new Intent(this, CameraHeadService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

}
