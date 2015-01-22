package com.integreight.onesheeld.shields.controller.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.integreight.onesheeld.Camera;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.utils.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CameraAidlService extends Service {
    public Queue<CameraShield.CameraCapture> cameraCaptureQueue = new ConcurrentLinkedQueue<>();

    @Override
    public IBinder onBind(Intent intent) {
        LocalBroadcastManager.getInstance(
                getApplication().getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME));
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(
                mMessageReceiver);
        ((OneSheeldApplication) getApplication()).setCameraCapturesSize(0);
        return super.onUnbind(intent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
//            Log.d("receiver", "Got message: " + message);
            Log.d("receiver",
                    "Before" + cameraCaptureQueue.size());
            if (!CameraHeadService.isRunning)
                if (!cameraCaptureQueue.isEmpty()) {
                    final CameraShield.CameraCapture capture = cameraCaptureQueue.poll();
                    if (capture.isFront()) {
                        sendFrontCaptureImageIntent(capture);
                    } else {
                        sendCaptureImageIntent(capture);
                    }
                }
            ((OneSheeldApplication) getApplication()).setCameraCapturesSize(cameraCaptureQueue.size());
            Log.d("receiver",
                    "After" + cameraCaptureQueue.size());
        }

    };

    private void sendCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            if (!camCapture.isTaken()) {
                Intent intent = new Intent(getApplication()
                        .getApplicationContext(), CameraHeadService.class);
                intent.putExtra("FLASH", camCapture.getFlash());
                intent.putExtra("Quality_Mode", camCapture.getQuality());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                camCapture.setTaken();
                getApplication().getApplicationContext().startService(intent);
                Log.d("ImageTakin", "OnTakeBack()");
            }
        }
    }

    private void sendFrontCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            if (!camCapture.isTaken()) {
                Intent front_translucent = new Intent(getApplication()
                        .getApplicationContext(), CameraHeadService.class);
                front_translucent.putExtra("Front_Request", true);
                front_translucent.putExtra("Quality_Mode",
                        camCapture.getQuality());
                camCapture.setTaken();
                getApplication().getApplicationContext().startService(
                        front_translucent);
            }
        }
    }

    public final Camera.Stub binder = new Camera.Stub() {
        @Override
        public void add(String flash, boolean isFront, int quality, long tag) throws RemoteException {
            cameraCaptureQueue.add(new CameraShield.CameraCapture(flash, isFront, quality, tag));
            ((OneSheeldApplication) getApplication()).setCameraCapturesSize(cameraCaptureQueue.size());
            if (!CameraHeadService.isRunning) {
                Intent intent1 = new Intent(CameraUtils.CAMERA_CAPTURE_RECEIVER_EVENT_NAME);
                // You can also include some extra data.
                intent1.putExtra("message", "This is my message!");
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
