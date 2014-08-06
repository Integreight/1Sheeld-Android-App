package com.integreight.onesheeld.shields.controller;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.SpeechRecognizer;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognition.RecognitionEventHandler;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognitionService;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class SpeechRecognitionShield extends
		ControllerParent<SpeechRecognitionShield> {
	private SpeechRecognitionService mSpeechRecognitionService;
	private RecognitionEventHandler eventHandler;
	private static final byte SEND_RESULT = 0x01;

	public SpeechRecognitionShield() {
		super();
	}

	public SpeechRecognitionShield(Activity activity, String tag) {
		super(activity, tag);
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mSpeechRecognitionService = ((SpeechRecognitionService.LocalBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSpeechRecognitionService = null;
		}

	};

	@Override
	public ControllerParent<SpeechRecognitionShield> setTag(String tag) {
		activity.bindService(new Intent(activity,
				SpeechRecognitionService.class), mServiceConnection,
				Context.BIND_AUTO_CREATE);
		return super.setTag(tag);
	}

	public void setEventHandler(final RecognitionEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	private ShieldFrame sf;
	RecognitionEventHandler controllerHandler = new RecognitionEventHandler() {

		@Override
		public void onResult(List<String> result) {
			if (result.size() > 0) {
				if (eventHandler != null)
					eventHandler.onResult(result);
				sf = new ShieldFrame(UIShield.SPEECH_RECOGNIZER_SHIELD.getId(),
						SEND_RESULT);
				String recognized = result.get(0);
				sf.addStringArgument(recognized);
				Log.d("Frame", sf.toString());
				sendShieldFrame(sf);
			} else {
				onError("No Matching result", SpeechRecognizer.ERROR_NO_MATCH);
			}
		}

		@Override
		public void onReadyForSpeach(Bundle params) {
			if (eventHandler != null)
				eventHandler.onReadyForSpeach(params);
		}

		@Override
		public void onError(String error, int errorCode) {
			if (eventHandler != null)
				eventHandler.onError(error, errorCode);
		}

		@Override
		public void onEndOfSpeech() {
			if (eventHandler != null)
				eventHandler.onEndOfSpeech();
		}

		@Override
		public void onBeginingOfSpeech() {
			eventHandler.onBeginingOfSpeech();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			eventHandler.onRmsChanged(rmsdB);
			Log.d("RMS", rmsdB + "");
		}
	};

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.SPEECH_RECOGNIZER_SHIELD.getId()) {
			if (frame.getFunctionId() == 0x01) {
				startRecognizer();
			}
		}
	}

	public void startRecognizer() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mSpeechRecognitionService != null)
					mSpeechRecognitionService
							.startRecognition(controllerHandler);
			}
		});
	}

	@Override
	public void reset() {
		activity.unbindService(mServiceConnection);
	}
}
