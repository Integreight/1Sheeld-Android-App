package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
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
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.keypad_row)+" 0", OneSheeldApplication.getContext().getString(R.string.keypad_row)+" 1", OneSheeldApplication.getContext().getString(R.string.keypad_row)+" 2", OneSheeldApplication.getContext().getString(R.string.keypad_row)+" 3",
                OneSheeldApplication.getContext().getString(R.string.keypad_column)+" 0", OneSheeldApplication.getContext().getString(R.string.keypad_column)+" 1", OneSheeldApplication.getContext().getString(R.string.keypad_column)+" 2", OneSheeldApplication.getContext().getString(R.string.keypad_column)+" 3"};
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
        ArduinoPin columnPin = matchedShieldPins.get(activity.getString(R.string.keypad_column)+" " + column);
        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, true);
        }
        columnByte = BitsUtils.setBit(columnByte, column);
        ArduinoPin rowPin = matchedShieldPins.get(activity.getString(R.string.keypad_row)+" " + row);
        if (rowPin != null) {
            digitalWrite(rowPin.microHardwarePin, true);
        }
        rowByte = BitsUtils.setBit(rowByte, row);
        sf = new ShieldFrame(UIShield.KEYPAD_SHIELD.getId(), DATA_IN);
        sf.addArgument(rowByte);
        sf.addArgument(columnByte);
        sendShieldFrame(sf);
    }

    public void resetRowAndColumn(int row, int column) {
        ArduinoPin columnPin = matchedShieldPins.get(activity.getString(R.string.keypad_column)+" " + column);
        if (columnPin != null) {
            digitalWrite(columnPin.microHardwarePin, false);
        }
        columnByte = BitsUtils.resetBit(columnByte, column);
        ArduinoPin rowPin = matchedShieldPins.get(activity.getString(R.string.keypad_row)+" " + row);
        if (rowPin != null) {
            digitalWrite(rowPin.microHardwarePin, false);
        }
        rowByte = BitsUtils.resetBit(rowByte, row);
        sf = new ShieldFrame(UIShield.KEYPAD_SHIELD.getId(), DATA_IN);
        sf.addArgument(rowByte);
        sf.addArgument(columnByte);
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
