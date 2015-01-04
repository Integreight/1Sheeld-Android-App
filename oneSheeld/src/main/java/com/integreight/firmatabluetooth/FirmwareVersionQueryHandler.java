package com.integreight.firmatabluetooth;

public interface FirmwareVersionQueryHandler {
    public void onVersionReceived(int minorVersion, int majorVersion);
}