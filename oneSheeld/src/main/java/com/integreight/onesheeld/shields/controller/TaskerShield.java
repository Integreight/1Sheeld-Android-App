package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Intent;

import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.plugin.condition.ConditionActivity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class TaskerShield extends ControllerParent<TaskerShield> {

    protected static final Intent INTENT_REQUEST_REQUERY = new Intent(
            com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY).putExtra(
            com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
            ConditionActivity.class.getName()).putExtra("Key", "Value");

    public TaskerShield() {
        super();
    }

    public TaskerShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public void onDigital(int portNumber, boolean portData) {
        boolean hasChanges = false;
        if (getApplication().isConnectedToBluetooth())
            for (ArduinoPin pin : ArduinoPin.values()) {
                hasChanges = getApplication().taskerPinsStatus.get(pin.microHardwarePin) != getApplication().getConnectedDevice().digitalRead(pin.microHardwarePin);
                if (hasChanges)
                    break;
            }
        if (hasChanges)
            activity.sendBroadcast(INTENT_REQUEST_REQUERY);
        super.onDigital(portNumber, portData);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        Log.sysOut("Reset");
    }
}
