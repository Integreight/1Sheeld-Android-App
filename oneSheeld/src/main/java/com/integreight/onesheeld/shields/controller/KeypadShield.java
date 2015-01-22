package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.BitsUtils;

public class KeypadShield extends ControllerParent<KeypadShield> {
    private static final byte DATA_IN = 0x01;

    public KeypadShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<KeypadShield> init(String tag) {
        return super.init(tag);
    }

    public KeypadShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{"Row 0", "Row 1", "Row 2", "Row 3",
                "Column 0", "Column 1", "Column 2", "Column 3"};
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        // TODO Auto-generated method stub
        super.setConnected(pins);
    }

    public void initPins() {

    }

    ShieldFrame sf;
    byte rowByte = 0, columnByte = 0;

    public void setRowAndColumn(int row, int column) {
        ArduinoPin columnPin = matchedShieldPins.get("Column " + column);
        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, ArduinoFirmata.HIGH);
        }
        columnByte = BitsUtils.setBit(columnByte, column);
        ArduinoPin rowPin = matchedShieldPins.get("Row " + row);
        if (rowPin != null) {
            digitalWrite(rowPin.microHardwarePin, ArduinoFirmata.HIGH);
        }
        rowByte = BitsUtils.setBit(rowByte, row);
        sf = new ShieldFrame(UIShield.KEYPAD_SHIELD.getId(), DATA_IN);
        sf.addByteArgument(rowByte);
        sf.addByteArgument(columnByte);
        sendShieldFrame(sf);
    }

    public void resetRowAndColumn(int row, int column) {
        ArduinoPin columnPin = matchedShieldPins.get("Column " + column);
        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, ArduinoFirmata.LOW);
        }
        columnByte = BitsUtils.resetBit(columnByte, column);
        ArduinoPin rowPin = matchedShieldPins.get("Row " + row);
        if (rowPin != null) {
            digitalWrite(rowPin.microHardwarePin, ArduinoFirmata.LOW);
        }
        rowByte = BitsUtils.resetBit(rowByte, row);
        sf = new ShieldFrame(UIShield.KEYPAD_SHIELD.getId(), DATA_IN);
        sf.addByteArgument(rowByte);
        sf.addByteArgument(columnByte);
        sendShieldFrame(sf);

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
