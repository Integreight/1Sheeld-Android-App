package com.integreight.firmatabluetooth;

public interface ArduinoVersionQueryHandler{
    public void onVersionReceived(int minorVersion, int majorVersion);
}