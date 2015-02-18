package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SliderShield;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;

public class SliderFragment extends ShieldFragmentParent<SliderFragment> {

    SeekBar seekBar;
    Button connectButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.slider_shield_fragment_layout, container,
                false);
        seekBar = (SeekBar) v.findViewById(R.id.slider_fragment_seekbar);
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

        if (((SliderShield) getApplication().getRunningShields().get(
                getControllerTag())) != null)
            seekBar.setEnabled(true);
        ((SliderShield) getApplication().getRunningShields().get(
                getControllerTag())).setSliderHandler(new SliderShield.SliderHandler() {
            @Override
            public void setSliderValue(int value) {
                if (seekBar != null && value > 0 && value <= 255)
                    seekBar.setProgress(value);
            }
        });
        return v;

    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        ConnectingPinsView.getInstance().reset(
                getApplication().getRunningShields().get(getControllerTag()),
                new OnPinSelectionListener() {

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null) {
                            int pinCode = pin.microHardwarePin;
                            ((SliderShield) getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag()))
                                    .setConnected(new ArduinoConnectedPin(
                                            pinCode, ArduinoFirmata.OUTPUT));
                            seekBar.setEnabled(true);
                        } else {
                            seekBar.setEnabled(false);
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                    }
                }); // TODO Auto-generated method stub
        ((SliderShield) getApplication().getRunningShields().get(
                getControllerTag())).setSliderHandler(new SliderShield.SliderHandler() {
            @Override
            public void setSliderValue(int value) {
                if (seekBar != null)
                    seekBar.setProgress(value);
            }
        });
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

    private void initializeFirmata(ArduinoFirmata firmata) {

        if (getApplication().getRunningShields().get(getControllerTag()) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new SliderShield(activity, getControllerTag()));

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata(getApplication().getAppFirmata());
    }

}
