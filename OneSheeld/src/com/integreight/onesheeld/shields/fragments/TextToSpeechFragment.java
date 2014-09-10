package com.integreight.onesheeld.shields.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield.TTsEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TextToSpeechFragment extends
		ShieldFragmentParent<TextToSpeechFragment> {

	ImageView speakerLevel;
	Button femaleBtn, maleBtn;
	OneSheeldTextView ttsText;
	// private float ttsPitch = 1;
	// private float freqValue = 1f;
	AnimationDrawable animation;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.tts_shield_fragment_layout, container,
				false);
		speakerLevel = (ImageView) v
				.findViewById(R.id.speaker_shield_imageview);
		femaleBtn = (Button) v.findViewById(R.id.increaseBtn);
		maleBtn = (Button) v.findViewById(R.id.decreaseBtn);
		ttsText = (OneSheeldTextView) v.findViewById(R.id.ttsText);
		ttsText.setMovementMethod(new ScrollingMovementMethod());
		animation = (AnimationDrawable) speakerLevel.getBackground();
		return v;
	}

	private Runnable runAnimation = new Runnable() {

		@Override
		public void run() {
			animation.start();
		}
	};

	@Override
	public void onStart() {
		uiHandler = new Handler();
		((TextToSpeechShield) getApplication().getRunningShields().get(
				getControllerTag())).setEventHandler(ttsEventHandler);
		// ttsPitch = ((TextToSpeechShield) getApplication().getRunningShields()
		// .get(getControllerTag())).getTtsPitch();
		// speakerLevel.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// ttsEventHandler
		// .onSpeek("Android Android Android Android Android Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android AndroidAndroid Android Android Android");
		// ((TextToSpeechShield) getApplication().getRunningShields().get(
		// getControllerTag()))
		// .speech("Android Android Android Android");
		// }
		// });
		// femaleBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if (ttsPitch < 3) {
		// ((TextToSpeechShield) getApplication().getRunningShields()
		// .get(getControllerTag())).setTtsPitch(ttsPitch
		// + freqValue);
		// ttsPitch = ((TextToSpeechShield) getApplication()
		// .getRunningShields().get(getControllerTag()))
		// .getTtsPitch();
		// speakerLevel
		// .setBackgroundResource(ttsPitch <= 0 ? R.drawable.tts_shield_0_volume
		// : ttsPitch == 1 ? R.drawable.tts_shield_1_volume
		// : ttsPitch == 2 ? R.drawable.tts_shield_2_volume
		// : R.drawable.tts_shield_3_volume);
		// }
		// }
		// });
		// maleBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if (ttsPitch >= 0) {
		// ((TextToSpeechShield) getApplication().getRunningShields()
		// .get(getControllerTag())).setTtsPitch(ttsPitch
		// - freqValue);
		// ttsPitch = ((TextToSpeechShield) getApplication()
		// .getRunningShields().get(getControllerTag()))
		// .getTtsPitch();
		// speakerLevel
		// .setBackgroundResource(ttsPitch <= 0 ? R.drawable.tts_shield_0_volume
		// : ttsPitch == 1 ? R.drawable.tts_shield_1_volume
		// : ttsPitch == 2 ? R.drawable.tts_shield_2_volume
		// : R.drawable.tts_shield_3_volume);
		// }
		// }
		// });
		super.onStart();
	}

	@Override
	public void onStop() {
		if (speakerLevel != null && animation != null) {
			speakerLevel.removeCallbacks(runAnimation);
			animation.stop();
		}
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	private TTsEventHandler ttsEventHandler = new TTsEventHandler() {

		@Override
		public void onSpeek(final String txt) {
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (canChangeUI()) {
						speakerLevel
								.setBackgroundResource(R.anim.tts_animation);
						ttsText.setText(txt);
						animation = (AnimationDrawable) speakerLevel
								.getBackground();
						speakerLevel.post(runAnimation);
					}
				}
			});
		}

		@Override
		public void onError(String error, int errorCode) {
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (canChangeUI()) {
						speakerLevel.removeCallbacks(runAnimation);
						animation.stop();
					}
				}
			});

		}

		@Override
		public void onStop() {
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (canChangeUI()) {
						speakerLevel.removeCallbacks(runAnimation);
						animation.stop();
						speakerLevel
								.setBackgroundResource(R.drawable.tts_shield_0_volume);
					}
				}
			});
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
					new TextToSpeechShield(activity, getControllerTag()));
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
