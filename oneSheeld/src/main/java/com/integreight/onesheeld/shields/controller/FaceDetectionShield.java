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
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.SparseArray;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraHeadService;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.utils.FaceGraphic;
import com.integreight.onesheeld.utils.customviews.utils.GraphicOverlay;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Atef-PC on 2/21/2017.
 */

public class FaceDetectionShield extends ControllerParent<FaceDetectionShield> {
    private static final String TAG = FaceDetectionShield.class.getName();
    public static final int FACE_CRASHED = 18;
    public static final int BIND_FACE_DETECTION = 9;
    public static final int START_DETECTION = 14;
    public static final int SEND_FACES = 20;
    public static final int UNBIND_FACE_DETECTION = 6;
    private static final byte SHIELD_ID = 0x2D;
    private static final byte DETECT_FACES = 0x01;
    private static final byte STOP_DETECTION = 0x02;
    private Messenger cameraBinder;
    private boolean isCameraBound = false;
    public boolean isBackPreview = true;
    private boolean isChangingPreview = false;
    private FaceDetectionHandler eventHandler;
    private static GraphicOverlay mGraphicOverlay;


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

    private static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    public static class MyFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new MyFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private static class MyFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private byte[] faceId;
        private byte[] xPosition;
        private byte[] yPosition;
        private byte[] height;
        private byte[] width;
        private byte[] leftEye;
        private byte[] rightEye;
        private byte[] isSmile;
        private ShieldFrame frame;
        private SparseArray<Face> faceArray = new SparseArray<>();

        MyFaceTracker(GraphicOverlay overlay) {
//            mOverlay = overlay;
//            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
//            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            android.util.Log.d(TAG, "onUpdate: " + detectionResults.getDetectedItems().size());
            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                faceArray.append(i, detectionResults.getDetectedItems().get(i));
                android.util.Log.d(TAG, "onUpdate: ID " + faceArray.get(i).getId());
            }

//            mOverlay.add(mFaceGraphic);
//            mFaceGraphic.updateFace(face);
//            if (detectionResults.getDetectedItems().size() > 0)
//                sendDetectedFaces(detectionResults.getDetectedItems());

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            //            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
//            mOverlay.remove(mFaceGraphic);
        }

        public void sendDetectedFaces(SparseArray<Face> mFacesArray) {
            frame = new ShieldFrame(UIShield.FACE_DETECTION.getId());
            Face faceDetection;
            if (mFacesArray.size() > 0)
                for (int i = 0; i < mFacesArray.size(); i++) {
                    faceDetection = mFacesArray.get(i);
                    faceId = intToByteArray(faceDetection.getId());
                    xPosition = float2ByteArray(faceDetection.getPosition().x);
                    yPosition = float2ByteArray(faceDetection.getPosition().y);
                    height = float2ByteArray(faceDetection.getHeight());
                    width = float2ByteArray(faceDetection.getWidth());
                    leftEye = float2ByteArray(faceDetection.getIsLeftEyeOpenProbability());
                    rightEye = float2ByteArray(faceDetection.getIsRightEyeOpenProbability());
                    isSmile = float2ByteArray(faceDetection.getIsSmilingProbability());

                }
        }
    }

}
