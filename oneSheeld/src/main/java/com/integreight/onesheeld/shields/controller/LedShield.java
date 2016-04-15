package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class LedShield extends ControllerParent<LedShield> {
    public int connectedPin = -1;
    private boolean isLedOn;
    private LedEventHandler eventHandler;

    public LedShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.led_pin_name)};
    }

    public LedShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<LedShield> init(String tag) {
        return super.init(tag);
    }

    public boolean isLedOn() {
        return isLedOn;
    }

    public boolean refreshLed() {
        if (connectedPin != -1) {
            if (getApplication().isConnectedToBluetooth())
                isLedOn = getApplication().getConnectedDevice().digitalRead(connectedPin);
        } else
            isLedOn = false;

        return isLedOn;
    }

    @Override
    public void onDigital(int portNumber, boolean portData) {
        refreshLed();
        if (eventHandler != null) {
            eventHandler.onLedChange(isLedOn);
        }

        super.onDigital(portNumber, portData);
    }

    public void setLedEventHandler(LedEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        refreshLed();
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        this.connectedPin = pins[0].getPinID();
        super.setConnected(pins);
    }

    public static interface LedEventHandler {
        void onLedChange(boolean isLedOn);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.LED_SHIELD.getId()) {
            if (eventHandler != null) {
                eventHandler.onLedChange(frame.getFunctionId() == 0x01
                        && frame.getArgument(0)[0] == 0x1);
            }
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        Log.sysOut("Reset");
    }
}
