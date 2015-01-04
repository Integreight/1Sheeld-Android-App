package com.integreight.onesheeld.model;

public class ArduinoConnectedPin {
    private int pinID;
    private byte pinMode;

    public ArduinoConnectedPin() {
    }

    public ArduinoConnectedPin(int pinID, byte pinMode) {
        super();
        this.pinID = pinID;
        this.pinMode = pinMode;
    }

    public int getPinID() {
        return pinID;
    }

    public void setPinID(int pinID) {
        this.pinID = pinID;
    }

    public byte getPinMode() {
        return pinMode;
    }

    public void setPinMode(byte pinMode) {
        this.pinMode = pinMode;
    }

}
