package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;

public class PatternShield extends
        ControllerParent<ControllerParent<PatternShield>> {
    private static final byte CLOCK_COMMAND = (byte) 0x21;
    private static final byte CLOCK_VALUE = (byte) 0x01;

    @Override
    public ControllerParent<ControllerParent<PatternShield>> init(String tag) {
        // TODO Auto-generated method stub
        return super.init(tag);
    }

    public PatternShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public PatternShield() {
        super();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }


}
