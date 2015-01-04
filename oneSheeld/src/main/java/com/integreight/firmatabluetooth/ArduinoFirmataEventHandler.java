package com.integreight.firmatabluetooth;

public interface ArduinoFirmataEventHandler {
    public void onClose(boolean closedManually);

    public void onError(String errorMessage);

    public void onConnect();
}