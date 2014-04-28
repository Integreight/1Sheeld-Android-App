package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.MicShield;
import com.integreight.onesheeld.shields.controller.MicShield.MicEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class MicFragment extends ShieldFragmentParent<MicFragment> {
	TextView amplitude_value;
	ImageView myBox;
	int leftMargin;
	int topMargin;
	RelativeLayout.LayoutParams params;
	TranslateAnimation tAnimation;
	LinearLayout micLayout;
	RelativeLayout backLayout;
	int boxheigh;
	float scale;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.mic_shield_fragment_layout,
				container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		scale = getResources().getDisplayMetrics().density;
		amplitude_value = (TextView) getView().findViewById(R.id.mytext);
		myBox = (ImageView) getView().findViewById(R.id.mybox);
		params = (LayoutParams) myBox.getLayoutParams();
		micLayout = (LinearLayout) getView().findViewById(R.id.micLayout);
		boxheigh = myBox.getHeight();
		backLayout = (RelativeLayout) getView().findViewById(R.id.backLayout);
		micLayout.bringToFront();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		// params.gravity = Gravity.BOTTOM;
		getView().findViewById(R.id.start_mic).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						((MicShield) getApplication().getRunningShields().get(
								getControllerTag())).startMic();

					}
				});
		getView().findViewById(R.id.stop_mic).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						((MicShield) getApplication().getRunningShields().get(
								getControllerTag())).stopMic();

					}
				});

	}

	private MicEventHandler micEventHandler = new MicEventHandler() {

		@Override
		public void getAmplitude(final Double value) {

			if (canChangeUI()) {
				// set data to UI
				uiHandler.removeCallbacksAndMessages(null);
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						amplitude_value.setText(value + "");
						params.height = (int) (((value) * 4) * scale + .5f);
						params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
						myBox.requestLayout();
					}
				});
			}
		}
	};

	private void initializeFirmata() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new MicShield(getActivity(), getControllerTag()));
		}

	}

	public void doOnServiceConnected() {
		initializeFirmata();
	};

	@Override
	public void onResume() {
		((MicShield) getApplication().getRunningShields().get(
				getControllerTag())).setMicEventHandler(micEventHandler);
		((MicShield) getApplication().getRunningShields().get(
				getControllerTag())).doOnResume();
		super.onResume();
	}

	public void startAnimation(int y) {
		tAnimation = new TranslateAnimation(0, 0, boxheigh, -y);
		tAnimation.setDuration(5000);
		tAnimation.setRepeatCount(0);
		tAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		tAnimation.setFillAfter(true);
		myBox.startAnimation(tAnimation);
	}
}
