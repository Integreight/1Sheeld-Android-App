package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
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
        shieldPins = new String[]{"Up Arrow", "Right Arrow", "Down Arrow",
                "Left Arrow", "Yellow Button", "Red Button", "Green Button",
                "Blue Button"};
    }

    public void setPinToHigh(String pinName, int pinId) {
        ArduinoPin columnPincolumnPin = matchedShieldPins.get(pinName);
        if (columnPincolumnPin != null) {
            digitalWrite(columnPincolumnPin.microHardwarePin,
                    ArduinoFirmata.HIGH);
        }
        buttonByte = BitsUtils.setBit(buttonByte, pinId);
        sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
        sf.addByteArgument(buttonByte);
        sendShieldFrame(sf);
    }

    public void setPinToLow(String pinName, int pinId) {
        ArduinoPin columnPin = matchedShieldPins.get(pinName);

        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, ArduinoFirmata.LOW);
        }
        buttonByte = BitsUtils.resetBit(buttonByte, pinId);
        sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), DATA_IN);
        sf.addByteArgument(buttonByte);
        sendShieldFrame(sf);
    }

    public static enum Pin {
        UP_ARROW(4, "Up Arrow"), RIGHT_ARROW(7, "Right Arrow"), DOWN_ARROW(5,
                "Down Arrow"), LEFT_ARROW(6, "Left Arrow"), YELLOW_BUTTON(0,
                "Yellow Button"), RED_BUTTON(1, "Red Button"), GREEN_BUTTON(2,
                "Green Button"), BLUE_BUTTON(3, "Blue Button");

        String name;
        int id;

        Pin(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
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
