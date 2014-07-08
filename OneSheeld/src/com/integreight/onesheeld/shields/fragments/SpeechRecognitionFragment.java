package com.integreight.onesheeld.shields.fragments;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SpeechRecognitionShield;
import com.integreight.onesheeld.shields.controller.utils.SpeechRecognition.RecognitionEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class SpeechRecognitionFragment extends
		ShieldFragmentParent<SpeechRecognitionFragment> {

	ImageView ledImage;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.led_shield_fragment_layout,
				container, false);
		ledImage = (ImageView) v.findViewById(R.id.led_shield_led_imageview);
		return v;

	}

	@Override
	public void onStart() {
		((SpeechRecognitionShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setEventHandler(speechRecognitionEventHandler);
		ledImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((SpeechRecognitionShield) getApplication().getRunningShields()
						.get(getControllerTag())).startRecognizer();
			}
		});
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	private RecognitionEventHandler speechRecognitionEventHandler = new RecognitionEventHandler() {

		@Override
		public void onResult(final List<String> result) {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						if (result.size() > 0)
							Toast.makeText(activity, result.get(0),
									Toast.LENGTH_LONG).show();
					}
				});
			}
		}

		@Override
		public void onReadyForSpeach(Bundle params) {
			if (canChangeUI()) {
				toggleLed(true);
			}
		}

		@Override
		public void onError(final String error, int errorCode) {
			if (canChangeUI()) {
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(activity, error, Toast.LENGTH_LONG)
								.show();
					}
				});
			}
		}

		@Override
		public void onEndOfSpeech() {
			if (canChangeUI()) {
				toggleLed(false);
			}
		}

		@Override
		public void onBeginingOfSpeech() {
			if (canChangeUI()) {
				toggleLed(true);
			}
		}
	};

	private void toggleLed(final boolean isOn) {
		uiHandler.removeCallbacksAndMessages(null);
		uiHandler.post(new Runnable() {

			@Override
			public void run() {
				if (isOn) {
					ledImage.setImageResource(R.drawable.led_shield_led_on);
				} else {
					ledImage.setImageResource(R.drawable.led_shield_led_off);
				}
			}
		});
	}

	// private void intializeFirmata(ArduinoFirmata firmata, int connectedPin){
	//
	// led=new Led(firmata,connectedPin);
	// led.setLedEventHandler(ledEventHandler);
	// toggleLed(led.isLedOn());
	// }

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new SpeechRecognitionShield(activity, getControllerTag()));
		}

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();

	}

}
