package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.os.Handler;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.Log;
import com.integreight.onesheeld.shields.controller.utils.MicSoundMeter;
import com.integreight.onesheeld.utils.ControllerParent;

public class MicShield extends ControllerParent<MicShield> {
	Handler handler;
	int PERIOD = 100;
	boolean isHandlerLive = false;

	private final Runnable processMic = new Runnable() {
		@Override
		public void run() {
			// Do work with the MIC values.
			double ampl = MicSoundMeter.getInstance().getAmplitudeEMA();
			if (!Double.isInfinite(ampl)) {
				// send Frame and update UI
				Log.d("MIC", "Amp = " + ampl);
			}
			// The Runnable is posted to run again here:
			handler.postDelayed(this, PERIOD);
		}
	};

	public MicShield() {
	}

	public MicShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<MicShield> setTag(String tag) {
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
		handler.post(processMic);
	}

	public void stopMic() {
		handler.removeCallbacks(processMic);
		handler.removeCallbacksAndMessages(null);
		MicSoundMeter.getInstance().stop();
	}

}
