package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;

public class SliderShield extends ControllerParent<SliderShield> {
    private int connectedPin;
    private int sliderValue;
    private ShieldFrame sf;
    private static final byte DATA_IN = 0x01;
    private static final byte SLIDER_VALUE = 0x01;
    private byte sValue = 0;
    private SliderHandler sliderHandler;

    public SliderShield() {
        super();
        requiredPinsIndex = 1;
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.slider_pin_name)};
    }

    public SliderShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        connectedPin = pins[0].getPinID();

        super.setConnected(pins);
    }

    public int getSliderValue() {
        return sliderValue;
    }

    public void setSliderValue(int sliderValue) {
        if (sliderValue != this.sliderValue) {
            this.sliderValue = sliderValue;
            analogWrite(connectedPin, sliderValue);
            sValue = (byte) sliderValue;
            sf = new ShieldFrame(UIShield.SLIDER_SHIELD.getId(), DATA_IN);
            sf.addArgument(sValue);
            sendShieldFrame(sf);
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.SLIDER_SHIELD.getId()) {
            if (frame.getFunctionId() == SLIDER_VALUE) {
                sliderValue = frame.getArgumentAsInteger(0);
                if (sliderHandler != null && isHasForgroundView())
                    sliderHandler.setSliderValue(sliderValue);
            }
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        sf = null;
    }

    public void setSliderHandler(SliderHandler sliderHandler) {
        this.sliderHandler = sliderHandler;
    }

    public interface SliderHandler {
        public void setSliderValue(int value);
    }

}
