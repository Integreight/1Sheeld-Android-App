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
    public static final int FACE_CRASHED = 18;
    public static final int BIND_FACE_DETECTION = 9;
    public static final int START_DETECTION = 14;
    public static final int SEND_FACES = 20;
    public static final int SEND_EMPTY = 21;
    public static final int IS_FACE_ACTIVE = 25;
    public static final int UNBIND_FACE_DETECTION = 6;
    private static final byte DETECTED_FACES = 0x01;
    private static final byte MISSING_FACES = 0x02;
    private Messenger faceBinder;
    private boolean isCameraBound = false;
    public boolean isBackPreview = true;
    public boolean isFaceSelected = false;
    private boolean isChangingPreview = false;
    private FaceDetectionHandler eventHandler;
    private byte[] faceId = new byte[2];
    private byte[] xPosition = new byte[2];
    private byte[] yPosition = new byte[2];
    private byte[] height = new byte[2];
    private byte[] width = new byte[2];
    private byte[] leftEye = new byte[2];
    private byte[] rightEye = new byte[2];
    private byte[] isSmile = new byte[2];
    private ShieldFrame frame;
    ArrayList<FaceDetectionObj> tmpArray = null;
    private int previewWidth, previewHeight;
    private int rotation;

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
                faceBinder = null;
                isCameraBound = false;
                bindService();
            } else if (msg.what == CameraHeadService.SET_CAMERA_PREVIEW_TYPE) {
                isBackPreview = msg.getData().getBoolean("isBack");
                if (eventHandler != null)
                    eventHandler.setOnCameraPreviewTypeChanged(isBackPreview);
                isChangingPreview = false;

            } else if (msg.what == SEND_FACES) {
                Bundle bundle = msg.getData();
                if (tmpArray == null)
                    tmpArray = new ArrayList<>();
                bundle.setClassLoader(FaceDetectionObj.class.getClassLoader());
                ArrayList<FaceDetectionObj> faceDetectionObjArrayList;
                faceDetectionObjArrayList = bundle.getParcelableArrayList("face_array");
                previewHeight = bundle.getInt("height");
                previewWidth = bundle.getInt("width");
                rotation = bundle.getInt("rotation");
                for (int i = 0; i < faceDetectionObjArrayList.size(); i++) {
                    boolean isMatched = false;
                    for (int j = 0; j < tmpArray.size(); j++)
                        if (faceDetectionObjArrayList.get(i).getFaceId() == tmpArray.get(j).getFaceId()) {
                            isMatched = true;
                            if (tmpArray.get(j).getxPosition() != faceDetectionObjArrayList.get(i).getxPosition() ||
                                    tmpArray.get(j).getyPosition() != faceDetectionObjArrayList.get(i).getyPosition() ||
                                    tmpArray.get(j).getWidth() != faceDetectionObjArrayList.get(i).getWidth() ||
                                    tmpArray.get(j).getHeight() != faceDetectionObjArrayList.get(i).getHeight() ||
                                    tmpArray.get(j).getLeftEye() != faceDetectionObjArrayList.get(i).getLeftEye() ||
                                    tmpArray.get(j).getRightEye() != faceDetectionObjArrayList.get(i).getRightEye() ||
                                    tmpArray.get(j).getIsSmile() != faceDetectionObjArrayList.get(i).getIsSmile()) {
                                sendDetectedFaces(tmpArray.get(j), previewWidth, previewHeight);
                                tmpArray.remove(j);
                            }
                            break;
                        }
                    if (!isMatched)
                        sendDetectedFaces(faceDetectionObjArrayList.get(i), previewWidth, previewHeight);
                }
                if (tmpArray.size() > 0)
                    for (int k = 0; k < tmpArray.size(); k++)
                        sendMissingFaces(tmpArray.get(k));
                tmpArray = faceDetectionObjArrayList;
            }
            if (msg.what == SEND_EMPTY) {
                if (tmpArray != null)
                    for (int i = 0; i < tmpArray.size(); i++)
                        sendMissingFaces(tmpArray.get(i));
                tmpArray = null;
            }
            super.handleMessage(msg);
        }
    });

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notifyHardwareOfShieldSelection();
            faceBinder = new Messenger(service);
            Message msg = Message.obtain(null, BIND_FACE_DETECTION);
            msg.replyTo = mMessenger;
            try {
                faceBinder.send(msg);
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

    public boolean isFaceSelected() {
        return isFaceSelected;
    }

    public void setStartDetection() throws RemoteException {
        if (isCameraBound) {
            Message msg = Message.obtain(null, START_DETECTION);
            msg.replyTo = mMessenger;
            if (faceBinder != null) {
                faceBinder.send(msg);
            }
        } else {
            bindService();
        }
    }

    public void setIsFaceSelected(boolean isFaceSelected) {
        if (faceBinder != null) {
            Message msg = Message.obtain(null, IS_FACE_ACTIVE);
            msg.replyTo = mMessenger;
            Bundle b = new Bundle();
            b.putBoolean("setIsFaceSelected", isFaceSelected);
            msg.setData(b);
            try {
                faceBinder.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
        this.isFaceSelected = isFaceSelected;
    }

    public boolean setCameraToPreview(boolean isBack) {
        if (faceBinder != null) {
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
                faceBinder.send(msg);
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
        if (faceBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.SHOW_PREVIEW);
            msg.replyTo = mMessenger;
            faceBinder.send(msg);
            return true;
        } else {
            bindService();
            return false;
        }
    }

    public boolean hidePreview() throws RemoteException {
        if (faceBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.HIDE_PREVIEW);
            msg.replyTo = mMessenger;
            faceBinder.send(msg);
            return true;
        } else {
            return false;
        }
    }

    public void invalidatePreview() throws RemoteException {
        if (faceBinder != null) {
            Message msg = Message.obtain(null, CameraHeadService.INVALIDATE_PREVIEW);
            msg.replyTo = mMessenger;
            faceBinder.send(msg);
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
        android.util.Log.d(TAG, "unbind: ");
        Message msg = Message.obtain(null, UNBIND_FACE_DETECTION);
        msg.replyTo = mMessenger;
        try {
            if (faceBinder != null)
                faceBinder.send(msg);
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
        int bits = Math.round(value);
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

    private static float transform(float value, float inMin, float inMax, float outMin, float outMax) {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public void sendDetectedFaces(FaceDetectionObj faceDetection, int previewWidth, int previewHeight) {
        float x = faceDetection.getxPosition();
        float y = faceDetection.getyPosition();
        float scaledX, scaledY, scaleSmile, scaleRightEye, scaleLeftEye;
        switch (rotation) {
            case 0: {
                if (!isBackPreview)
                    x = previewHeight - x;
                break;
            }
            case 1: {
                if (!isBackPreview)
                    x = previewWidth - x;
                break;
            }
            case 2: {
                if (!isBackPreview)
                    x = previewHeight - x;
                break;
            }
            case 3: {
                if (!isBackPreview)
                    x = previewWidth - x;
                break;
            }
        }
        frame = new ShieldFrame(UIShield.FACE_DETECTION.getId(), DETECTED_FACES);
        faceId = intTo2ByteArray(faceDetection.getFaceId());
        scaledX = transform(x, 0, previewWidth, -500, 500);
        scaledY = transform(y, 0, previewHeight, 500, -500);
        if (faceDetection.getIsSmile() == -1)
            isSmile = float2ByteArray(faceDetection.getIsSmile());
        else if (faceDetection.getRightEye() == -1)
            rightEye = float2ByteArray(faceDetection.getRightEye());
        else if (faceDetection.getLeftEye() == -1)
            leftEye = float2ByteArray(faceDetection.getLeftEye());
        else {
            scaleSmile = transform(faceDetection.getIsSmile(), 0, 1, 0, 100);
            scaleRightEye = transform(faceDetection.getRightEye(), 0, 1, 0, 100);
            scaleLeftEye = transform(faceDetection.getLeftEye(), 0, 1, 0, 100);
            leftEye = float2ByteArray(scaleLeftEye);
            rightEye = float2ByteArray(scaleRightEye);
            isSmile = float2ByteArray(scaleSmile);
        }
        xPosition = float2ByteArray(scaledX);
        yPosition = float2ByteArray(scaledY);
        height = float2ByteArray(faceDetection.getHeight());
        width = float2ByteArray(faceDetection.getWidth());
        frame.addArgument(faceId);
        frame.addArgument(twoByteArraysInto4ByteArray(xPosition, yPosition));
        if (rotation == 1 || rotation == 3)
            frame.addArgument(twoByteArraysInto4ByteArray(height, width));
        else
            frame.addArgument(twoByteArraysInto4ByteArray(width, height));
        frame.addArgument(threeByteArraysTo4ByteArray(leftEye, rightEye, isSmile));
        sendShieldFrame(frame);
    }

    public void sendMissingFaces(FaceDetectionObj faceDetectionObj) {
        frame = new ShieldFrame(UIShield.FACE_DETECTION.getId(), MISSING_FACES);
        faceId = intTo2ByteArray(faceDetectionObj.getFaceId());
        frame.addArgument(faceId);
        sendShieldFrame(frame);
    }


}
