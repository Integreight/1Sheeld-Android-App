package com.integreight.onesheeld.shields.fragments;

import java.util.Hashtable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield.SevenSegmentsEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;

public class SevenSegmentFragment extends
		ShieldFragmentParent<SevenSegmentFragment> {

	Button connectButton;
	ImageView aSegment;
	ImageView bSegment;
	ImageView cSegment;
	ImageView dSegment;
	ImageView eSegment;
	ImageView fSegment;
	ImageView gSegment;
	ImageView dotSegment;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.sevensegment_shield_fragment_layout,
				container, false);
		aSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_a_segment_imageview);
		bSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_b_segment_imageview);
		cSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_c_segment_imageview);
		dSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_d_segment_imageview);
		eSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_e_segment_imageview);
		fSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_f_segment_imageview);
		gSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_g_segment_imageview);
		dotSegment = (ImageView) v
				.findViewById(R.id.sevensegment_shield_dot_segment_imageview);
		return v;

	}

	@Override
	public void onStart() {
		if (getApplication().getRunningShields().get(getControllerTag()) != null)
			refreshSegments(((SevenSegmentShield) getApplication()
					.getRunningShields().get(getControllerTag()))
					.refreshSegments());
		ConnectingPinsView.getInstance().reset(
				getApplication().getRunningShields().get(getControllerTag()),
				new OnPinSelectionListener() {

					@Override
					public void onSelect(ArduinoPin pin) {
						if (pin != null) {
							((SevenSegmentShield) getApplication()
									.getRunningShields()
									.get(getControllerTag()))
									.setConnected(new ArduinoConnectedPin(
											pin.microHardwarePin,
											ArduinoFirmata.INPUT));
						}

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
		((SevenSegmentShield) getApplication().getRunningShields().get(
				getControllerTag()))
				.setSevenSegmentsEventHandler(sevenSegmentsEventHandler);
		super.onActivityCreated(savedInstanceState);

	}

	private SevenSegmentsEventHandler sevenSegmentsEventHandler = new SevenSegmentsEventHandler() {

		@Override
		public void onSegmentsChange(Hashtable<String, Boolean> segmentsStatus) {
			// TODO Auto-generated method stub
			if (canChangeUI())
				refreshSegments(segmentsStatus);

		}
	};

	private void refreshSegments(Hashtable<String, Boolean> segmentsStatus) {
		if (segmentsStatus.get("  A  ")) {
			aSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			aSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}

		if (segmentsStatus.get("  B  ")) {
			bSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			bSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}

		if (segmentsStatus.get("  C  ")) {
			cSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			cSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}

		if (segmentsStatus.get("  D  ")) {
			dSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			dSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}

		if (segmentsStatus.get("  E  ")) {
			eSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			eSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}

		if (segmentsStatus.get("  F  ")) {
			fSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			fSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}

		if (segmentsStatus.get("  G  ")) {
			gSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			gSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}

		if (segmentsStatus.get(" DOT ")) {
			dotSegment.setImageResource(R.drawable.seventsegment_on_dot_image);
		} else {
			dotSegment.setImageResource(R.drawable.seventsegment_off_dot_image);
		}

	}

	private void initializeFirmata(ArduinoFirmata firmata) {
		if ((getApplication().getRunningShields().get(getControllerTag())) == null)
			getApplication().getRunningShields().put(getControllerTag(),
					new SevenSegmentShield(getActivity(), getControllerTag()));

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata(getApplication().getAppFirmata());
	}

}