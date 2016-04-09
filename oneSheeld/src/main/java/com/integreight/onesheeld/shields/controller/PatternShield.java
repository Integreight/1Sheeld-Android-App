package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.customviews.LockPatternViewEx;

import java.util.List;

public class PatternShield extends
        ControllerParent<ControllerParent<PatternShield>> {
    private static final byte SEND_PATTERN = (byte) 0x01;
    private ShieldFrame frame;
    byte patternPath[];
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
    public void onPatternDetected(List<LockPatternViewEx.Cell> pattern) {
        frame=new ShieldFrame(UIShield.PATTERN_SHIELD.getId(),SEND_PATTERN);
        patternPath=new byte[pattern.size()];
        for (int i=0;i<pattern.size();i++)
        {
            patternPath[i]=(byte)((pattern.get(i).getRow())|((pattern.get(i).getColumn())<<4));
        }
        frame.addArgument(patternPath);
        sendShieldFrame(frame,true);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }


}
