package com.integreight.onesheeld.utils.customviews;

import java.util.ArrayList;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;
import com.integreight.onesheeld.utils.ControllerParent;
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
	private int selectedConnectingPin = 0;
	private ArrayList<LinearLayout> pinsSubContainers = new ArrayList<LinearLayout>();
	private View view;

	public static ConnectingPinsView getInstance() {
		if (thisInstance == null) {
			thisInstance = new ConnectingPinsView();
		}
		return thisInstance;
	}

	public void recycle() {
		thisInstance = null;
	}

	boolean isInflated = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.connecting_pins_layout, container,
					false);
			isInflated = true;
		} else
			isInflated = false;
		if (((ViewGroup) view.getParent()) != null)
			((ViewGroup) view.getParent()).removeAllViews();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (isInflated) {
			show = (TextView) getView().findViewById(R.id.show);
		}
		super.onActivityCreated(savedInstanceState);
	}

	public void reset(final ControllerParent<?> controller,
			final OnPinSelectionListener listner) {
		selectedConnectingPin = 0;
		final int selectedColor = getResources().getColor(
				R.color.arduinoPinsSelector);
		final int unSelectedColor = 0x00000000;
		((ImageView) getView().findViewById(R.id.cursor))
				.setVisibility(View.INVISIBLE);
		show.setText("");
		show.setVisibility(View.INVISIBLE);
		final PinsColumnContainer thisPinsContainer = ((PinsColumnContainer) getView()
				.findViewById(R.id.cont));
		pinsSubContainers = new ArrayList<LinearLayout>();
		final LinearLayout pinsContainer = (LinearLayout) getView()
				.findViewById(R.id.requiredPinsContainer);
		pinsContainer.removeAllViews();
		thisPinsContainer.setup(
				new OnChildFocusListener() {

					@Override
					public void focusOnThisChild(int childIndex, String tag) {
						show.setVisibility(tag.length() > 0 ? View.VISIBLE
								: View.INVISIBLE);
						show.setText(tag.startsWith("_") ? tag.substring(1)
								: tag);
					}

					@Override
					public void selectThisChild(int childIndex, String tag) {
						show.setVisibility(tag.length() > 0 ? View.VISIBLE
								: View.INVISIBLE);
						show.setText(tag.startsWith("_") ? tag.substring(1)
								: tag);
						String shieldPinName = controller.shieldPins[selectedConnectingPin];
						if (childIndex != -1) {
							ArduinoPin arduinoPin = ArduinoPin.valueOf(tag);
							ArduinoPin prevArduinoPin = controller.matchedShieldPins
									.get(shieldPinName);
							if (prevArduinoPin != null
									&& prevArduinoPin.connectedPins
											.containsKey(controller.getClass()
													.getName() + shieldPinName))
								prevArduinoPin.connectedPins.remove(controller
										.getClass().getName() + shieldPinName);
							controller.matchedShieldPins.put(shieldPinName,
									arduinoPin);
							arduinoPin.connectedPins.put(controller.getClass()
									.getName() + shieldPinName, true);
							listner.onSelect(arduinoPin);
						} else {
							ArduinoPin prevArduinoPin = controller.matchedShieldPins
									.get(shieldPinName);
							if (prevArduinoPin != null
									&& prevArduinoPin.connectedPins
											.containsKey(controller.getClass()
													.getName() + shieldPinName))
								prevArduinoPin.connectedPins.remove(controller
										.getClass().getName() + shieldPinName);
							controller.matchedShieldPins
									.remove(controller.shieldPins[selectedConnectingPin]);
							listner.onSelect(null);
						}
						((OneShieldTextView) pinsSubContainers.get(selectedConnectingPin)
								.getChildAt(1))
								.setText(tag.startsWith("_") ? tag.substring(1)
										: tag);
					}
				}, (ImageView) getView().findViewById(R.id.cursor), controller,
				new OnPinsDrawn() {

					@Override
					public void onDraw() {
						// int pdng = (int) (2 *
						// getResources().getDisplayMetrics().density
						// -
						// .5f);
						HorizontalScrollView scrollingPins = (HorizontalScrollView) getView()
								.findViewById(R.id.scrollingPins);
						for (int i = 0; i < controller.shieldPins.length; i++) {
							LinearLayout pinSubContainer = (LinearLayout) getActivity()
									.getLayoutInflater().inflate(
											R.layout.pin_sub_container, null,
											false);
							OneShieldButton pinButton = (OneShieldButton) pinSubContainer
									.getChildAt(0);
							pinButton.setText(controller.shieldPins[i]);
							OneShieldTextView pinText = (OneShieldTextView) pinSubContainer
									.getChildAt(1);
							final String pinName = controller.matchedShieldPins
									.get(pinButton.getText().toString()) != null ? controller.matchedShieldPins
									.get(pinButton.getText().toString()).name()
									: "";
							pinText.setText(pinName.startsWith("_") ? pinName
									.substring(1) : pinName);
							final int x = i;
							pinButton
									.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											if (selectedConnectingPin != -1)
												pinsSubContainers
														.get(selectedConnectingPin)
														.getChildAt(0)
														.setBackgroundColor(
																unSelectedColor);
											pinsSubContainers
													.get(x)
													.getChildAt(0)
													.setBackgroundColor(
															selectedColor);
											selectedConnectingPin = x;
											// thisPinsContainer.setCursorTo(thisPinsContainer
											// .getDataOfTag(pinName));
										}
									});
							pinsContainer.addView(pinSubContainer);
							pinsSubContainers.add(pinSubContainer);
							scrollingPins.invalidate();
						}
						pinsSubContainers.get(0).getChildAt(0)
								.setBackgroundColor(selectedColor);
						selectedConnectingPin = 0;
						// thisPinsContainer
						// .setCursorTo(thisPinsContainer.getDataOfTag(controller.matchedShieldPins
						// .get(controller.shieldPins[0]) != null ?
						// controller.matchedShieldPins
						// .get(controller.shieldPins[0]).name() : ""));
					}
				});
	}

	public static interface OnPinSelectionListener {
		public void onSelect(ArduinoPin pin);
	}

	public static interface OnPinsDrawn {
		public void onDraw();
	}
}
