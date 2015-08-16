package com.integreight.onesheeld.shields;

import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.firmatabluetooth.ArduinoFirmataShieldFrameHandler;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.TaskerShield;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.CrashlyticsUtils;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * @param <T> the Child class used for casting
 *            <p/>
 *            Is the parent class for all shields controllers
 * @author Ahmed Saad
 */
@SuppressWarnings("unchecked")
public abstract class ControllerParent<T extends ControllerParent<?>> {
    public MainActivity activity;
    private boolean hasConnectedPins = false; // flag for detecting the
    // connected pins to stop the
    // analog and digital effects
    private String tag = ""; // Shield identical key
    private boolean hasForgroundView = false;// flag to stop UI handler calls in
    // case of FALSE
    public String[][] requiredPinsNames = new String[][]{
            {"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
                    "A0", "A1", "A2", "A3", "A4", "A5"},
            {"3", "5", "6", "9", "10", "11"}}; // 2D array for pins types
    // (Digital and PWM)
    public Hashtable<String, ArduinoPin> matchedShieldPins = new Hashtable<String, ArduinoPin>(); // saving
    // connected
    // pins
    public int requiredPinsIndex = -1;// related to the pins type (index of the
    // requiredPinsNames array)
    private boolean isALive = false; // flag for fired shields (in case of
    // disconnection or disselect)
    public String[] shieldPins = new String[]{};// Specific shield pins names
    // (like "Toggle" pin with
    // ON/Off shield`)
    public SelectionAction selectionAction;// interface implemented in case of
    // shields that may be unavailable
    // like not
    // found sensors
    public boolean isInteractive = true;// flag used for the interaction
    // top-right toggle button

    private long selectionTime;

    private static final byte IS_SHIELD_SELECTED = (byte) 0xFF;
    private static final byte SELECT_SHIELD = (byte) 0xFE;
    private static final byte DESELECT_SHIELD = (byte) 0xFD;

    public boolean cachedArduinoCallbackStatus = false;

    private boolean isInit = false;

    public void notifyHardwareOfShieldSelection() {
        if (isItARealShield())
            activity.getThisApplication().getAppFirmata()
                    .sendShieldFrame(new ShieldFrame(getShieldId(), IS_SHIELD_SELECTED), true);
        isInit = true;
    }

    public ControllerParent() {
        // TODO Auto-generated constructor stub
        tag = AppShields.getInstance().getShieldTag(((T) (this)).getClass().getName());
    }

    /**
     * @param activity MainActivity instance
     * @param tag      Shield unique name gotten from UIShield ENUM
     */
    public ControllerParent(Activity activity, String tag) {
        setActivity((MainActivity) activity);
        init(tag);
    }

    /**
     * @param activity MainActivity instance
     * @param tag      Shield unique name gotten from UIShield ENUM
     */
    public ControllerParent(Activity activity, String tag, boolean manageShieldSelectionFrameManually) {
        setActivity((MainActivity) activity);
        init(tag, manageShieldSelectionFrameManually);
    }

    /**
     * used to set selected pin mode
     *
     * @param pins array of connected pins
     */
    public void setConnected(ArduinoConnectedPin... pins) {
        for (int i = 0; i < pins.length; i++) {
            activity.getThisApplication().getAppFirmata()
                    .pinMode(pins[i].getPinID(), pins[i].getPinMode());
        }
        this.setHasConnectedPins(true);

    }

    /**
     * @return Shield visibility status to allow UI interaction or not
     */
    public boolean isHasForgroundView() {
        return hasForgroundView;
    }

    /**
     * changing shield UI status
     *
     * @param hasForgroundView true -- onStart | false -- onStop
     */
    public void setHasForgroundView(boolean hasForgroundView) {
        this.hasForgroundView = hasForgroundView;
        if (hasForgroundView) {
            ((T) ControllerParent.this).refresh();
            if (getActivity() != null
                    && getActivity().findViewById(R.id.pinsFixedHandler) != null)
                getActivity().findViewById(R.id.pinsFixedHandler)
                        .setVisibility(
                                requiredPinsIndex != -1 ? View.VISIBLE
                                        : View.GONE);
        }

    }

    /**
     * @return activity instance
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * @param activity
     * @return instance of Shield ControllerParent for reflection usage
     */
    public ControllerParent<T> setActivity(MainActivity activity) {
        this.activity = activity;
        setFirmataEventHandler();
        return this;
    }

    public void updateActivty(MainActivity activity) {
        this.activity = activity;
    }

    public void refresh() {

    }

    public void onSysex(byte command, byte[] data) {
        // TODO Auto-generated method stub

    }

    public void onDigital(int portNumber, int portData) {

    }

    public void onAnalog(int pin, int value) {
        // TODO Auto-generated method stub

    }

