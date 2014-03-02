package com.integreight.onesheeld.utils.customviews;

import java.util.ArrayList;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;
import com.integreight.onesheeld.utils.OneShieldButton;
import com.integreight.onesheeld.utils.OneShieldTextView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectingPinsView extends Fragment {
	private static ConnectingPinsView thisInstance;
	TextView show;
	final String[] pins = new String[] { "Pin1", "Pin2", "Pin3", "Pin4",
			"Pin5", "Pin6", "Pin7", "Pin8", "Pin9" };
	String[] selectedPins = null;
	int selectedPin = -1;
	ArrayList<LinearLayout> pinsSubContainers = new ArrayList<LinearLayout>();

	public static ConnectingPinsView getInstance() {
		if (thisInstance == null) {
			thisInstance = new ConnectingPinsView();
		}
		return thisInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.connecting_pins_layout, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		show = (TextView) getView().findViewById(R.id.show);
		((PinsColumnContainer) getView().findViewById(R.id.cont)).setup(
				new OnChildFocusListener() {

					@Override
					public void focusOnThisChild(int childIndex, String tag) {
						show.setText(tag);
					}

					@Override
					public void selectThisChild(int childIndex, String tag) {
						show.setText(tag);
						((OneShieldTextView) pinsSubContainers.get(selectedPin)
								.getChildAt(1)).setText(tag);
					}
				}, (ImageView) getView().findViewById(R.id.cursor));
		selectedPins = new String[pins.length];
		LinearLayout pinsContainer = (LinearLayout) getView().findViewById(
				R.id.requiredPinsContainer);
		pinsContainer.removeAllViews();
		// int pdng = (int) (2 * getResources().getDisplayMetrics().density -
		// .5f);
		HorizontalScrollView scrollingPins = (HorizontalScrollView) getView()
				.findViewById(R.id.scrollingPins);
		for (int i = 0; i < pins.length; i++) {
			LinearLayout pinSubContainer = (LinearLayout) getActivity()
					.getLayoutInflater().inflate(R.layout.pin_sub_container,
							null, false);
			OneShieldButton pinButton = (OneShieldButton) pinSubContainer
					.getChildAt(0);
			pinButton.setText(pins[i]);
			OneShieldTextView pinText = (OneShieldTextView) pinSubContainer
					.getChildAt(1);
			pinText.setText(selectedPins[i]);
			final int x = i;
			pinButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (selectedPin != -1)
						pinsSubContainers.get(selectedPin).getChildAt(0)
								.setBackgroundColor(0x00000000);
					pinsSubContainers.get(x).getChildAt(0)
							.setBackgroundColor(0x99ffffff);
					selectedPin = x;
				}
			});
			pinsContainer.addView(pinSubContainer);
			pinsSubContainers.add(pinSubContainer);
			scrollingPins.invalidate();
		}
		pinsSubContainers.get(0).getChildAt(0).setBackgroundColor(0x99ffffff);
		selectedPin = 0;
		super.onActivityCreated(savedInstanceState);
	}
}
