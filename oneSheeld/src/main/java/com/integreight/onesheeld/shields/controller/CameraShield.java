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

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraAidlService;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.fragments.CameraFragment.CameraFragmentHandler;
import com.integreight.onesheeld.utils.Log;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CameraShield extends ControllerParent<CameraShield> implements
        CameraFragmentHandler {
    private static final byte CAPTURE_METHOD_ID = (byte) 0x01;
    private static final byte FLASH_METHOD_ID = (byte) 0x02;
    private static final byte QUALITY_METHOD_ID = (byte) 0x04;
    private static String FLASH_MODE;
    private static int QUALITY_MODE = 0;
    private Messenger aidlBinder;
    private static boolean isAidlBound;
    private Queue<CameraCapture> capturesQueue = new ConcurrentLinkedQueue<>();
    public static int SHOW_PREVIEW = 7;
    public static int HIDE_PREVIEW = 8;

    private static final byte FRONT_CAPTURE = (byte) 0x03;
    int numberOfFrames = 0;
    Handler UIHandler;

    public CameraShield() {

    }

    public CameraShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<CameraShield> init(String tag) {
        Intent intent = new Intent(getActivity(), CameraAidlService.class);
        if (!getApplication().getRunningShields().containsKey(UIShield.COLOR_DETECTION_SHIELD.name()))
            getActivity().stopService(new Intent(getActivity(), CameraHeadService.class));
        getActivity().stopService(new Intent(getActivity(), CameraAidlService.class));
        if (myAidlConnection != null && isAidlBound)
            getActivity().unbindService(myAidlConnection);
        myAidlConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                aidlBinder = new Messenger(service);
                Message msg = Message.obtain(null, CameraAidlService.SET_REPLYTO);
                msg.replyTo = mMessenger;
                if (capturesQueue != null && !capturesQueue.isEmpty()) {
                    Bundle b = new Bundle();
                    CameraShield.CameraCapture[] arr = new CameraShield.CameraCapture[]{};
                    arr = capturesQueue.toArray(arr);
                    b.putSerializable("queue", arr);
                    msg.setData(b);
                }
                try {
                    aidlBinder.send(msg);
                    capturesQueue = new ConcurrentLinkedQueue<>();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                isAidlBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                aidlBinder = null;
                isAidlBound = false;
            }

        };
        getActivity().bindService(intent, myAidlConnection, Context.BIND_AUTO_CREATE);
        UIHandler = new Handler();
        return super.init(tag);
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

        }
        return super.invalidate(selectionAction, isToastable);
    }

    private Messenger mMessenger = new Messenger(new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == CameraAidlService.CRASHED) {
                aidlBinder = null;
                isAidlBound = false;
                if (capturesQueue == null)
                    capturesQueue = new ConcurrentLinkedQueue<>();
                if (msg.getData() != null && msg.getData().getSerializable("queue") != null) {
                    CameraCapture[] captures = Arrays.copyOf(((Object[]) msg.getData().getSerializable("queue")), ((Object[]) msg.getData().getSerializable("queue")).length, CameraCapture[].class);
                    for (CameraCapture capture : captures) {
                        capturesQueue.add(capture);
                    }
                }
                Intent intent = new Intent(getActivity(), CameraAidlService.class);
                getActivity().bindService(intent, myAidlConnection, Context.BIND_AUTO_CREATE);
            } else {
                super.handleMessage(msg);
            }
        }

        ;
    });
    private static ServiceConnection myAidlConnection;

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
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
        // this.eventHandler = eventHandler;

    }

    public void showPreview() {
        Message msg = Message.obtain(null, SHOW_PREVIEW);
        msg.replyTo = mMessenger;
        try {
            aidlBinder.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void hidePreview() {
        Message msg = Message.obtain(null, HIDE_PREVIEW);
        msg.replyTo = mMessenger;
        try {
            if(aidlBinder!=null)
            aidlBinder.send(msg);
        } catch (RemoteException e) {
        }
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
                    try {
                        if (aidlBinder == null || !isAidlBound) {
                            if (capturesQueue == null)
                                capturesQueue = new ConcurrentLinkedQueue<>();
                            capturesQueue.add(camCapture);
                        } else {
                            Message msgBack = Message.obtain(null, CameraAidlService.ADD_TO_QUEUE);
                            Bundle b = new Bundle();
                            b.putSerializable("capture", camCapture);
                            msgBack.setData(b);
                            if (capturesQueue != null && !capturesQueue.isEmpty()) {
                                CameraShield.CameraCapture[] arr = new CameraShield.CameraCapture[]{};
                                arr = capturesQueue.toArray(arr);
                                b.putSerializable("queue", arr);
                            }
                            msgBack.replyTo = mMessenger;
                            aidlBinder.send(msgBack);
                        }
                    } catch (RemoteException e) {
                        if (capturesQueue != null)
                            capturesQueue.add(camCapture);
                    }
                    break;
                case FRONT_CAPTURE:
                    numberOfFrames++;
                    Log.d("Camera", "Frames number front = " + numberOfFrames);
                    CameraCapture frontCamCapture = new CameraCapture(FLASH_MODE,
                            true, QUALITY_MODE, new Date().getTime());
                    try {

                        if (aidlBinder == null || !isAidlBound) {
                            if (capturesQueue == null)
                                capturesQueue = new ConcurrentLinkedQueue<>();
                            capturesQueue.add(frontCamCapture);
                        } else {
                            Message msgFront = Message.obtain(null, CameraAidlService.ADD_TO_QUEUE);
                            Bundle b = new Bundle();
                            b.putSerializable("capture", frontCamCapture);
                            msgFront.setData(b);
                            if (capturesQueue != null && !capturesQueue.isEmpty()) {
                                CameraShield.CameraCapture[] arr = new CameraShield.CameraCapture[]{};
                                arr = capturesQueue.toArray(arr);
                                b.putSerializable("queue", arr);
                            }
                            msgFront.replyTo = mMessenger;
                            aidlBinder.send(msgFront);
                            capturesQueue = new ConcurrentLinkedQueue<>();
                        }
                    } catch (RemoteException e) {
                        if (capturesQueue != null)
                            capturesQueue.add(frontCamCapture);
                    }
                    break;

                default:
                    break;
            }
        }

    }

    public static interface CameraEventHandler {
        void OnPictureTaken();

        void checkCameraHardware(boolean isHasCamera);

        void takePicture();

        void setFlashMode(String flash_mode);
    }

    @Override
    public void reset() {
        if (isAidlBound)
            getActivity().unbindService(myAidlConnection);
        capturesQueue = new ConcurrentLinkedQueue<>();
        isAidlBound = false;

    }

    @Override
    public void onCameraFragmentIntilized() {

    }

    public static class CameraCapture implements Serializable {
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
