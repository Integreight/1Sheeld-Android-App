package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.MicShield;
import com.integreight.onesheeld.shields.controller.MicShield.MicEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class MicFragment extends ShieldFragmentParent<MicFragment> {
	RelativeLayout.LayoutParams params;
	TextView soundLevelIndicator;
	int stepValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.mic_shield_fragment_view, container,
				false);
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
		soundLevelIndicator = (TextView) v
				.findViewById(R.id.soundLevelIndicator);
		params = (LayoutParams) soundLevelIndicator.getLayoutParams();
		soundLevelIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						stepValue = soundLevelIndicator.getHeight() / 80;
					}
				});
		// params.gravity = Gravity.BOTTOM;
		// v.findViewById(R.id.start_mic).setOnClickListener(
		// new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// invalidateController();
		// ((MicShield) getApplication().getRunningShields().get(
		// getControllerTag())).startMic(false);
		//
		// }
		// });
		// v.findViewById(R.id.stop_mic).setOnClickListener(
		// new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// invalidateController();
		// ((MicShield) getApplication().getRunningShields().get(
		// getControllerTag())).stopMic();
		//
		// }
		// });

	}

	private MicEventHandler micEventHandler = new MicEventHandler() {

		@Override
		public void getAmplitude(final Double value) {

			// set data to UI
			uiHandler.removeCallbacksAndMessages(null);
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					if (canChangeUI()) {
						params.bottomMargin = (int) (value * stepValue);
						if (soundLevelIndicator != null)
							soundLevelIndicator.requestLayout();
						if (v != null && v.findViewById(R.id.micValue) != null)
							((OneSheeldTextView) v.findViewById(R.id.micValue))
									.setText(String.valueOf(value).substring(0,
											4)
											+ " db");
					}
				}
			});
		}
	};

	private void invalidateController() {
		if (getApplication().getRunningShields().get(getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new MicShield(activity, getControllerTag()));
		}

	}

	public void doOnServiceConnected() {
		invalidateController();
	}

	@Override
	public void onResume() {
		invalidateController();
		((MicShield) getApplication().getRunningShields().get(
				getControllerTag())).setMicEventHandler(micEventHandler);
		((MicShield) getApplication().getRunningShields().get(
				getControllerTag())).startMic(false);
		((MicShield) getApplication().getRunningShields().get(
				getControllerTag())).doOnResume();

		super.onResume();
	}
}
