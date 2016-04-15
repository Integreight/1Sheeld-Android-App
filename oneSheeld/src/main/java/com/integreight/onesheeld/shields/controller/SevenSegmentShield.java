package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.BitsUtils;

import java.util.Hashtable;
import java.util.Map.Entry;

public class SevenSegmentShield extends ControllerParent<SevenSegmentShield> {
    private SevenSegmentsEventHandler eventHandler;
    public Hashtable<String, Boolean> pinsStatus = new Hashtable<String, Boolean>();

    public SevenSegmentShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{"  A  ", "  B  ", "  C  ", "  D  ",
                "  E  ", "  F  ", "  G  ", " DOT "};
        for (int i = 0; i < shieldPins.length; i++) {
            pinsStatus.put(shieldPins[i], false);
        }
    }

    public SevenSegmentShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<SevenSegmentShield> init(String tag) {
        return super.init(tag);
    }

    public Hashtable<String, Boolean> getSegmentsStatus() {
        return pinsStatus;
    }

    public Hashtable<String, Boolean> refreshSegments() {

        updateSegmentsStatusFromFirmata();
        return pinsStatus;
    }

    @Override
    public void onDigital(int portNumber, boolean portData) {
        updateSegmentsStatusFromFirmata();
        if (eventHandler != null) {
            eventHandler.onSegmentsChange(pinsStatus);
        }

        super.onDigital(portNumber, portData);
    }

    public void setSevenSegmentsEventHandler(
            SevenSegmentsEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        updateSegmentsStatusFromFirmata();
    }

    public static interface SevenSegmentsEventHandler {
        void onSegmentsChange(Hashtable<String, Boolean> segmentsStatus);
    }

    private synchronized void updateSegmentsStatusFromFirmata() {
        for (Entry<String, Boolean> entry : pinsStatus.entrySet()) {
            pinsStatus.put(entry.getKey(), false);
        }
        if (getApplication().isConnectedToBluetooth())
            for (Entry<String, ArduinoPin> entry : matchedShieldPins.entrySet()) {
                pinsStatus.put(entry.getKey(), getApplication().getConnectedDevice().digitalRead(entry.getValue().microHardwarePin));
            }

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.SEVENSEGMENT_SHIELD.getId()
                && frame.getFunctionId() == 0x01)
            for (int i = 0; i < shieldPins.length; i++) {
                pinsStatus.put(shieldPins[i],
                        BitsUtils.isBitSet(frame.getArgument(0)[0], i));
                if (eventHandler != null) {
                    eventHandler.onSegmentsChange(pinsStatus);
                }
            }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
