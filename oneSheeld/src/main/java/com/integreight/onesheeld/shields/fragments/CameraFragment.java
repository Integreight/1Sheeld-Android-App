package com.integreight.onesheeld.shields.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.integreight.onesheeld.utils.customviews.OneSheeldToggleButton;

public class CameraFragment extends ShieldFragmentParent<CameraFragment> implements ShieldsOperations.OnChangeListener, MainActivity.OnSlidingMenueChangeListner {
    private CameraFragmentHandler fragmentHandler;
    private CheckBox frontBackToggle;
    private CheckBox cameraPreviewToggle;
    private View camerLogo;

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
        frontBackToggle = (CheckBox) view.findViewById(R.id.frontBackToggle);
        cameraPreviewToggle = (CheckBox) view.findViewById(R.id.camera_preview_toggle);
        camerLogo = view.findViewById(R.id.camera_log);
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
                boolean feeback = ((CameraShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCameraToPreview(b);
                if (!feeback) {
                    removeListners();
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
                        Rect rectangle = new Rect();
                        Window window = getActivity().getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                        ((CameraShield) getApplication().getRunningShields().get(
                                getControllerTag())).showPreview();
                        camerLogo.setVisibility(View.INVISIBLE);
                    }
                } else {
                    camerLogo.setVisibility(View.VISIBLE);
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
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
            if (canChangeUI() && frontBackToggle != null && getView() != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeListners();
                        frontBackToggle.setChecked(isBack);
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
                    if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                        Rect rectangle = new Rect();
                        Window window = getActivity().getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                        ((CameraShield) getApplication().getRunningShields().get(
                                getControllerTag())).showPreview();
                        camerLogo.setVisibility(View.INVISIBLE);
                    } else {
                        ((CameraShield) getApplication().getRunningShields().get(
                                getControllerTag())).hidePreview();
                        camerLogo.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, 500);
        ((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).invalidatePreview();
        removeListners();
        frontBackToggle.setChecked(((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());
        applyListeners();
    }

    @Override
    public void onPause() {
        if (getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            ((CameraShield) getApplication().getRunningShields().get(
                    getControllerTag())).hidePreview();
            camerLogo.setVisibility(View.VISIBLE);
        }
        getView().invalidate();
        super.onPause();
    }

    @Override
    public void onChange(boolean isChecked) {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (isChecked && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).showPreview();
                    camerLogo.setVisibility(View.INVISIBLE);
                } else if (!isChecked || activity.isMenuOpened()) {
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
                    camerLogo.setVisibility(View.VISIBLE);
                    if (!isChecked && !activity.isMenuOpened()) {
                        removeListners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                }
            }
        }
    }

    @Override
    public void onMenuClosed() {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (((CheckBox) activity.findViewById(R.id.isMenuOpening)).isChecked() && cameraPreviewToggle.isChecked()) {
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).showPreview();
                    camerLogo.setVisibility(View.INVISIBLE);
                } else {
                    ((CameraShield) getApplication().getRunningShields().get(
                            getControllerTag())).hidePreview();
                    camerLogo.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static interface CameraFragmentHandler {
        void onCameraFragmentIntilized();
    }
}
