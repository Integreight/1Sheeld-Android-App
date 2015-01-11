package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Camera;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraAidlService;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.fragments.CameraFragment.CameraFragmentHandler;
import com.integreight.onesheeld.utils.Log;

public class CameraShield extends ControllerParent<CameraShield> implements
        CameraFragmentHandler {
    private static final byte CAPTURE_METHOD_ID = (byte) 0x01;
    private static final byte FLASH_METHOD_ID = (byte) 0x02;
    private static final byte QUALITY_METHOD_ID = (byte) 0x04;
    private static String FLASH_MODE;
    private static int QUALITY_MODE = 0;
    private Camera aidlBinder;
    private boolean isAidlBound;

    private static final byte FRONT_CAPTURE = (byte) 0x03;
    int numberOfFrames = 0;
    Handler UIHandler;

    private void sendCaptureImageIntent(CameraCapture camCapture) {
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

    private void sendFrontCaptureImageIntent(CameraCapture camCapture) {
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

    public CameraShield() {

    }

    public CameraShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<CameraShield> setTag(String tag) {
        Intent intent = new Intent(getActivity(), CameraAidlService.class);
        getActivity().bindService(intent, myAidlConnection, Context.BIND_AUTO_CREATE);
        getApplication().setCameraCapturesSize(0);
        UIHandler = new Handler();
        return super.setTag(tag);
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

    private ServiceConnection myAidlConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidlBinder = Camera.Stub.asInterface(service);
            getApplication().setCameraCapturesSize(0);
            isAidlBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            aidlBinder = null;
            isAidlBound = false;
        }

    };

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

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {

        if (frame.getShieldId() == UIShield.CAMERA_SHIELD.getId()) {
//            Log.d("OnNewFrame", "cameraCaptureQueue size = "
//                    + cameraCaptureQueue.size());

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
                            QUALITY_MODE);
                    if (getApplication().getCameraCapturesSize() == 0) {
                        sendCaptureImageIntent(camCapture);
                    }
                    if (!isAidlBound)
                        getActivity().bindService(new Intent(getActivity(), CameraAidlService.class), myAidlConnection, Context.BIND_AUTO_CREATE);
                    try {
                        aidlBinder.add(FLASH_MODE, false,
                                QUALITY_MODE);
                    } catch (RemoteException e) {
                        if (!isAidlBound)
                            getActivity().bindService(new Intent(getActivity(), CameraAidlService.class), myAidlConnection, Context.BIND_AUTO_CREATE);
                        e.printStackTrace();
                    }
                    break;
                case FRONT_CAPTURE:
                    numberOfFrames++;
                    Log.d("Camera", "Frames number front = " + numberOfFrames);
                    CameraCapture frontCamCapture = new CameraCapture(FLASH_MODE,
                            true, QUALITY_MODE);
                    if (getApplication().getCameraCapturesSize() == 0) {
                        sendFrontCaptureImageIntent(frontCamCapture);
                    }
                    if (!isAidlBound)
                        getActivity().bindService(new Intent(getActivity(), CameraAidlService.class), myAidlConnection, Context.BIND_AUTO_CREATE);
                    try {
                        aidlBinder.add(FLASH_MODE, true,
                                QUALITY_MODE);
                    } catch (RemoteException e) {
                        if (!isAidlBound)
                            getActivity().bindService(new Intent(getActivity(), CameraAidlService.class), myAidlConnection, Context.BIND_AUTO_CREATE);
                        e.printStackTrace();
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
//        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(
//                mMessageReceiver);
        if (isAidlBound)
            getActivity().unbindService(myAidlConnection);
        getApplication().setCameraCapturesSize(0);

    }

    @Override
    public void onCameraFragmentIntilized() {

    }

    public static class CameraCapture implements Parcelable {
        private String flash;
        private boolean isTaken;
        private boolean isFrontCamera;
        private int mquality;

        public CameraCapture(String flash, boolean isFront, int quality) {
            this.flash = flash;
            isTaken = false;
            isFrontCamera = isFront;
            mquality = quality;
        }

        public int getQuality() {
            return mquality;

        }

        public String getFlash() {
            return flash;
        }

        public boolean isTaken() {
            return isTaken;
        }

        public void setTaken() {
            isTaken = true;
        }

        public boolean isFront() {
            return isFrontCamera;

        }

        protected CameraCapture(Parcel in) {
            flash = in.readString();
            isTaken = in.readByte() != 0x00;
            isFrontCamera = in.readByte() != 0x00;
            mquality = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(flash);
            dest.writeByte((byte) (isTaken ? 0x01 : 0x00));
            dest.writeByte((byte) (isFrontCamera ? 0x01 : 0x00));
            dest.writeInt(mquality);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<CameraCapture> CREATOR = new Parcelable.Creator<CameraCapture>() {
            @Override
            public CameraCapture createFromParcel(Parcel in) {
                return new CameraCapture(in);
            }

            @Override
            public CameraCapture[] newArray(int size) {
                return new CameraCapture[size];
            }
        };
    }

}
