package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.fragments.KeyboardFragment.KeyboardEventHandler;

public class KeyboardShield extends ControllerParent<KeyboardShield> {
    private ShieldFrame frame;
    private static final byte KEYBOARD_VALUE = (byte) 0x01;

    @Override
    public ControllerParent<KeyboardShield> init(String tag) {
        // TODO Auto-generated method stub

        return super.init(tag);
    }

    public KeyboardShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public KeyboardShield() {
        super();
    }

    private KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler() {

        @Override
        public void onKeyPressed(String myText) {

            if (isNotNullNotEmpty(myText)) {
                for (int i = 0; i < myText.length(); i++) {

                    frame = new ShieldFrame(UIShield.KEYBOARD_SHIELD.getId(),
                            KEYBOARD_VALUE);
                    frame.addArgument(myText.charAt(0));
                    sendShieldFrame(frame);

                }
            }
        }

        @Override
        public void onEnterOrbspacepressed(char myChar) {
            frame = new ShieldFrame(UIShield.KEYBOARD_SHIELD.getId(),
                    KEYBOARD_VALUE);
            frame.addArgument(myChar);
            sendShieldFrame(frame);

        }
    };

    public static boolean isNotNullNotEmpty(final String string) {
        return string != null && !string.isEmpty();
    }

    public KeyboardEventHandler getKeyboardEventHandler() {
        return keyboardEventHandler;
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        keyboardEventHandler = null;
    }

}
