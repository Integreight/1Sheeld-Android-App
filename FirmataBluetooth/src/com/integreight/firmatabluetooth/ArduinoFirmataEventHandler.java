package com.integreight.firmatabluetooth;

public interface ArduinoFirmataEventHandler{
    public void onClose();
    public void onError(String errorMessage);
    public void onConnect();
}