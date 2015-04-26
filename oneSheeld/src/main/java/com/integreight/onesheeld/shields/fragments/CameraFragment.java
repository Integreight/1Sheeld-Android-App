package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.ShieldsOperations;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.CameraShield.CameraEventHandler;

import java.io.FileOutputStream;

public class CameraFragment extends ShieldFragmentParent<CameraFragment> implements ShieldsOperations.OnChangeListener, MainActivity.OnSlidingMenueChangeListner {
    FileOutputStream fo;
    private CameraFragmentHandler fragmentHandler;
    private ToggleButton cameraPreviewToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.camera_shield_fragment_layout, container,
                false);
        if (getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName()) != null)
            ((ShieldsOperations) getAppActivity().getSupportFragmentManager().findFragmentByTag(ShieldsOperations.class.getName())).addOnSlidingLocksListener(this);
        activity.registerSlidingMenuListner(this);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraPreviewToggle = (ToggleButton) view.findViewById(R.id.frontBackToggle);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }

        ((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).setCameraEventHandler(cameraEventHandler);
        super.onStart();

    }

    private void removeListners() {
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    private void applyListeners() {
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean feeback = ((CameraShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCameraToPreview(b);
                if (!feeback) {
                    removeListners();
                    cameraPreviewToggle.setChecked(!b);
                    applyListeners();
                }
            }
        });
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        fragmentHandler = new CameraFragmentHandler() {

            @Override
            public void onCameraFragmentIntilized() {
                // TODO Auto-generated method stub

            }
        };
        fragmentHandler.onCameraFragmentIntilized();
    }

    private CameraEventHandler cameraEventHandler = new CameraEventHandler() {

        @Override
        public void checkCameraHardware(boolean isHasCamera) {

            if (canChangeUI()) {
                if (!isHasCamera) {
                    Toast.makeText(activity, "Your Device doesn't have Camera",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void OnPictureTaken() {
            if (canChangeUI())
                Toast.makeText(activity, "Your Camera has been Captured Image",
                        Toast.LENGTH_SHORT).show();
        }

        @Override
        public void takePicture() {
        }

        @Override
        public void setFlashMode(String flash_mode) {

        }

        @Override
        public void setOnCameraPreviewTypeChanged(final boolean isBack) {
            if (canChangeUI() && cameraPreviewToggle != null && getView() != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeListners();
                        cameraPreviewToggle.setChecked(isBack);
                        applyListeners();
                    }
                });
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new CameraShield(activity, getControllerTag()));

        }

    }

    public void doOnServiceConnected() {
        initializeFirmata();
    }

    ;

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                    if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked())
                        ((CameraShield) getApplication().getRunningShields().get(
                                getControllerTag())).showPreview();
                    else
                        ((CameraShield) getApplication().getRunningShields().get(
                                getControllerTag())).hidePreview();
                }
            }
        }, 500);
        removeListners();
        cameraPreviewToggle.setChecked(((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());
        applyListeners();
    }

    @Override
    public void onPause() {
        if (getApplication().getRunningShields().get(
                getControllerTag()) != null)
            ((CameraShield) getApplication().getRunningShields().get(
                    getControllerTag())).hidePreview();
        getView().invalidate();
        super.onPause();
    }

    @Override
    public void onChange(boolean isChecked) {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (isChecked && !activity.isMenuOpened())
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).showPreview();
                else if (!isChecked || activity.isMenuOpened())
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
            }
        }
    }

    @Override
    public void onMenuClosed() {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked())
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).showPreview();
                else
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
            }
        }
    }

    public static interface CameraFragmentHandler {
        void onCameraFragmentIntilized();
    }
}
