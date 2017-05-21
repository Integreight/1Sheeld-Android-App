package com.integreight.onesheeld.shields.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.integreight.onesheeld.BuildConfig;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.appFragments.ShieldsOperations;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.CameraShield.CameraEventHandler;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;

import java.io.File;

public class CameraFragment extends ShieldFragmentParent<CameraFragment> implements ShieldsOperations.OnChangeListener, MainActivity.OnSlidingMenueChangeListner {
    Bitmap lastImageBitmap;
    private CheckBox frontBackToggle;
    private CheckBox cameraPreviewToggle;
    private ImageView lastImage;
    private View cameraLogo;
    private String lastImageSrc = null;
    private CameraEventHandler cameraEventHandler = new CameraEventHandler() {

        @Override
        public void checkCameraHardware(boolean isHasCamera) {
        }

        @Override
        public void OnPictureTaken() {
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

        @Override
        public void updatePreviewButton(final String lastImagePath) {
            if (canChangeUI() && frontBackToggle != null && getView() != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (lastImagePath != null && !lastImagePath.equals("")) {
                            lastImageSrc = lastImagePath;
                            lastImageBitmap = BitmapFactory.decodeFile(lastImageSrc);
                            if (lastImage != null && lastImageBitmap != null) {
                                lastImageBitmap = Bitmap.createScaledBitmap(lastImageBitmap, 50, 50, true);
                                lastImage.setImageBitmap(lastImageBitmap);
                                lastImage.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
        }
    };

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
        lastImage = (ImageView) view.findViewById(R.id.camera_last_image);
        cameraLogo = view.findViewById(R.id.camera_log);

        activity.backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    lastImageSrc = CameraUtils.getLastCapturedImagePathFromOneSheeldFolder(activity, false);
                } catch (SecurityException e) {
                }
                if (lastImageSrc != null) {
                    lastImageBitmap = BitmapFactory.decodeFile(lastImageSrc);
                    if (lastImage != null && lastImageBitmap != null) {
                        lastImageBitmap = Bitmap.createScaledBitmap(lastImageBitmap, 50, 50, true);
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                lastImage.setImageBitmap(lastImageBitmap);
                                lastImage.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else
                    uiHandler.post(new Runnable() {
                                       @Override
                                       public void run() {
                                           lastImage.setVisibility(View.INVISIBLE);
                                       }
                                   });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (lastImage != null) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                lastImage.setAlpha((float) 1);
                            }
                        }
                    });
                    try {
                        lastImageSrc = CameraUtils.getLastCapturedImagePathFromOneSheeldFolder(activity, false);
                    } catch (SecurityException e) {
                    }
                    if (lastImageSrc != null) {
                        lastImageBitmap = BitmapFactory.decodeFile(lastImageSrc);
                        if (lastImage != null && lastImageBitmap != null) {
                            lastImageBitmap = Bitmap.createScaledBitmap(lastImageBitmap, 50, 50, true);
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    lastImage.setImageBitmap(lastImageBitmap);
                                    lastImage.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                lastImage.setVisibility(View.INVISIBLE);
                            }
                        });
                }
            }
        });
    }

    @Override
    public void doOnStart() {
        ((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).setCameraEventHandler(cameraEventHandler);
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
        lastImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                        if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                            try {
                                if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).showPreview())
                                    cameraLogo.setVisibility(View.INVISIBLE);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                removeListners();
                                cameraPreviewToggle.setChecked(false);
                                applyListeners();
                            }
                    }
                } else {
                    if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                        try {
                            if ((((CameraShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview()))
                                cameraLogo.setVisibility(View.VISIBLE);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListners();
                            cameraPreviewToggle.setChecked(true);
                            applyListeners();
                        }
                }
            }
        });
        lastImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (lastImageSrc != null) {
                    File img = new File(lastImageSrc);
                    if (img.exists()) {
                        cameraPreviewToggle.setEnabled(false);
                        if (Build.VERSION.SDK_INT >= 24) {
                            Uri fileURI = FileProvider.getUriForFile(activity,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    img);
                            intent.setDataAndType(fileURI, "image/*");
                        } else {
                            intent.setDataAndType(Uri.fromFile(img), "image/*");
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            v.setAlpha((float) 0.5);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void doOnActivityCreated(Bundle savedInstanceState) {
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new CameraShield(activity, getControllerTag()));
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
                            if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                                if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).showPreview())
                                    cameraLogo.setVisibility(View.INVISIBLE);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListners();
                            cameraPreviewToggle.setChecked(false);
                            applyListeners();
                        }
                    } else {
                        if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                            try {
                                if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview())
                                    cameraLogo.setVisibility(View.VISIBLE);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                removeListners();
                                cameraPreviewToggle.setChecked(true);
                                applyListeners();
                            }
                    }
                }
            }
        }, 500);

        try {
            ((CameraShield) getApplication().getRunningShields().get(
                    getControllerTag())).setCameraToPreview(((CameraShield) getApplication().getRunningShields().get(
                    getControllerTag())).isBackPreview());
            ((CameraShield) getApplication().getRunningShields().get(
                    getControllerTag())).invalidatePreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        removeListners();
        frontBackToggle.setChecked(((CameraShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());
        applyListeners();

    }

    @Override
    public void doOnPause() {
        if (getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            try {
                ((CameraShield) getApplication().getRunningShields().get(
                        getControllerTag())).hidePreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        cameraLogo.setVisibility(View.VISIBLE);
        if (getView() != null) getView().invalidate();
    }

    @Override
    public void onChange(boolean isChecked) {
        if (canChangeUI() && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (activity != null && activity.findViewById(R.id.isMenuOpening) != null) {
                if (isChecked && !activity.isMenuOpened() && cameraPreviewToggle.isChecked()) {
                    try {
                        if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                            if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).showPreview())
                                cameraLogo.setVisibility(View.INVISIBLE);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                } else if (!isChecked || activity.isMenuOpened()) {
                    try {
                        if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                            if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview())
                                cameraLogo.setVisibility(View.VISIBLE);
                        if (!isChecked && !activity.isMenuOpened()) {
                            removeListners();
                            cameraPreviewToggle.setChecked(false);
                            applyListeners();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListners();
                        cameraPreviewToggle.setChecked(true);
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
                    try {
                        if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                            if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).showPreview())
                                cameraLogo.setVisibility(View.INVISIBLE);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeListners();
                        cameraPreviewToggle.setChecked(false);
                        applyListeners();
                    }
                } else {
                    if ((CameraShield) getApplication().getRunningShields().get(getControllerTag()) != null)
                        try {
                            if (((CameraShield) getApplication().getRunningShields().get(getControllerTag())).hidePreview())
                                cameraLogo.setVisibility(View.VISIBLE);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeListners();
                            cameraPreviewToggle.setChecked(true);
                            applyListeners();
                        }
                }
            }
        }
    }
}
