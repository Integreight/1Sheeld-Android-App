package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.BitsUtils;
import com.integreight.onesheeld.utils.Log;

public class ToggleButtonShield extends ControllerParent<ToggleButtonShield> {
    private int connectedPin;
    private boolean isButtonOn;
    private static final byte DATA_IN = 0x01;

    public ToggleButtonShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.toggle_button_pin_name)};
    }

    public ToggleButtonShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        connectedPin = pins[0].getPinID();
        super.setConnected(pins);
    }

    public boolean isButtonOn() {
        return isButtonOn;
    }

    private ShieldFrame sf;
    private byte toggle = 0;

    public void setButton(boolean isButtonOn) {
        if (connectedPin != -1) {
            this.isButtonOn = isButtonOn;
            digitalWrite(connectedPin, isButtonOn);
            Log.sysOut(connectedPin + "    " + isButtonOn);
        }
        toggle = isButtonOn ? BitsUtils.setBit(toggle, 1) : BitsUtils.resetBit(
                toggle, 1);
        sf = new ShieldFrame(UIShield.TOGGLEBUTTON_SHIELD.getId(), DATA_IN);
        sf.addArgument(toggle);
        sendShieldFrame(sf);

    }

    public boolean getButton(){
        return isButtonOn;
    }
    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
