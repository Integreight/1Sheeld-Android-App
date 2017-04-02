package com.integreight.onesheeld.shields.fragments;


import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.ShieldsOperations;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield.FaceDetectionHandler;

/**
 * Created by Atef-PC on 2/21/2017.
 */

public class FaceDetectionFragment extends ShieldFragmentParent<FaceDetectionFragment> implements ShieldsOperations.OnChangeListener, MainActivity.OnSlidingMenueChangeListner {

    private CheckBox frontBackToggle;
    private CheckBox cameraPreviewToggle;
    private ImageView lastImage;
    private View cameraLogo;
    private FaceDetectionHandler faceDetectionHandler = new FaceDetectionHandler() {
        @Override
        public void setOnCameraPreviewTypeChanged(final boolean isBack) {
            if (canChangeUI() && frontBackToggle != null && getView() != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeListeners();
                        frontBackToggle.setChecked(isBack);
                        applyListeners();
                    }
                });
        }
    };

    private void removeListeners() {
        frontBackToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
    }

    private void applyListeners() {
        frontBackToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean feeback = ((FaceDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCameraToPreview(b);
                if (!feeback) {
                    removeListeners();
                    frontBackToggle.setChecked(!b);
                    applyListeners();
                }
            }
        });

        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked())
                        ((CheckBox) activity.findViewById(R.id.isMenuOpening)).setChecked(true);
                    else {
                        if (getApplication().getRunningShields().get(getControllerTag()) != null)
                            try {
                                if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).showPreview()) {
                                    cameraLogo.setVisibility(View.INVISIBLE);
                                    ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(true);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                removeListeners();
                                cameraPreviewToggle.setChecked(false);
                                applyListeners();
                            }
                    }
                } else {
                    if (getApplication().getRunningShields().get(getControllerTag()) != null)
                        try {
                            if ((((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview())) {
                                cameraLogo.setVisibility(View.VISIBLE);
                                ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(false);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListeners();
                            cameraPreviewToggle.setChecked(true);
                            applyListeners();
                        }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName()) != null)
            ((ShieldsOperations) getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName())).addOnSlidingLocksListener(this);
        activity.registerSlidingMenuListner(this);
        return inflater.inflate(R.layout.camera_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnViewCreated(View view, Bundle savedInstanceState) {
        frontBackToggle = (CheckBox) view.findViewById(R.id.frontBackToggle);
        cameraPreviewToggle = (CheckBox) view.findViewById(R.id.camera_preview_toggle);
        cameraLogo = view.findViewById(R.id.camera_log);
        lastImage = (ImageView) view.findViewById(R.id.camera_last_image);
        activity.backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                lastImage.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void doOnStart() {
        ((FaceDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).setCameraEventHandler(faceDetectionHandler);
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new FaceDetectionShield(activity, getControllerTag()));
        }
    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

    @Override
    public void doOnResume() {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraPreviewToggle.setEnabled(true);
                if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                    if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                        try {
                            if (getApplication().getRunningShields().get(getControllerTag()) != null)
                                if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).showPreview()) {
                                    cameraLogo.setVisibility(View.INVISIBLE);
                                    ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(true);
                                }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListeners();
                            cameraPreviewToggle.setChecked(false);
                            applyListeners();
                        }
                    } else {
                        if (getApplication().getRunningShields().get(getControllerTag()) != null)
                            try {
                                if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview()) {
                                    cameraLogo.setVisibility(View.VISIBLE);
                                    ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(false);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                removeListeners();
                                cameraPreviewToggle.setChecked(true);
                                applyListeners();
                            }
                    }
                }
            }
        }, 500);

        try {
            ((FaceDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).setCameraToPreview(((FaceDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).isBackPreview());
            ((FaceDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).invalidatePreview();
            ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(
                    ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).isFaceSelected());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        removeListeners();
        frontBackToggle.setChecked(((FaceDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());
        applyListeners();
    }

    @Override
    public void doOnPause() {
        if (getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            try {
                ((FaceDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).hidePreview();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(false);
        cameraLogo.setVisibility(View.VISIBLE);
        if (getView() != null)
            getView().invalidate();
    }

    @Override
    public void onMenuClosed() {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && cameraPreviewToggle.isChecked()) {
                    try {
                        if (getApplication().getRunningShields().get(getControllerTag()) != null)
                            if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).showPreview()) {
                                cameraLogo.setVisibility(View.INVISIBLE);
                                ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(true);
                            }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListeners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                } else {
                    if (getApplication().getRunningShields().get(getControllerTag()) != null)
                        try {
                            if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview()) {
                                cameraLogo.setVisibility(View.VISIBLE);
                                ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(false);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListeners();
                            cameraPreviewToggle.setChecked(true);
                            applyListeners();
                        }
                }
            }
        }
    }

    @Override
    public void onChange(boolean isChecked) {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (isChecked && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                    try {
                        if (getApplication().getRunningShields().get(getControllerTag()) != null)
                            if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).showPreview()) {
                                cameraLogo.setVisibility(View.INVISIBLE);
                                ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(true);
                            }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListeners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                } else if (!isChecked || activity.isMenuOpened()) {
                    try {
                        if ((getApplication().getRunningShields().get(getControllerTag()) != null))
                            if (((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview()) {
                                cameraLogo.setVisibility(View.VISIBLE);
                                ((FaceDetectionShield) getApplication().getRunningShields().get(getControllerTag())).setIsFaceSelected(false);
                            }
                        if (!isChecked && !activity.isMenuOpened()) {
                            removeListeners();
                            cameraPreviewToggle.setChecked(false);
                            applyListeners();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListeners();
                        cameraPreviewToggle.setChecked(true);
                        applyListeners();
                    }
                }
            }
        }
    }
}
