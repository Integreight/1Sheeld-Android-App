package com.integreight.onesheeld.utils.customviews;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;
import com.integreight.onesheeld.utils.customviews.PluginPinsColumnContainer.PinData;

public class PluginConnectingPinsView extends Fragment {
	private static PluginConnectingPinsView thisInstance;
	// TextView show;
	// private int selectedPin = 0;
	// private ArrayList<LinearLayout> pinsSubContainers = new
	// ArrayList<LinearLayout>();
	private View view;
	private String selectedPinName = "";

	public static PluginConnectingPinsView getInstance() {
		if (thisInstance == null) {
			thisInstance = new PluginConnectingPinsView();
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
			view = inflater.inflate(R.layout.plugin_connecting_pins_layout,
					container, false);
			isInflated = true;
		} else
			isInflated = false;
		if (((ViewGroup) view.getParent()) != null)
			((ViewGroup) view.getParent()).removeAllViews();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// if (isInflated) {
		// show = (TextView) view.findViewById(R.id.show);
		// }
		super.onActivityCreated(savedInstanceState);
	}

	private Handler resettingHandler = new Handler();

	public void reset(final OnPinSelectionListener listner) {
		// if (view == null) {
		// ((MainActivity) getActivity())
		// .getSupportFragmentManager()
		// .beginTransaction()
		// .replace(R.id.pinsViewContainer,
		// PluginConnectingPinsView.getInstance()).commit();
		// }
		final TextView show = (TextView) view.findViewById(R.id.show);
		// pinsSubContainers = new ArrayList<LinearLayout>();
		// selectedPin = 0;
		selectedPinName = "";
		show.setText("");
		show.setVisibility(View.INVISIBLE);
		// final int selectedColor = getResources().getColor(
		// R.color.arduinoPinsSelector);
		// final int unSelectedColor = 0x00000000;
		final ImageView cursor = ((ImageView) view.findViewById(R.id.cursor));
		cursor.setVisibility(View.INVISIBLE);
		final PluginPinsColumnContainer thisPinsContainer = ((PluginPinsColumnContainer) view
				.findViewById(R.id.cont));
		if (resettingHandler == null)
			resettingHandler = new Handler();
		resettingHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				thisPinsContainer.setup(new OnChildFocusListener() {

					@Override
					public void focusOnThisChild(int childIndex, String tag) {
						show.setVisibility(View.VISIBLE);
						show.setText(tag.startsWith("_") ? tag.substring(1)
								: tag);
					}

					@Override
					public void selectThisChild(int childIndex, String tag) {
						show.setVisibility(tag.length() > 0 ? View.VISIBLE
								: View.INVISIBLE);
						show.setText(tag.startsWith("_") ? tag.substring(1)
								: tag);
						// String shieldPinName =
						// controller.shieldPins[selectedPin];
						if (childIndex != -1) {
							ArduinoPin arduinoPin = ArduinoPin.valueOf(tag);
							// ArduinoPin prevArduinoPin =
							// controller.matchedShieldPins
							// .get(shieldPinName);
							// if (prevArduinoPin != null
							// && prevArduinoPin.connectedPins
							// .containsKey(controller.getClass()
							// .getName() + shieldPinName))
							// prevArduinoPin.connectedPins.remove(controller
							// .getClass().getName() + shieldPinName);
							// controller.matchedShieldPins.put(shieldPinName,
							// arduinoPin);
							// arduinoPin.connectedPins.put(controller.getClass()
							// .getName() + shieldPinName, true);
							listner.onSelect(arduinoPin);
						} else {
							// ArduinoPin prevArduinoPin =
							// controller.matchedShieldPins
							// .get(shieldPinName);
							// if (prevArduinoPin != null
							// && prevArduinoPin.connectedPins
							// .containsKey(controller.getClass()
							// .getName() + shieldPinName))
							// prevArduinoPin.connectedPins.remove(controller
							// .getClass().getName() + shieldPinName);
							// ((OneSheeldApplication) getActivity()
							// .getApplication())
							// .getAppFirmata()
							// .digitalWrite(
							// controller.matchedShieldPins
							// .get(controller.shieldPins[selectedPin]).microHardwarePin,
							// ArduinoFirmata.LOW);
							// controller.matchedShieldPins
							// .remove(controller.shieldPins[selectedPin]);
							// listner.onUnSelect(prevArduinoPin);
							listner.onSelect(null);
						}
						// ((OneShieldTextView) pinsSubContainers.get(
						// selectedPin).getChildAt(1)).setText(tag
						// .startsWith("_") ? tag.substring(1) : tag);
					}
				}, cursor, new onGetPinsView() {

					@Override
					public void onPinsDrawn() {
						// pinsSubContainers.get(0).getChildAt(0)
						// .setBackgroundColor(selectedColor);
						// selectedPin = 0;
						// selectedPinName = controller.matchedShieldPins
						// .get(controller.shieldPins[0]) != null ?
						// controller.matchedShieldPins
						// .get(controller.shieldPins[0]).name() : "";
						if (selectedPinName.length() > 0) {
							PinData data = thisPinsContainer
									.getDataOfTag(selectedPinName);
							if (data.rect != null && data.index != -1) {
								thisPinsContainer.setCursorTo(data);
								show.setVisibility(View.VISIBLE);
								show.setText(data.tag.startsWith("_") ? data.tag
										.substring(1) : data.tag);
							} else {
								show.setText("");
								show.setVisibility(View.INVISIBLE);
								cursor.setVisibility(View.INVISIBLE);
							}
						} else {
							show.setText("");
							show.setVisibility(View.INVISIBLE);
							cursor.setVisibility(View.INVISIBLE);
						}
					}
				});
				// pinsSubContainers = new ArrayList<LinearLayout>();
				LinearLayout pinsContainer = (LinearLayout) view
						.findViewById(R.id.requiredPinsContainer);
				pinsContainer.removeAllViews();
				// int pdng = (int) (2 *
				// getResources().getDisplayMetrics().density
				// -
				// .5f);
				// selectedPin = 0;
				// HorizontalScrollView scrollingPins = (HorizontalScrollView)
				// view
				// .findViewById(R.id.scrollingPins);
				// for (int i = 0; i < controller.shieldPins.length; i++) {
				// LinearLayout pinSubContainer = (LinearLayout) getActivity()
				// .getLayoutInflater().inflate(
				// R.layout.pin_sub_container, null, false);
				// final OneShieldButton pinButton = (OneShieldButton)
				// pinSubContainer
				// .getChildAt(0);
				// pinButton.setText(controller.shieldPins[i]);
				// if (i == 0)
				// pinButton.setBackgroundColor(selectedColor);
				// else
				// pinButton.setBackgroundColor(unSelectedColor);
				// OneShieldTextView pinText = (OneShieldTextView)
				// pinSubContainer
				// .getChildAt(1);
				// String pinName = controller.matchedShieldPins.get(pinButton
				// .getText().toString()) != null ? controller.matchedShieldPins
				// .get(pinButton.getText().toString()).name() : "";
				// pinText.setText(pinName.startsWith("_") ? pinName
				// .substring(1) : pinName);
				// final int x = i;
				// pinButton.setOnClickListener(new View.OnClickListener() {
				//
				// @Override
				// public void onClick(View arg0) {
				// if (selectedPin != -1)
				// pinsSubContainers.get(selectedPin)
				// .getChildAt(0)
				// .setBackgroundColor(unSelectedColor);
				// pinsSubContainers.get(x).getChildAt(0)
				// .setBackgroundColor(selectedColor);
				// selectedPin = x;
				// selectedPinName = controller.matchedShieldPins
				// .get(pinButton.getText().toString()) != null ?
				// controller.matchedShieldPins
				// .get(pinButton.getText().toString()).name()
				// : "";
				// if (selectedPinName != null
				// && selectedPinName.length() > 0) {
				// PinData data = thisPinsContainer
				// .getDataOfTag(selectedPinName);
				// if (data.rect != null && data.index != -1) {
				// thisPinsContainer.setCursorTo(data);
				// show.setVisibility(View.VISIBLE);
				// show.setText(data.tag.startsWith("_") ? data.tag
				// .substring(1) : data.tag);
				// } else {
				// show.setText("");
				// show.setVisibility(View.INVISIBLE);
				// cursor.setVisibility(View.INVISIBLE);
				// }
				// } else {
				// show.setText("");
				// show.setVisibility(View.INVISIBLE);
				// cursor.setVisibility(View.INVISIBLE);
				// }
				// }
				// });
				// pinsContainer.addView(pinSubContainer);
				// pinsSubContainers.add(pinSubContainer);
				// // scrollingPins.invalidate();
				// }
			}
		});
	}

	public static interface OnPinSelectionListener {
		public void onSelect(ArduinoPin pin);

		public void onUnSelect(ArduinoPin pin);
	}

	public static interface onGetPinsView {
		public void onPinsDrawn();
	}
}
