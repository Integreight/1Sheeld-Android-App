package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SliderShield;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;
import com.integreight.onesheeld.utils.customviews.VerticalSeekBar;

public class SliderFragment extends ShieldFragmentParent<SliderFragment> {

    VerticalSeekBar seekBar;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.slider_shield_fragment_layout, container,
                false);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        seekBar = (VerticalSeekBar) v.findViewById(R.id.slider_fragment_seekbar);
        seekBar.setEnabled(false);
        seekBar.setMax(255);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                ((SliderShield) getApplication().getRunningShields().get(
                        getControllerTag())).setSliderValue(progress);

            }
        });

        if ((getApplication().getRunningShields().get(
                getControllerTag())) != null)
            seekBar.setEnabled(true);
    }

    @Override
    public void doOnStart() {
        ConnectingPinsView.getInstance().reset(
                getApplication().getRunningShields().get(getControllerTag()),
                new OnPinSelectionListener() {

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null) {
                            int pinCode = pin.microHardwarePin;
                            (getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag()))
                                    .setConnected(new ArduinoConnectedPin(
                                            pinCode, OneSheeldDevice.OUTPUT));
                            seekBar.setEnabled(true);
                        } else {
                            seekBar.setEnabled(false);
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                    }
                }); // TODO Auto-generated method stub
        seekBar.removeCallbacks(null);
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                if (seekBar != null && ((SliderShield) getApplication().getRunningShields().get(
                        getControllerTag())).getSliderValue() > 0 && ((SliderShield) getApplication().getRunningShields().get(
                        getControllerTag())).getSliderValue() <= 255) {
                    seekBar.setProgress(((SliderShield) getApplication().getRunningShields().get(
                            getControllerTag())).getSliderValue());
                    seekBar.refreshDrawableState();
                    seekBar.updateThumb();
                }
            }
        });
        ((SliderShield) getApplication().getRunningShields().get(
                getControllerTag())).setSliderHandler(new SliderShield.SliderHandler() {
            @Override
            public void setSliderValue(final int value) {
                seekBar.removeCallbacks(null);
                seekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        if (seekBar != null && value > 0 && value <= 255) {
                            seekBar.setProgress(value);
                            seekBar.refreshDrawableState();
                            seekBar.updateThumb();
                        }
                    }
                });
            }
        });

    }

    private void initializeFirmata() {

        if (getApplication().getRunningShields().get(getControllerTag()) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new SliderShield(activity, getControllerTag()));

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
