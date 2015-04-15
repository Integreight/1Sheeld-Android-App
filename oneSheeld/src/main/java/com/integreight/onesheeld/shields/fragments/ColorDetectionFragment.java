package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.utils.customviews.OneSheeldToggleButton;

public class ColorDetectionFragment extends ShieldFragmentParent<ColorDetectionFragment> implements ColorDetectionShield.ColorDetectionEventHandler {
    View normalColor;
    LinearLayout fullColor;
    OneSheeldToggleButton operationToggle;
    OneSheeldToggleButton scaleToggle;
    OneSheeldToggleButton typeToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.color_detection_shield_fragment_layout, container,
                false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        normalColor = view.findViewById(R.id.normalColor);
        fullColor = (LinearLayout) view.findViewById(R.id.fullColor);
        operationToggle = (OneSheeldToggleButton) view.findViewById(R.id.operation);
        scaleToggle = (OneSheeldToggleButton) view.findViewById(R.id.scale);
        typeToggle = (OneSheeldToggleButton) view.findViewById(R.id.type);
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
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.CENTER);
                } else {
                    ((ColorDetectionShield) getApplication().getRunningShields().get(
                            getControllerTag())).setRecevedFramesOperation(ColorDetectionShield.RECEIVED_FRAMES.NINE_FRAMES);
                }
            }
        });
        typeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((ColorDetectionShield) getApplication().getRunningShields().get(
                        getControllerTag())).setColorType(b ? ColorDetectionShield.COLOR_TYPE.COMMON : ColorDetectionShield.COLOR_TYPE.AVERAGE);

            }
        });
        ((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).setColorEventHandler(this);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        if (((ColorDetectionShield) getApplication().getRunningShields().get(
                getControllerTag())).getRecevedFramesOperation() == ColorDetectionShield.RECEIVED_FRAMES.CENTER)
            enableNormalColor();
        else
            enableFullColor();
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

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onColorChanged(final int... color) {
        if (normalColor != null && fullColor != null && getApplication().getRunningShields().get(
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
        if (normalColor != null && fullColor != null) {
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
        if (normalColor != null && fullColor != null) {
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

    @Override
    public void enableNormalColor() {
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

    @Override
    public void setPallete(final ColorDetectionShield.ColorPalette pallete) {
        if (normalColor != null && fullColor != null) {
            if (uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (scaleToggle != null && getView() != null)
                            scaleToggle.setChecked(!pallete.isGrayscale());
                    }
                });
        }
    }
}
