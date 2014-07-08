package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.SpeechRecognitionShield;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield.TTsEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class TextToSpeechFragment extends
		ShieldFragmentParent<TextToSpeechFragment> {

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
		((TextToSpeechShield) getApplication().getRunningShields().get(
				getControllerTag())).setEventHandler(ttsEventHandler);
		ledImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((TextToSpeechShield) getApplication().getRunningShields().get(
						getControllerTag())).speech("Android is is is is is is");
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

	private TTsEventHandler ttsEventHandler = new TTsEventHandler() {

		@Override
		public void onSpeek(String txt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(String error, int errorCode) {
			// TODO Auto-generated method stub

		}
	};

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
