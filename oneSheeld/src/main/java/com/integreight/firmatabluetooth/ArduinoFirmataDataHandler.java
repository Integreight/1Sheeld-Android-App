package com.integreight.firmatabluetooth;

public interface ArduinoFirmataDataHandler {
    public void onSysex(byte command, byte[] data);

    public void onAnalog(int pin, int value);

    public void onDigital(int portNumber, int portData);
}