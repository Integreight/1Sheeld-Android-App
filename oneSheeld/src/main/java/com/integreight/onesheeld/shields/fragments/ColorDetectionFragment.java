package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.customviews.ComboSeekBar;
import com.integreight.onesheeld.utils.customviews.OneSheeldToggleButton;

import java.util.Arrays;

public class ColorDetectionFragment extends ShieldFragmentParent<ColorDetectionFragment> implements ColorDetectionShield.ColorDetectionEventHandler {
    View normalColor;
    LinearLayout fullColor;
    OneSheeldToggleButton operationToggle;
    OneSheeldToggleButton scaleToggle;
    OneSheeldToggleButton typeToggle;
    private ToggleButton cameraPreviewToggle;
    ComboSeekBar scaleSeekBar;
    ComboSeekBar patchSizeSeekBar;
    private String[] grayScale = new String[]{"1 Bit", "2 Bit", "4 Bit", "8 Bit"};
    private String[] rgbScale = new String[]{"3 Bit", "6 Bit", "9 Bit", "12 Bit", "15 Bit", "18 Bit", "24 Bit"};
    private String[] patchSizes = new String[]{"Small", "Medium", "Large"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.color_detection_shield_fragment_layout, container,
                false);
        setHasOptionsMenu(true);
        return v;
    }

    private void applyListeners() {
        operationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.CENTER);
                    notifyNormalColor();
                } else {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.NINE_FRAMES);
                    notifyFullColor();
                }
            }
        });
        scaleToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette._24_BIT_RGB);
                } else {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette._8_BIT_GRAYSCALE);
                }
                scaleSeekBar.setAdapter(Arrays.asList(b ? rgbScale : grayScale));
                scaleSeekBar.setSelection((b ? rgbScale : grayScale).length - 1);
            }
        });
        typeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setColorType(b ? ColorDetectionShield.COLOR_TYPE.COMMON : ColorDetectionShield.COLOR_TYPE.AVERAGE);

            }
        });
        scaleSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCurrentPallete(ColorDetectionShield.ColorPalette.get((byte) ((scaleToggle.isChecked() ? 4 : 0) + 1 + i)));
            }
        });
        patchSizeSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setPatchSize(i == 0 ? ColorDetectionShield.PATCH_SIZE.SMALL : i == 1 ? ColorDetectionShield.PATCH_SIZE.MEDIUM : ColorDetectionShield.PATCH_SIZE.LARGE);
            }
        });
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean feeback = ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setCameraToPreview(b);
                if (!feeback) {
                    removeListeners();
                    cameraPreviewToggle.setChecked(!b);
                    applyListeners();
                }
            }
        });
    }

    private void removeListeners() {
        operationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        scaleToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        typeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        scaleSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        patchSizeSeekBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        cameraPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        normalColor = view.findViewById(R.id.normalColor);
        fullColor = (LinearLayout) view.findViewById(R.id.fullColor);
        operationToggle = (OneSheeldToggleButton) view.findViewById(R.id.operation);
        scaleToggle = (OneSheeldToggleButton) view.findViewById(R.id.scale);
        typeToggle = (OneSheeldToggleButton) view.findViewById(R.id.type);
        cameraPreviewToggle = (ToggleButton) view.findViewById(R.id.frontBackToggle);
        scaleSeekBar = (ComboSeekBar) view.findViewById(R.id.scaleSeekBar);
        patchSizeSeekBar = (ComboSeekBar) view.findViewById(R.id.patchSeekBar);
        ((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).setColorEventHandler(this);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        removeListeners();
        if (((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.CENTER)
            enableNormalColor();
        else
            enableFullColor();
        scaleToggle.setChecked(!((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getCurrentPallete().isGrayscale());
        typeToggle.setChecked(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getColorType() == ColorDetectionShield.COLOR_TYPE.COMMON);
        scaleSeekBar.setAdapter(Arrays.asList(scaleToggle.isChecked() ? rgbScale : grayScale));
        scaleSeekBar.setSelection(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getCurrentPallete().getIndex());
        patchSizeSeekBar.setAdapter(Arrays.asList(patchSizes));
        patchSizeSeekBar.setSelection(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getPatchSize() == ColorDetectionShield.PATCH_SIZE.SMALL ? 0 : ((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getPatchSize() == ColorDetectionShield.PATCH_SIZE.MEDIUM ? 1 : 2);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(activity!=null&&activity.findViewById(R.id.isMenuOpening)!=null)
                    ((CheckBox) activity.findViewById(R.id.isMenuOpening)).setChecked(true);
            }
        },500);
        applyListeners();
        super.onStart();

    }


    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        cameraPreviewToggle.setChecked(((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).isBackPreview());

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onColorChanged(final int... color) {
        if (normalColor != null && getView() != null && getApplication().getRunningShields().get(
                getControllerTag()) != null) {
            if (((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.CENTER && color.length > 0) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        normalColor.setBackgroundColor(color[0]);
                    }
                });
            } else if (((ColorDetectionShield) getApplication().getRunningShields().get(
                    getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.NINE_FRAMES && color.length > 0) {
                int k = 0;
                for (int i = 0; i < fullColor.getChildCount(); i++) {
                    for (int j = 0; j < ((LinearLayout) fullColor.getChildAt(i)).getChildCount(); j++) {
                        final View cell = ((LinearLayout) fullColor.getChildAt(i)).getChildAt(j);
                        final int m = k;
                        cell.removeCallbacks(null);
                        cell.post(new Runnable() {
                            @Override
                            public void run() {
                                if (m < color.length) {
//                                    Log.d("logColor", color.length + "   " + m + "   " + color[m]);
                                    cell.setBackgroundColor(color[m]);
                                }
                            }
                        });
                        k++;
                    }
                }
            }
        }
    }

    public void notifyFullColor() {
        if (normalColor != null && getView() != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    normalColor.setVisibility(View.INVISIBLE);
                    fullColor.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void notifyNormalColor() {
        if (normalColor != null && getView() != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    normalColor.setVisibility(View.VISIBLE);
                    fullColor.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void enableFullColor() {
        if (operationToggle != null && getView() != null) {
            notifyFullColor();
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (operationToggle != null && getView() != null)
                            operationToggle.setChecked(false);
                    }
                });
        }
    }

    @Override
    public void enableNormalColor() {
        if (operationToggle != null && getView() != null) {
            notifyNormalColor();
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (operationToggle != null && getView() != null)
                            operationToggle.setChecked(true);
                    }
                });
        }
    }

    @Override
    public void setPallete(final ColorDetectionShield.ColorPalette pallete) {
        if (normalColor != null && getView() != null) {
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (scaleToggle != null && getView() != null) {
                            removeListeners();
                            scaleToggle.setChecked(!pallete.isGrayscale());
                            scaleSeekBar.setAdapter(Arrays.asList(scaleToggle.isChecked() ? rgbScale : grayScale));
                            scaleSeekBar.setSelection(pallete.getIndex());
                            applyListeners();
                        }
                    }
                });
        }
    }

    @Override
    public void changeCalculationMode(final ColorDetectionShield.COLOR_TYPE type) {
        if (typeToggle != null && getView() != null) {
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeListeners();
                        typeToggle.setChecked(type == ColorDetectionShield.COLOR_TYPE.COMMON);
                        applyListeners();
                    }
                });
        }
    }

    @Override
    public void changePatchSize(final ColorDetectionShield.PATCH_SIZE patchSize) {
        if (scaleSeekBar != null && getView() != null) {
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeListeners();
                        patchSizeSeekBar.setSelection(patchSize == ColorDetectionShield.PATCH_SIZE.SMALL ? 0 : patchSize == ColorDetectionShield.PATCH_SIZE.MEDIUM ? 1 : 2);
                        applyListeners();
                    }
                });
        }
    }

    @Override
    public void onCameraPreviewTypeChanged(final boolean isBack) {
        if (canChangeUI() && getView() != null && cameraPreviewToggle != null)
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    removeListeners();
                    cameraPreviewToggle.setChecked(isBack);
                    applyListeners();
                }
            });
    }
}
