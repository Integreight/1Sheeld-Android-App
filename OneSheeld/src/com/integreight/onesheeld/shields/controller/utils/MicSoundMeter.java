package com.integreight.onesheeld.shields.controller.utils;

import java.io.IOException;

import android.media.MediaRecorder;

public class MicSoundMeter {
	// static final private double EMA_FILTER = 0.6;
	static final private double POWER_REFERENCE = 0.00002;

	private MediaRecorder mRecorder = null;
	private double mEMA = 0.0;
	private static MicSoundMeter thisInstance;

	private MicSoundMeter() {
		// TODO Auto-generated constructor stub
	}

	public static MicSoundMeter getInstance() {
		if (thisInstance == null)
			thisInstance = new MicSoundMeter();
		return thisInstance;
	}

	public void start() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");
			try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mRecorder.start();
			mEMA = 0.0;
		}
	}

	public void stop() {
		if (mRecorder != null) {
			try {
				mRecorder.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mRecorder.release();
			mRecorder = null;
		}
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude());
		else
			return 0;

	}

	// db= 20* log10(amplitude/baseline_amplitude);
	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		// mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;

		mEMA = (20.0 * Math.log10(amp / POWER_REFERENCE));
		return (mEMA - 100.0);
	}

}
