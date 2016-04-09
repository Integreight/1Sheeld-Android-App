package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

import java.util.Calendar;

public class ClockShield extends
        ControllerParent<ControllerParent<ClockShield>> {
    private static final byte CLOCK_COMMAND = (byte) 0x21;
    private static final byte CLOCK_VALUE = (byte) 0x01;
    private static final byte BEGIN_METHOD = (byte) 0x01;
    private Calendar calendar;
    private static int seconds, minutes, hour, day, month, year;
    private ClockEventHandler eventHandler;
    private IntentFilter intentFilter;
    Handler handler;
    int PERIOD = 1000;
    boolean isClockBegin = false;

    private final Runnable updateClockSeconds = new Runnable() {
        Calendar calendar;

        @Override
        public void run() {
            // Do work
            ShieldFrame frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);
            calendar = Calendar.getInstance();

            if (frame != null && calendar != null) {
                if (isClockBegin) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    sendShieldFrame(frame);
                }
                String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
                String min = calendar.get(Calendar.MINUTE) + "";
                String sec = calendar.get(Calendar.SECOND) + "";
                if (eventHandler != null)
                    eventHandler.onTimeChanged(""
                                    + (hour.length() == 1 ? "0" + hour : hour) + ":"
                                    + (min.length() == 1 ? "0" + min : min) + ":"
                                    + (sec.length() == 1 ? "0" + sec : sec) + "",
                            calendar.get(Calendar.AM_PM) == Calendar.AM);
            }
            if (handler != null)
                handler.postDelayed(this, PERIOD);

        }
    };

    @Override
    public ControllerParent<ControllerParent<ClockShield>> init(String tag) {
        // TODO Auto-generated method stub
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        getApplication().registerReceiver(m_timeChangedReceiver, intentFilter);

        isClockBegin = true;
        ShieldFrame frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);

        if (frame != null) {
            setTime();
            frame.addArgument((byte) seconds);
            frame.addArgument((byte) minutes);
            frame.addArgument((byte) hour);
            frame.addArgument((byte) day);
            frame.addArgument((byte) month);
            frame.addArgument(2, Math.round(year));

            // frame.addArgument((byte) year);
            sendShieldFrame(frame);
            String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
            String min = calendar.get(Calendar.MINUTE) + "";
            String sec = calendar.get(Calendar.SECOND) + "";
            if (eventHandler != null)
                eventHandler.onTimeChanged(""
                                + (hour.length() == 1 ? "0" + hour : hour) + ":"
                                + (min.length() == 1 ? "0" + min : min) + ":"
                                + (sec.length() == 1 ? "0" + sec : sec) + "",
                        calendar.get(Calendar.AM_PM) == Calendar.AM);

            handler = new Handler();
            if (updateClockSeconds != null)
                handler.post(updateClockSeconds);

        }

        //handler = new Handler();
        //if (updateClockSeconds != null)
        //    handler.post(updateClockSeconds);

        return super.init(tag);
    }

    public ClockShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public ClockShield() {
        super();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame clock_frame) {

        if (clock_frame.getShieldId() == CLOCK_COMMAND&&clock_frame.getFunctionId()==BEGIN_METHOD) {
            isClockBegin = true;
            ShieldFrame frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);

            if (frame != null) {
                setTime();
                frame.addArgument((byte) seconds);
                frame.addArgument((byte) minutes);
                frame.addArgument((byte) hour);
                frame.addArgument((byte) day);
                frame.addArgument((byte) month);
                frame.addArgument(2, Math.round(year));

                // frame.addArgument((byte) year);
                sendShieldFrame(frame);
                String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
                String min = calendar.get(Calendar.MINUTE) + "";
                String sec = calendar.get(Calendar.SECOND) + "";
                if (eventHandler != null)
                    eventHandler.onTimeChanged(""
                                    + (hour.length() == 1 ? "0" + hour : hour) + ":"
                                    + (min.length() == 1 ? "0" + min : min) + ":"
                                    + (sec.length() == 1 ? "0" + sec : sec) + "",
                            calendar.get(Calendar.AM_PM) == Calendar.AM);

                handler = new Handler();
                if (updateClockSeconds != null)
                    handler.post(updateClockSeconds);

            }
        }
    }

    public void setClockEventHandler(ClockEventHandler clockEventHandler) {
        this.eventHandler = clockEventHandler;

    }

    public static interface ClockEventHandler {
        void onTimeChanged(String Time, boolean isAM);
    }

    private void setTime() {
        calendar = Calendar.getInstance();
        seconds = calendar.get(Calendar.SECOND);
        minutes = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = (calendar.get(Calendar.MONTH) + 1);
        year = calendar.get(Calendar.YEAR);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        if (handler != null) {
            if (updateClockSeconds != null)
                handler.removeCallbacks(updateClockSeconds);
            handler.removeCallbacksAndMessages(null);
        }
        if (m_timeChangedReceiver != null)
            getApplication().unregisterReceiver(m_timeChangedReceiver);
        if (calendar != null)
            calendar = null;

    }

    @Override
    public void preConfigChange() {
//        if (m_timeChangedReceiver != null)
//            getActivity().unregisterReceiver(m_timeChangedReceiver);
        super.preConfigChange();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
//        intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_TIME_TICK);
//        getActivity().registerReceiver(m_timeChangedReceiver, intentFilter);
    }

    private BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // check on hour and day...
            ShieldFrame frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);
            calendar = Calendar.getInstance();

            Log.d("ClockShield:: old time", seconds + " " + minutes + " "
                    + hour + " " + day + " " + month + " " + year);
            Log.d("ClockShield:: new time",
                    calendar.get(Calendar.SECOND) + " "
                            + calendar.get(Calendar.MINUTE) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + " "
                            + calendar.get(Calendar.DAY_OF_MONTH) + " "
                            + (calendar.get(Calendar.MONTH) + 1) + " "
                            + calendar.get(Calendar.YEAR));

            // compare between the old and new hour&&day and send new frame
            if (hour != calendar.get(Calendar.HOUR_OF_DAY)
                    && day != calendar.get(Calendar.DAY_OF_MONTH)
                    && month != (calendar.get(Calendar.MONTH) + 1)
                    && year != calendar.get(Calendar.YEAR)) {
                // send frame year + month + day + hour + min + sec
                if (frame != null) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    frame.addArgument((byte) calendar.get(Calendar.MINUTE));
                    frame.addArgument((byte) calendar
                            .get(Calendar.HOUR_OF_DAY));
                    frame.addArgument((byte) calendar
                            .get(Calendar.DAY_OF_MONTH));
                    frame.addArgument((byte) (calendar.get(Calendar.MONTH) + 1));
                    frame.addArgument((byte) calendar.get(Calendar.YEAR));
                    if (isClockBegin)
                        sendShieldFrame(frame);
                }
            } else if (hour != calendar.get(Calendar.HOUR_OF_DAY)
                    && day != calendar.get(Calendar.DAY_OF_MONTH)
                    && month != (calendar.get(Calendar.MONTH) + 1)) {
                // send frame month + day + hour + min + sec
                if (frame != null) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    frame.addArgument((byte) calendar.get(Calendar.MINUTE));
                    frame.addArgument((byte) calendar
                            .get(Calendar.HOUR_OF_DAY));
                    frame.addArgument((byte) calendar
                            .get(Calendar.DAY_OF_MONTH));
                    frame.addArgument((byte) (calendar.get(Calendar.MONTH) + 1));
                    if (isClockBegin)
                        sendShieldFrame(frame);
                }

            } else if (hour != calendar.get(Calendar.HOUR_OF_DAY)
                    && day != calendar.get(Calendar.DAY_OF_MONTH)) {
                // send hour + day + min + secon
                if (frame != null) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    frame.addArgument((byte) calendar.get(Calendar.MINUTE));
                    frame.addArgument((byte) calendar
                            .get(Calendar.HOUR_OF_DAY));
                    frame.addArgument((byte) calendar
                            .get(Calendar.DAY_OF_MONTH));
                    sendShieldFrame(frame);

                }

            } else if (hour != calendar.get(Calendar.HOUR_OF_DAY)) {
                // send hour + min + sec
                if (frame != null) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    frame.addArgument((byte) calendar.get(Calendar.MINUTE));
                    frame.addArgument((byte) calendar
                            .get(Calendar.HOUR_OF_DAY));
                    if (isClockBegin)
                        sendShieldFrame(frame);

                }

            } else {

                if (frame != null) {
                    frame.addArgument((byte) calendar.get(Calendar.SECOND));
                    frame.addArgument((byte) calendar.get(Calendar.MINUTE));
                    if (isClockBegin)
                        sendShieldFrame(frame);
                }
            }
            setTime();
            String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
            String min = calendar.get(Calendar.MINUTE) + "";
            String sec = calendar.get(Calendar.SECOND) + "";
            if (eventHandler != null)
                eventHandler.onTimeChanged(
                        "" + (hour.length() == 1 ? "0" + hour : hour) + ":"
                                + (min.length() == 1 ? "0" + min : min) + ":"
                                + (sec.length() == 1 ? "0" + sec : sec) + "",
                        calendar.get(Calendar.AM_PM) == Calendar.AM);

            calendar = null;
            frame = null;
            // set new time
        }
    };

}
