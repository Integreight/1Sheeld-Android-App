package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.google.android.gms.vision.face.Face;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService.FaceDetectionObj;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Atef-PC on 2/21/2017.
 */

public class FaceDetectionShield extends ControllerParent<FaceDetectionShield> {
    private static final String TAG = FaceDetectionShield.class.getName();
    private static final String TAG1 = "Frame";
    public static final int FACE_CRASHED = 18;
    public static final int BIND_FACE_DETECTION = 9;
    public static final int START_DETECTION = 14;
    public static final int SEND_FACES = 20;
    public static final int SEND_MISSING = 21;
    public static final int UNBIND_FACE_DETECTION = 6;
    private static final byte DETECTED_FACES = 0x01;
    private static final byte MISSING_FACES = 0x02;
    private Messenger cameraBinder;
    private boolean isCameraBound = false;
    public boolean isBackPreview = true;
    private boolean isChangingPreview = false;
    private FaceDetectionHandler eventHandler;
    private byte[] faceId;
    private byte[] xPosition;
    private byte[] yPosition;
    private byte[] height;
    private byte[] width;
    private byte[] leftEye;
    private byte[] rightEye;
    private byte[] isSmile;
    private ShieldFrame frame;
    ArrayList<FaceDetectionObj> tmpArray = new ArrayList<>();

    public FaceDetectionShield() {
    }

