package com.integreight.onesheeld.shields.controller;

import java.util.Calendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class ClockShield extends
		ControllerParent<ControllerParent<ClockShield>> {
	private static final byte CLOCK_COMMAND = (byte) 0x21;
	private ShieldFrame frame;
	private static final byte CLOCK_VALUE = (byte) 0x01;
	private Calendar calendar;
	private static int seconds, minutes, hour, day, month, year;
	boolean isFirstFrame = true;
	private ClockEventHandler eventHandler;
	private IntentFilter intentFilter;
	Handler handler;
	int PERIOD = 1000;

	private final Runnable updateClockSeconds = new Runnable() {
		Calendar calendar;

		@Override
		public void run() {
			// Do work
			frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);
			calendar = Calendar.getInstance();

			if (frame != null && calendar != null) {
				frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
				activity.getThisApplication().getAppFirmata()
						.sendShieldFrame(frame);
				if (eventHandler != null)
					eventHandler.onTimeChanged(""
							+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
							+ calendar.get(Calendar.MINUTE) + ":"
							+ calendar.get(Calendar.SECOND) + "");
			}
			calendar = null;
			frame = null;
			if (handler != null)
				handler.postDelayed(this, PERIOD);

		}
	};

	@Override
	public ControllerParent<ControllerParent<ClockShield>> setTag(String tag) {
		// TODO Auto-generated method stub
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		getActivity().registerReceiver(m_timeChangedReceiver, intentFilter);

		return super.setTag(tag);
	}

	public ClockShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public ClockShield() {
		super();
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame clock_frame) {

		if (clock_frame.getShieldId() == CLOCK_COMMAND) {
			frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);

			if (frame != null) {
				setTime();
				frame.addByteArgument((byte) seconds);
				frame.addByteArgument((byte) minutes);
				frame.addByteArgument((byte) hour);
				frame.addByteArgument((byte) day);
				frame.addByteArgument((byte) month);
				frame.addByteArgument((byte) year);
				activity.getThisApplication().getAppFirmata()
						.sendShieldFrame(frame);
				if (eventHandler != null)
					eventHandler.onTimeChanged(hour + ":" + minutes + ":"
							+ seconds);

				if (handler != null) {
					if(updateClockSeconds != null)
					handler.removeCallbacks(updateClockSeconds);
					handler.removeCallbacksAndMessages(null);
				} else {
					handler = new Handler();
					if (updateClockSeconds != null)
						handler.post(updateClockSeconds);
				}
			}
		}
	}

	public void setClockEventHandler(ClockEventHandler clockEventHandler) {
		this.eventHandler = clockEventHandler;
		CommitInstanceTotable();

	}

	public static interface ClockEventHandler {
		void onTimeChanged(String Time);
	}

	private void setTime() {
		calendar = Calendar.getInstance();
		seconds = calendar.get(Calendar.SECOND);
		minutes = calendar.get(Calendar.MINUTE);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
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
			getActivity().unregisterReceiver(m_timeChangedReceiver);
		if (calendar != null)
			calendar = null;
		if (frame != null)
			frame = null;

	}

	private BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// check on hour and day...
			frame = new ShieldFrame(UIShield.CLOCK_SHIELD.getId(), CLOCK_VALUE);
			calendar = Calendar.getInstance();

			Log.d("ClockShield:: old time", seconds + " " + minutes + " "
					+ hour + " " + day + " " + month + " " + year);
			Log.d("ClockShield:: new time",
					calendar.get(Calendar.SECOND) + " "
							+ calendar.get(Calendar.MINUTE) + " "
							+ calendar.get(Calendar.HOUR_OF_DAY) + " "
							+ calendar.get(Calendar.DAY_OF_MONTH) + " "
							+ calendar.get(Calendar.MONTH) + " "
							+ calendar.get(Calendar.YEAR));

			// compare between the old and new hour&&day and send new frame
			if (hour != calendar.get(Calendar.HOUR_OF_DAY)
					&& day != calendar.get(Calendar.DAY_OF_MONTH)
					&& month != calendar.get(Calendar.MONTH)
					&& year != calendar.get(Calendar.YEAR)) {
				// send frame year + month + day + hour + min + sec
				if (frame != null) {
					frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
					frame.addByteArgument((byte) calendar.get(Calendar.MINUTE));
					frame.addByteArgument((byte) calendar
							.get(Calendar.HOUR_OF_DAY));
					frame.addByteArgument((byte) calendar
							.get(Calendar.DAY_OF_MONTH));
					frame.addByteArgument((byte) calendar.get(Calendar.MONTH));
					frame.addByteArgument((byte) calendar.get(Calendar.YEAR));

					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);
				}
			} else if (hour != calendar.get(Calendar.HOUR_OF_DAY)
					&& day != calendar.get(Calendar.DAY_OF_MONTH)
					&& month != calendar.get(Calendar.MONTH)) {
				// send frame month + day + hour + min + sec
				if (frame != null) {
					frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
					frame.addByteArgument((byte) calendar.get(Calendar.MINUTE));
					frame.addByteArgument((byte) calendar
							.get(Calendar.HOUR_OF_DAY));
					frame.addByteArgument((byte) calendar
							.get(Calendar.DAY_OF_MONTH));
					frame.addByteArgument((byte) calendar.get(Calendar.MONTH));

					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);
				}

			} else if (hour != calendar.get(Calendar.HOUR_OF_DAY)
					&& day != calendar.get(Calendar.DAY_OF_MONTH)) {
				// send hour + day + min + secon
				if (frame != null) {
					frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
					frame.addByteArgument((byte) calendar.get(Calendar.MINUTE));
					frame.addByteArgument((byte) calendar
							.get(Calendar.HOUR_OF_DAY));
					frame.addByteArgument((byte) calendar
							.get(Calendar.DAY_OF_MONTH));

					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);

				}

			} else if (hour != calendar.get(Calendar.HOUR_OF_DAY)) {
				// send hour + min + sec
				if (frame != null) {
					frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
					frame.addByteArgument((byte) calendar.get(Calendar.MINUTE));
					frame.addByteArgument((byte) calendar
							.get(Calendar.HOUR_OF_DAY));

					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);

				}

			} else {

				if (frame != null) {
					frame.addByteArgument((byte) calendar.get(Calendar.SECOND));
					frame.addByteArgument((byte) calendar.get(Calendar.MINUTE));

					activity.getThisApplication().getAppFirmata()
							.sendShieldFrame(frame);
				}
			}
			setTime();
			if (eventHandler != null)
				eventHandler.onTimeChanged("" + hour + ":" + minutes + ":"
						+ seconds + "");

			calendar = null;
			frame = null;
			// set new time
		}
	};

}
