package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.utils.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CameraShield extends ControllerParent<CameraShield>  {
    private static final byte CAPTURE_METHOD_ID = (byte) 0x01;
    private static final byte FLASH_METHOD_ID = (byte) 0x02;
    private static final byte QUALITY_METHOD_ID = (byte) 0x04;
    private static String FLASH_MODE;
    private static int QUALITY_MODE = 0;
    private Messenger cameraBinder;
    private boolean isCameraBound;
    public Queue<CameraShield.CameraCapture> capturesQueue = new ConcurrentLinkedQueue<>();
    public static final int CRASHED = 3;
    public final static int UNBIND_CAMERA_CAPTURE = 4, BIND_CAMERA_CAPTURE = 5, NEXT_CAPTURE = 14;
    CameraCapture capture;
    private boolean isCameraCapturing = false;

    private static final byte FRONT_CAPTURE = (byte) 0x03;
    int numberOfFrames = 0;
    Handler UIHandler;
    public boolean isBackPreview = true;
    private CameraEventHandler eventHandler;
    private boolean hasFrontCamera = false;

    public CameraShield() {

    }

    public CameraShield(Activity activity, String tag) {
        super(activity, tag, true);
    }

    @Override
    public ControllerParent<CameraShield> init(String tag) {
        UIHandler = new Handler();
        return super.init(tag, true);
    }

    private Messenger mMessenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == CRASHED) {
                cameraBinder = null;
                isCameraBound = false;
                if (capturesQueue == null)
                    capturesQueue = new ConcurrentLinkedQueue<>();
                bindService();
            } else if (msg.what == NEXT_CAPTURE) {
                if (msg.getData().getBoolean("takenSuccessfuly")) {
                    if (capturesQueue != null && !capturesQueue.isEmpty())
                        capturesQueue.poll();
                }
                isCameraCapturing = false;
                checkQueue();
            } else if (msg.what == CameraHeadService.SET_CAMERA_PREVIEW_TYPE) {
                isBackPreview = msg.getData().getBoolean("isBack");
                if (eventHandler != null)
                    eventHandler.setOnCameraPreviewTypeChanged(isBackPreview);
                isChangingPreview = false;
            }
            super.handleMessage(msg);
        }


    });

    void bindService() {
        getApplication().bindService(new Intent(getActivity(), CameraHeadService.class), cameraServiceConnector, Context.BIND_AUTO_CREATE);
    }

    @Override
    public ControllerParent<CameraShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        if (!checkCameraHardware(getApplication().getApplicationContext())) {
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast("Camera is unavailable, maybe it's used by another application !");
        } else {
            if (selectionAction != null)
                selectionAction.onSuccess();
            hasFrontCamera = CameraUtils.checkFrontCamera(activity.getApplicationContext());
            bindService();
            UIHandler = new Handler();
        }
        return super.invalidate(selectionAction, isToastable);
    }

    public boolean isBackPreview() {
        return isBackPreview;
    }

    private boolean isChangingPreview = false;

    public boolean setCameraToPreview(boolean isBack) {
        if (!isBack && !CameraUtils.checkFrontCamera(getActivity().getApplicationContext()))
            return false;
        if (isChangingPreview)
            return false;
        isChangingPreview = true;
        Log.d("Acc", isBack + "   **");
        Message msg = Message.obtain(null, CameraHeadService.SET_CAMERA_PREVIEW_TYPE);
        msg.replyTo = mMessenger;
        Bundle b = new Bundle();
        b.putBoolean("isBack", isBack);
        msg.setData(b);
        try {
            cameraBinder.send(msg);
        } catch (RemoteException e) {
            return false;
        }
        isBackPreview = isBack;
        return true;
    }

    private ServiceConnection cameraServiceConnector = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isCameraCapturing = false;
            notifyHardwareOfShieldSelection();
            cameraBinder = new Messenger(service);
            Message msg = Message.obtain(null, BIND_CAMERA_CAPTURE);
            msg.replyTo = mMessenger;
            try {
                cameraBinder.send(msg);
            } catch (RemoteException e) {
            }
            isCameraBound = true;
            checkQueue();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cameraBinder = null;
            isCameraBound = false;
        }

    };

    private synchronized void checkQueue() {
        if (capturesQueue != null && !capturesQueue.isEmpty() && !isCameraCapturing) {
            if (isCameraBound) {
                capture = capturesQueue.peek();
                if (capture.isFront()) {
                    if (hasFrontCamera)
                        sendFrontCaptureImageIntent(capture);
                    else {
                        Toast.makeText(getActivity(), "Your device doesn't have a front camera", Toast.LENGTH_SHORT).show();
                        capturesQueue.poll();
                    }
                } else {
                    sendCaptureImageIntent(capture);
                }
            } else bindService();
        }
    }

    /**
     * Check if this device has a camera
     */
    private synchronized boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void setCameraEventHandler(CameraEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }


    public void showPreview() throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.SHOW_PREVIEW);
        msg.replyTo = mMessenger;
        if (cameraBinder != null)
            cameraBinder.send(msg);
        else bindService();
    }

    public void invalidatePreview() throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.INVALIDATE_PREVIEW);
        msg.replyTo = mMessenger;
        if (cameraBinder != null)
            cameraBinder.send(msg);
        else bindService();
    }

    public void hidePreview() throws RemoteException {
        Message msg = Message.obtain(null, CameraHeadService.HIDE_PREVIEW);
        msg.replyTo = mMessenger;
        if (cameraBinder != null)
            cameraBinder.send(msg);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {

        if (frame.getShieldId() == UIShield.CAMERA_SHIELD.getId()) {
            switch (frame.getFunctionId()) {
                case QUALITY_METHOD_ID:
                    byte quality_mode = frame.getArgument(0)[0];
                    switch (quality_mode) {
                        case 1:
                            QUALITY_MODE = 40;
                            break;
                        case 2:
                            QUALITY_MODE = 70;
                            break;
                        case 3:
                            QUALITY_MODE = 100;
                            break;

                        default:
                            break;
                    }
                    break;

                case FLASH_METHOD_ID:
                    byte flash_mode = frame.getArgument(0)[0];
                    switch (flash_mode) {
                        case 0:
                            FLASH_MODE = "off";
                            break;
                        case 1:
                            FLASH_MODE = "on";
                            break;
                        case 2:
                            FLASH_MODE = "auto";
                            break;
                        default:
                            break;
                    }
                    break;

                case CAPTURE_METHOD_ID:

                    numberOfFrames++;
                    Log.d("Camera", "Frames number = " + numberOfFrames);
                    CameraCapture camCapture = new CameraCapture(FLASH_MODE, false,
                            QUALITY_MODE, new Date().getTime());
                    if (capturesQueue == null)
                        capturesQueue = new ConcurrentLinkedQueue<>();
                    capturesQueue.add(camCapture);
                    checkQueue();
                    break;
                case FRONT_CAPTURE:
                    numberOfFrames++;
                    Log.d("Camera", "Frames number front = " + numberOfFrames);
                    CameraCapture frontCamCapture = new CameraCapture(FLASH_MODE,
                            true, QUALITY_MODE, new Date().getTime());
                    if (capturesQueue == null)
                        capturesQueue = new ConcurrentLinkedQueue<>();
                    capturesQueue.add(frontCamCapture);
                    checkQueue();
                    break;

                default:
                    break;
            }
        }

    }

    public interface CameraEventHandler {
        void OnPictureTaken();

        void checkCameraHardware(boolean isHasCamera);

        void takePicture();

        void setFlashMode(String flash_mode);

        void setOnCameraPreviewTypeChanged(boolean isBack);
    }

    @Override
    public void reset() {
        Message msg = Message.obtain(null, UNBIND_CAMERA_CAPTURE);
        try {
            if (cameraBinder != null)
                cameraBinder.send(msg);
        } catch (RemoteException e) {
        }
        getApplication().unbindService(cameraServiceConnector);
        capturesQueue = new ConcurrentLinkedQueue<>();
        isCameraBound = false;

    }

    @Override
    public void preConfigChange() {
//        Message msg = Message.obtain(null, UNBIND_CAMERA_CAPTURE);
//        try {
//            if (cameraBinder != null)
//                cameraBinder.send(msg);
//        } catch (RemoteException e) {
//        }
//        getActivity().unbindService(cameraServiceConnector);
        super.preConfigChange();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
//        bindService();
    }

    private void sendCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            if (isCameraBound) {
                isCameraCapturing = true;
                Bundle intent = new Bundle();
                intent.putString("FLASH", camCapture.getFlash());
                intent.putInt("Quality_Mode", camCapture.getQuality());
                Message msg = Message.obtain(null, CameraHeadService.CAPTURE_IMAGE);
                msg.setData(intent);
                try {
                    cameraBinder.send(msg);
                } catch (RemoteException e) {
//                    capturesQueue.add(camCapture);
                    checkQueue();
                }
            } else bindService();
            Log.d("ImageTakin", "OnTakeBack()");
        }
    }

    private void sendFrontCaptureImageIntent(CameraShield.CameraCapture camCapture) {
        if (camCapture != null) {
            if (isCameraBound) {
                isCameraCapturing = true;
                Bundle intent = new Bundle();
                intent.putInt("Quality_Mode", camCapture.getQuality());
                intent.putBoolean("Front_Request", true);
                Message msg = Message.obtain(null, CameraHeadService.CAPTURE_IMAGE);
                msg.setData(intent);
                try {
                    cameraBinder.send(msg);
                } catch (RemoteException e) {
                    checkQueue();
                }
//                    service.takeImage(front_translucent);
            } else {
                bindService();
            }
        }
    }

    public class CameraCapture implements Serializable {
        private String flash;
        private boolean isFrontCamera;

        private long tag;
        private int mquality;

        public CameraCapture(String flash, boolean isFront, int quality, long tag) {
            this.flash = flash;
            isFrontCamera = isFront;
            mquality = quality;
            this.tag = tag;
        }

        public int getQuality() {
            return mquality;

        }

        public String getFlash() {
            return flash;
        }

        public boolean isFront() {
            return isFrontCamera;

        }

        public long getTag() {
            return tag;
        }

        public void setTag(long tag) {
            this.tag = tag;
        }
    }

}
