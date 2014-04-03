package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.os.Handler;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.MicSoundMeter;
import com.integreight.onesheeld.utils.ControllerParent;

public class MicShield extends ControllerParent<MicShield> {
	Handler handler;
	int PERIOD = 100;
	boolean isHandlerLive = false;
	private MicEventHandler eventHandler;
	private double ampl;
	boolean isResumed = false;
	private ShieldFrame frame;
	public static final byte MIC_VALUE = 0x01;
	// private int counter = 0;

	private final Runnable processMic = new Runnable() {
		@Override
		public void run() {
			// Do work with the MIC values.
			double amplitude = MicSoundMeter.getInstance().getAmplitudeEMA();
			if (!Double.isInfinite(amplitude)) {
				// send Frame and update UI
				// counter++;
				ampl = amplitude;
				Log.d("MIC", "Amp = " + ampl);
				frame = new ShieldFrame(UIShield.MIC_SHIELD.getId(), MIC_VALUE);
				frame.addByteArgument((byte) Math.round(ampl));
				activity.getThisApplication().getAppFirmata()
						.sendShieldFrame(frame);

				// if (counter == 5) {
				if (isResumed)
					if (eventHandler != null)
						eventHandler.getAmplitude(ampl);
				// counter = 0;
				// }

			}
			// The Runnable is posted to run again here:
			if (handler != null)
				handler.postDelayed(this, PERIOD);
		}
	};

	public MicShield() {
	}

	public void doOnResume() {
		isResumed = true;
	}

	public MicShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<MicShield> setTag(String tag) {
		startMic();
		return super.setTag(tag);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		stopMic();
	}

	public void startMic() {
		MicSoundMeter.getInstance().start();
		handler = new Handler();
		if (processMic != null)
			handler.post(processMic);
	}

	public void stopMic() {
		if (handler != null) {
			if (processMic != null)
				handler.removeCallbacks(processMic);
			handler.removeCallbacksAndMessages(null);
		}
		MicSoundMeter.getInstance().stop();
		frame = null;
	}

	public static interface MicEventHandler {
		void getAmplitude(Double value);
	}

	public void setMicEventHandler(MicEventHandler micEventHandler) {
		this.eventHandler = micEventHandler;
		CommitInstanceTotable();

	}

}