    public abstract void onNewShieldFrameReceived(ShieldFrame frame);

    public Handler actionHandler = new Handler(); // queuing sysex UI calls
    public Handler onDigitalActionHandler = new Handler(); // Queuing digital UI
    // calls

    /**
     * add Shield handlers to firmata listening list
     */
    private void setFirmataEventHandler() {
        ((OneSheeldApplication) activity.getApplication()).getAppFirmata()
                .addDataHandler(arduinoFirmataDataHandler);
        ((OneSheeldApplication) activity.getApplication()).getAppFirmata()
                .addShieldFrameHandler(arduinoFirmataShieldFrameHandler);
    }

    // Interface implemented for listening to Arduino actions
    public ArduinoFirmataDataHandler arduinoFirmataDataHandler = new ArduinoFirmataDataHandler() {

        @Override
        public void onSysex(final byte command, final byte[] data) {
            actionHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (isALive && isInteractive)
                        ((T) ControllerParent.this).onSysex(command, data);
                }
            });
        }

        // UI runnable to be called onDigital actions
        Runnable onDigitalRunnable = new Runnable() {

            @Override
            public void run() {
                if (hasConnectedPins
                        || ((T) ControllerParent.this) instanceof TaskerShield)
                    ((T) ControllerParent.this).onDigital(portNumber, portData);
            }
        };
        private int portNumber,
                portData;

        @Override
        public void onDigital(final int portNumber, final int portData) {
            if (isALive && isInteractive) {
                onDigitalActionHandler.removeCallbacks(onDigitalRunnable);
                this.portData = portData;
                this.portNumber = portNumber;
                onDigitalActionHandler.post(onDigitalRunnable);
            }
        }

        @Override
        public void onAnalog(final int pin, final int value) {
            if (isALive && isInteractive)
                actionHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (hasConnectedPins)
                            ((T) ControllerParent.this).onAnalog(pin, value);
                    }
                });
        }
    };

    private byte getShieldId() {
        if (ControllerParent.this instanceof TaskerShield) return UIShield.TASKER_SHIELD.id;
        return AppShields.getInstance().getShield(tag).id;
    }

    // Interface implemented for listening to received Shields Frames
    public ArduinoFirmataShieldFrameHandler arduinoFirmataShieldFrameHandler = new ArduinoFirmataShieldFrameHandler() {

        @Override
        public void onNewShieldFrameReceived(final ShieldFrame frame) {
            if (isALive && frame != null && matchedShieldPins.size() == 0 && isInit)
                if (frame.getShieldId() == getShieldId())
                    if (frame.getFunctionId() == IS_SHIELD_SELECTED)
                        notifyHardwareOfShieldSelection();
                    else if (frame.getFunctionId() == SELECT_SHIELD) {
                    } else if (frame.getFunctionId() == DESELECT_SHIELD) {
                    } else {
                        cachedArduinoCallbackStatus = getApplication().getAppFirmata().getCallbackStatus();
                        actionHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (isInteractive)
                                        ((T) ControllerParent.this)
                                                .onNewShieldFrameReceived(frame);
                                    cachedArduinoCallbackStatus = false;
                                } catch (RuntimeException e) {
                                    cachedArduinoCallbackStatus = false;
                                    Toast.makeText(getActivity(), "Received an unexpected frame.", Toast.LENGTH_SHORT).show();
                                    CrashlyticsUtils.logException(e);
                                }
                            }
                        });
                    }
        }
    };

    public OneSheeldApplication getApplication() {
        return activity.getThisApplication();
    }

    public String getTag() {
        return tag;
    }

    /**
     * @param tag unique shield name
     * @return instance of shield controller for Java reflection usage
     * (initialization)
     */
    public ControllerParent<T> init(String tag) {
        return init(tag, false);
    }

    /**
     * @param tag unique shield name
     * @return instance of shield controller for Java reflection usage
     * (initialization)
     */
    public ControllerParent<T> init(String tag, boolean manageShieldSelectionFrameManually) {
        this.tag = tag;
        isALive = true;
        if (getApplication().getRunningShields().get(tag) == null)
            getApplication().getRunningShields().put(tag, this);
        selectionTime = SystemClock.elapsedRealtime();
        CrashlyticsUtils
                .setString(
                        "Number of running shields",
                        getApplication().getRunningShields() == null
                                || getApplication().getRunningShields().size() == 0 ? "No Running Shields"
                                : ""
                                + getApplication().getRunningShields()
                                .size());
        CrashlyticsUtils
                .setString(
                        "Running Shields",
                        getApplication().getRunningShields() != null
                                && getApplication().getRunningShields().size() > 0 ? getApplication()
                                .getRunningShields().keySet().toString()
                                : "No Running Shields");
        if (!manageShieldSelectionFrameManually) notifyHardwareOfShieldSelection();
        return this;
    }

    /**
     * @param selectionAction implemented interface to do on validation
     * @param isToastable     allow showing toasts or UI Message, related to selectAll or
     *                        select single shield
     * @return instance of shield controller for Java reflection usage
     * (initialization)
     */
    public ControllerParent<T> invalidate(SelectionAction selectionAction,
                                          boolean isToastable) {
        this.selectionAction = selectionAction;
        return this;
    }

    public boolean isHasConnectedPins() {
        return hasConnectedPins;
    }

    private boolean isItARealShield() {
        return !(ControllerParent.this instanceof TaskerShield);
    }

    public void setHasConnectedPins(boolean hasConnectedPins) {
        this.hasConnectedPins = hasConnectedPins;
    }

    /**
     * Called on shield firing process
     */
    public void resetThis() {
        if (activity != null) {
            if (activity.looperThread == null
                    || (!activity.looperThread.isAlive() || activity.looperThread
                    .isInterrupted()))
                activity.initLooperThread();
            isALive = false;
            activity.backgroundThreadHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    if (matchedShieldPins != null)
                        matchedShieldPins.clear();
                    if (shieldPins != null && shieldPins.length > 0) {
                        for (ArduinoPin pin : Arrays.asList(ArduinoPin.values())) {
                            for (int i = 0; i < shieldPins.length; i++) {
                                if (pin.connectedPins.size() == 0)
                                    break;
                                pin.connectedPins
                                        .remove(((T) ControllerParent.this)
                                                .getClass().getName()
                                                + shieldPins[i]);
                            }
                        }
                    }
                    ((T) ControllerParent.this).reset();
                    getApplication().getAppFirmata().removeDataHandler(
                            arduinoFirmataDataHandler);
                    getApplication().getAppFirmata().removeShieldFrameHandler(
                            arduinoFirmataShieldFrameHandler);
                }
            });
        }
        if (selectionTime != 0 && !(((T) ControllerParent.this) instanceof TaskerShield)) {
            getApplication().getTracker().send(new HitBuilders.TimingBuilder()
                    .setCategory("Shields Selection Timing")
                    .setValue(SystemClock.elapsedRealtime() - selectionTime)
                    .setVariable(tag)
                    .build());
            selectionTime = 0;
        }
        CrashlyticsUtils
                .setString(
                        "Number of running shields",
                        getApplication().getRunningShields() == null
                                || getApplication().getRunningShields().size() == 0 ? "No Running Shields"
                                : ""
                                + getApplication().getRunningShields()
                                .size());
        CrashlyticsUtils
                .setString(
                        "Running Shields",
                        getApplication().getRunningShields() != null
                                && getApplication().getRunningShields().size() > 0 ? getApplication()
                                .getRunningShields().keySet().toString()
                                : "No Running Shields");
        cachedArduinoCallbackStatus = false;
    }

    /**
     * @param frame target frame sending frame to Arduino Onesheeld
     */
    public void queueShieldFrame(ShieldFrame frame) {
        if (isInteractive)
            activity.getThisApplication().getAppFirmata()
                    .queueShieldFrame(frame);
    }

    /**
     * @param frame target frame sending frame to Arduino Onesheeld
     */
    public void sendShieldFrame(ShieldFrame frame) {
        if (isInteractive)
            activity.getThisApplication().getAppFirmata()
                    .sendShieldFrame(frame);
    }

    /**
     * @param frame target frame queuing frame to Arduino Onesheeld
     */
    public void sendShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        if (isInteractive) {
            activity.getThisApplication().getAppFirmata()
                    .sendShieldFrame(frame, waitIfInACallback);
        }
    }

    public void digitalWrite(int pin, boolean value) {
        if (isInteractive)
            activity.getThisApplication().getAppFirmata()
                    .digitalWrite(pin, value);
    }

    public void analogWrite(int pin, int value) {
        if (isInteractive)
            activity.getThisApplication().getAppFirmata()
                    .analogWrite(pin, value);
    }

    /**
     * abstract implemented in child class to be called on firing the Shield
     */
    public abstract void reset();

    public void preConfigChangeThis() {
        ((T) ControllerParent.this).preConfigChange();
    }

    public void postConfigChangeThis() {
        ((T) ControllerParent.this).postConfigChange();
    }

    public void preConfigChange() {
    }

    public void postConfigChange() {
        this.activity = (MainActivity) getActivity();
    }

    public String[] getRequiredPinsNames() {
        return requiredPinsNames[requiredPinsIndex];
    }

    /**
     * @author Ahmed Saad Interface used to Invalidate specific shield on select
     */
    public static interface SelectionAction {
        public void onSuccess();

        public void onFailure();
    }


}
