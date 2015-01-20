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

import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CameraAidlService extends Service {
    public Queue<CameraShield.CameraCapture> cameraCaptureQueue = new ConcurrentLinkedQueue<>();
    private Hashtable<Long, CameraShield.CameraCapture> tempCapturesTable = new Hashtable<>();

    @Override
    public IBinder onBind(Intent intent) {
        LocalBroadcastManager.getInstance(
                getApplication().getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event-name"));
        cameraCaptureQueue = new ConcurrentLinkedQueue<>();
        tempCapturesTable = new Hashtable<>();
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
            Log.d("receiver", "Got message: " + message);
            Log.d("receiver",
                    "cameraCaptureQueue size = " + cameraCaptureQueue.size());

            while (cameraCaptureQueue.peek() != null && cameraCaptureQueue.peek().isTaken()) {
                tempCapturesTable.remove(cameraCaptureQueue.peek().getTag());
                cameraCaptureQueue.poll();
            }

            if (!CameraHeadService.isRunning)
                if (!cameraCaptureQueue.isEmpty()) {
                    if (cameraCaptureQueue.peek().isFront())
                        sendFrontCaptureImageIntent(cameraCaptureQueue.poll());
                    else
                        sendCaptureImageIntent(cameraCaptureQueue.poll());
                }
            ((OneSheeldApplication) getApplication()).setCameraCapturesSize(cameraCaptureQueue.size());
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
                tempCapturesTable.put(camCapture.getTag(), camCapture);
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
                tempCapturesTable.put(camCapture.getTag(), camCapture);
                getApplication().getApplicationContext().startService(
                        front_translucent);
            }
        }
    }

    public final Camera.Stub binder = new Camera.Stub() {
        @Override
        public void add(String flash, boolean isFront, int quality, long tag) throws RemoteException {
            cameraCaptureQueue.add(new CameraShield.CameraCapture(flash, isFront, quality, tag));
            tempCapturesTable.put(tag, new CameraShield.CameraCapture(flash, isFront, quality, tag));
            ((OneSheeldApplication) getApplication()).setCameraCapturesSize(cameraCaptureQueue.size());
        }

        @Override
        public void setTaken(long tag) throws RemoteException {
            CameraShield.CameraCapture capture = tempCapturesTable.get(tag);
            if (capture != null) {
                capture.setTaken();
                tempCapturesTable.put(tag, new CameraShield.CameraCapture(capture.getFlash(), capture.isFront(), capture.getQuality(), tag));
            }
        }
    };

}
