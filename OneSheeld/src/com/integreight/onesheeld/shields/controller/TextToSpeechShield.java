package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class TextToSpeechShield extends ControllerParent<TextToSpeechShield>
		implements TextToSpeech.OnInitListener {
	private TTsEventHandler eventHandler;
	// private UtteranceProgressListener uteranceListener;
	// private int TTS_DATA_CHECK_CODE = 0;
	// private int RESULT_TALK_CODE = 1;
	private TextToSpeech myTTS;
	private float ttsPitch = 1.0f;

	public TextToSpeechShield() {
		super();
	}

	public TextToSpeechShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public ControllerParent<TextToSpeechShield> setTag(String tag) {
		myTTS = new TextToSpeech(activity, this);
		return super.setTag(tag);
	}

	public void setEventHandler(final TTsEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.TEXT_TO_SPEECH_SHIELD.getId()) {
			if (frame.getFunctionId() == 0x01) {
				speech(frame.getArgumentAsString(0));
				if (eventHandler != null) {
					eventHandler.onSpeek("Heyyyyyyyyy!");
				}
			}
		}
	}

	@Override
	public void reset() {
		if (myTTS != null) {
			myTTS.stop();
			myTTS.shutdown();
			myTTS = null;
		}
	}

	public interface TTsEventHandler {
		public void onSpeek(String txt);

		public void onError(String error, int errorCode);
	}

	@SuppressWarnings("unused")
	private void talkToText() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Extra options
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
		// intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
		// 2000000);
		// intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
		// 2000000);
		// intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
		// 20000000);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text example"); // Text
																					// message
																					// in
																					// the
																					// recognition
																					// dialog

		try {
			// startActivityForResult(intent, RESULT_TALK_CODE);

		} catch (ActivityNotFoundException a) {
			Toast t = Toast.makeText(activity.getApplicationContext(),
					"Opps! Your device doesn't support Speech to Text",
					Toast.LENGTH_SHORT);
			t.show();
		}
	}

	public void speech(String speechText) {
		if (myTTS != null) {
			myTTS.speak(speechText, TextToSpeech.QUEUE_FLUSH, //
					null);
		}

	}

	@Override
	public void onInit(int status) {

		switch (status) {
		case TextToSpeech.SUCCESS:
			configureTTSLocale();
			break;

		case TextToSpeech.ERROR:
			Toast.makeText(activity, "TTS Failed :(", Toast.LENGTH_SHORT)
					.show();
			Log.e("[ERROR] doc.saulmm.text2speech.MainActivity.onInit ",
					"TTS Failed");
			break;
		}
	}

	private void configureTTSLocale() {
		/*
		 * Locale deviceLocale = Locale.getDefault();
		 * 
		 * if(myTTS.isLanguageAvailable(deviceLocale) ==
		 * TextToSpeech.LANG_AVAILABLE) myTTS.setLanguage(deviceLocale);
		 */
	}

	public float getTtsPitch() {
		return ttsPitch;
	}

	public void setTtsPitch(float ttsPitch) {
		if (myTTS.setPitch(ttsPitch) == TextToSpeech.SUCCESS)
			this.ttsPitch = ttsPitch;
	}
}
