package com.integreight.onesheeld.shields.controller;

import java.util.Calendar;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
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

	@Override
	public ControllerParent<ControllerParent<ClockShield>> setTag(String tag) {
		// TODO Auto-generated method stub

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

			if (isFirstFrame) {
				setTime();
				frame.addByteArgument((byte) seconds);
				frame.addByteArgument((byte) minutes);
				frame.addByteArgument((byte) hour);
				frame.addByteArgument((byte) day);
				frame.addByteArgument((byte) month);
				frame.addByteArgument((byte) year);
				activity.getThisApplication().getAppFirmata()
						.sendShieldFrame(frame);
				isFirstFrame = false;
				if (eventHandler != null)
					eventHandler.onTimeChanged(hour + ":" + minutes + ":"
							+ seconds);
			} else
				setTime();
			frame.addByteArgument((byte) seconds);
			activity.getThisApplication().getAppFirmata()
					.sendShieldFrame(frame);
			if (eventHandler != null)
				eventHandler.onTimeChanged("" + hour + ":" + minutes + ":"
						+ seconds + "");

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
		day = calendar.get(Calendar.DAY_OF_WEEK);
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
