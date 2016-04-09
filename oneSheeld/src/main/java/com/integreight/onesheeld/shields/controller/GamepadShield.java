package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.BitsUtils;

public class GamepadShield extends ControllerParent<GamepadShield> {
    ShieldFrame sf;
    byte buttonByte = 0;
    private static final byte DATA_IN = 0x01;

    public GamepadShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<GamepadShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        // TODO Auto-generated method stub
        super.setConnected(pins);
    }

    public GamepadShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.game_pad_up_arrow), OneSheeldApplication.getContext().getString(R.string.game_pad_right_arrow), OneSheeldApplication.getContext().getString(R.string.game_pad_down_arrow),
                OneSheeldApplication.getContext().getString(R.string.game_pad_left_arrow), OneSheeldApplication.getContext().getString(R.string.game_pad_yellow_button), OneSheeldApplication.getContext().getString(R.string.game_pad_red_button), OneSheeldApplication.getContext().getString(R.string.game_pad_green_button),
                OneSheeldApplication.getContext().getString(R.string.game_pad_blue_button)};
    }

    public void setPinToHigh(String pinName, int pinId) {
        ArduinoPin columnPincolumnPin = matchedShieldPins.get(pinName);
        if (columnPincolumnPin != null) {
            digitalWrite(columnPincolumnPin.microHardwarePin,
                    true);
        }
        buttonByte = BitsUtils.setBit(buttonByte, pinId);
        sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
        sf.addArgument(buttonByte);
        sendShieldFrame(sf);
    }

    public void setPinToLow(String pinName, int pinId) {
        ArduinoPin columnPin = matchedShieldPins.get(pinName);

        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, false);
        }
        buttonByte = BitsUtils.resetBit(buttonByte, pinId);
        sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
        sf.addArgument(buttonByte);
        sendShieldFrame(sf);
    }

    public static enum Pin {
        UP_ARROW(4, R.string.game_pad_up_arrow), RIGHT_ARROW(7, R.string.game_pad_right_arrow), DOWN_ARROW(5,
                R.string.game_pad_down_arrow), LEFT_ARROW(6, R.string.game_pad_left_arrow), YELLOW_BUTTON(0,
                R.string.game_pad_yellow_button), RED_BUTTON(1, R.string.game_pad_red_button), GREEN_BUTTON(2,
                R.string.game_pad_green_button), BLUE_BUTTON(3, R.string.game_pad_blue_button);

        private int nameStringResource;
        int id;

        Pin(int id, int nameStringResource) {
            this.id = id;
            this.nameStringResource = nameStringResource;
        }

        public String getName() {
            return OneSheeldApplication.getContext().getString(nameStringResource);
        }

        public int getId() {
            return id;
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        sf = null;
    }
}
