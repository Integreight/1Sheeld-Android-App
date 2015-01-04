package com.integreight.onesheeld.shields.observer;

import com.integreight.firmatabluetooth.ArduinoFirmata;

public interface OneSheeldServiceHandler {
    public void onSuccess(ArduinoFirmata firmate);

    public void onFailure();
}