    public FaceDetectionShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<FaceDetectionShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public ControllerParent<FaceDetectionShield> invalidate(SelectionAction selectionAction, boolean isToastable) {
        this.selectionAction = selectionAction;
        if (!checkCameraHardware(getApplication().getApplicationContext())) {
            if (selectionAction != null)
                selectionAction.onFailure();
            if (isToastable)
                activity.showToast(R.string.camera_camera_is_unavailable_maybe_its_used_by_another_application_toast);
        } else {
            addRequiredPremission(Manifest.permission.CAMERA);
            addRequiredPremission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT >= 16)
                addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkForPermissions()) {
                if (!activity.canDrawOverApps()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            activity);
                    builder.setMessage(
                            R.string.camera_we_need_you_to_enable_the_draw_over_apps_permission_in_order_to_show_the_camera_preview_correctly)
                            .setCancelable(false)
                            .setPositiveButton(R.string.camera_validation_dialog_ok_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog,
                                                            final int id) {
                                            activity.requestDrawOverApps();
                                        }
                                    })
                            .setNegativeButton(R.string.camera_validation_dialog_later_button, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog,
                                                    final int id) {
                                    dialog.cancel();
                                    activity.showToast(R.string.camera_please_enable_the_permission_to_be_able_to_select_this_shield_toast);
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                    if (this.selectionAction != null) {
                        this.selectionAction.onFailure();
                    }
                } else {
                    if (selectionAction != null)
                        selectionAction.onSuccess();
                    bindService();
                }
            } else {
                if (this.selectionAction != null) {
                    this.selectionAction.onFailure();
                }
            }
        }

        return super.invalidate(selectionAction, isToastable);
    }

    private Messenger mMessenger = new Messenger(new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == FACE_CRASHED) {
                cameraBinder = null;
                isCameraBound = false;
                bindService();
            } else if (msg.what == CameraHeadService.SET_CAMERA_PREVIEW_TYPE) {
                isBackPreview = msg.getData().getBoolean("isBack");
                if (eventHandler != null)
                    eventHandler.setOnCameraPreviewTypeChanged(isBackPreview);
                isChangingPreview = false;
            } else if (msg.what == SEND_FACES) {
                Bundle bundle = msg.getData();
                bundle.setClassLoader(FaceDetectionObj.class.getClassLoader());
                ArrayList<FaceDetectionObj> faceDetectionObjArrayList;
                faceDetectionObjArrayList = bundle.getParcelableArrayList("face_array");
                for (int i = 0; i < tmpArray.size(); i++) {
                    for (int j = 0; j < faceDetectionObjArrayList.size(); j++) {
                        if (tmpArray.get(i).getFaceId() == faceDetectionObjArrayList.get(j).getFaceId()) {
                            if ((tmpArray.get(i).getxPosition() != faceDetectionObjArrayList.get(j).getxPosition()) ||
                                    (tmpArray.get(i).getxPosition() != faceDetectionObjArrayList.get(j).getxPosition()) ||
                                    (tmpArray.get(i).getyPosition() != faceDetectionObjArrayList.get(j).getyPosition()) ||
                                    (tmpArray.get(i).getWidth() != faceDetectionObjArrayList.get(j).getWidth()) ||
                                    (tmpArray.get(i).getHeight() != faceDetectionObjArrayList.get(j).getHeight()) ||
                                    (tmpArray.get(i).getLeftEye() != faceDetectionObjArrayList.get(j).getLeftEye()) ||
                                    (tmpArray.get(i).getRightEye() != faceDetectionObjArrayList.get(j).getRightEye()) ||
                                    (tmpArray.get(i).getIsSmile() != faceDetectionObjArrayList.get(j).getIsSmile())) {
                                sendDetectedFaces(tmpArray.get(i));
                            }
                        } else {
                            sendMissingFaces(tmpArray.get(i));
                        }
                    }
                }
                tmpArray = faceDetectionObjArrayList;
            } else if (msg.what == SEND_MISSING) {
                Bundle bundle = msg.getData();
                bundle.setClassLoader(FaceDetectionObj.class.getClassLoader());
                ArrayList<FaceDetectionObj> missingFacesList;
                missingFacesList = bundle.getParcelableArrayList("missing_faces");
                for (int i = 0; i < tmpArray.size(); i++) {
                    for (int j = 0; j < missingFacesList.size(); j++) {
                        if (tmpArray.get(i).getFaceId() != missingFacesList.get(j).getFaceId()) {
                            sendMissingFaces(tmpArray.get(i));
                        } else if (tmpArray.get(i).getFaceId() == missingFacesList.get(j).getFaceId()) {
                            sendDetectedFaces(tmpArray.get(i));
                        }
                    }
                }
                tmpArray = missingFacesList;
            }
            super.handleMessage(msg);
        }
    });


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notifyHardwareOfShieldSelection();
            cameraBinder = new Messenger(service);
            Message msg = Message.obtain(null, BIND_FACE_DETECTION);
            msg.replyTo = mMessenger;
            try {
                cameraBinder.send(msg);
            } catch (RemoteException e) {
            }
            isCameraBound = true;
            try {
                setStartDetection();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isCameraBound = false;
        }
    };

    private void bindService() {
        getApplication().bindService(new Intent(getActivity(), CameraHeadService.class), serviceConnection, Context.BIND_AUTO_CREATE);

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

    public boolean isBackPreview() {
        return isBackPreview;
    }

    public void setStartDetection() throws RemoteException {
        if (isCameraBound) {
            Message msg = Message.obtain(null, START_DETECTION);
            msg.replyTo = mMessenger;
            if (cameraBinder != null) {
                cameraBinder.send(msg);
            }
        } else {
            bindService();
        }
    }

    public boolean setCameraToPreview(boolean isBack) {
        if (cameraBinder != null) {
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
        } else {
            bindService();
        }
        isBackPreview = isBack;
        return true;
    }

    public boolean showPreview() throws RemoteException {
        if (cameraBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.SHOW_PREVIEW);
            msg.replyTo = mMessenger;
            cameraBinder.send(msg);
            return true;
        } else {
            bindService();
            return false;
        }
    }

    public boolean hidePreview() throws RemoteException {
        if (cameraBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.HIDE_PREVIEW);
            msg.replyTo = mMessenger;
            cameraBinder.send(msg);
            return true;
        } else {
            return false;
        }
    }

    public void invalidatePreview() throws RemoteException {
        if (cameraBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.INVALIDATE_PREVIEW);
            msg.replyTo = mMessenger;
            cameraBinder.send(msg);
        } else
            bindService();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
    }

    public interface FaceDetectionHandler {
        void setOnCameraPreviewTypeChanged(boolean isBack);
    }

    @Override
    public void reset() {
        Message msg = Message.obtain(null, UNBIND_FACE_DETECTION);
        msg.replyTo = mMessenger;
        try {
            if (cameraBinder != null)
                cameraBinder.send(msg);
        } catch (RemoteException e) {
        }
        try {
            getApplication().unbindService(serviceConnection);
        } catch (IllegalArgumentException e) {
        }
        isCameraBound = false;
    }

    public void setCameraEventHandler(FaceDetectionHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void preConfigChange() {
        super.preConfigChange();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
    }

    private static byte[] intTo2ByteArray(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >>> 8) & 0xff);
        return bytes;
    }

    private static byte[] float2ByteArray(float value) {
        int bits = Float.floatToIntBits(value);
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (bits & 0xff);
        bytes[1] = (byte) ((bits >>> 8) & 0xff);
        return bytes;
    }

    private static byte[] twoByteArraysInto4ByteArray(byte[] value1, byte[] value2) {
        byte[] bytes = new byte[4];
        bytes[0] = value1[0];
        bytes[1] = value1[1];
        bytes[2] = value2[0];
        bytes[3] = value2[1];
        return bytes;
    }

    private static byte[] threeByteArraysTo4ByteArray(byte[] value, byte[] value1, byte[] value2) {
        byte[] bytes = new byte[4];
        bytes[0] = value[0];
        bytes[1] = value1[0];
        bytes[2] = value2[0];
        bytes[3] = 0;
        return bytes;
    }

    public void sendDetectedFaces(FaceDetectionObj faceDetection) {
        frame = new ShieldFrame(UIShield.FACE_DETECTION.getId(), DETECTED_FACES);
        faceId = intTo2ByteArray(faceDetection.getFaceId());
        xPosition = float2ByteArray(faceDetection.getxPosition());
        yPosition = float2ByteArray(faceDetection.getyPosition());
        height = float2ByteArray(faceDetection.getHeight());
        width = float2ByteArray(faceDetection.getWidth());
        leftEye = float2ByteArray(faceDetection.getLeftEye());
        rightEye = float2ByteArray(faceDetection.getRightEye());
        isSmile = float2ByteArray(faceDetection.getIsSmile());
        frame.addArgument(faceId);
        frame.addArgument(twoByteArraysInto4ByteArray(xPosition, yPosition));
        frame.addArgument(twoByteArraysInto4ByteArray(height, width));
        frame.addArgument(threeByteArraysTo4ByteArray(leftEye, rightEye, isSmile));
        sendShieldFrame(frame);
        android.util.Log.d(TAG1, "sendDetectedFaces: " + frame.toString());
    }

    public void sendMissingFaces(FaceDetectionObj faceDetectionObj) {
        frame = new ShieldFrame(UIShield.FACE_DETECTION.getId(), MISSING_FACES);
        faceId = intTo2ByteArray(faceDetectionObj.getFaceId());
        frame.addArgument(faceId);
        sendShieldFrame(frame);
        android.util.Log.d(TAG1, "sendMissingFaces: " + frame.toString());


    }

}
