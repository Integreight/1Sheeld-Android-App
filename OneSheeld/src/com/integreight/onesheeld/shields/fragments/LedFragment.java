package com.integreight.onesheeld.shields.fragments;

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
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.LedShield.LedEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;

public class LedFragment extends ShieldFragmentParent<LedFragment> {

	ImageView ledImage;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.led_shield_fragment_layout,
				container, false);
		ledImage = (ImageView) v.findViewById(R.id.led_shield_led_imageview);
		return v;

	}

	@Override
	public void onStart() {
		((LedShield) getApplication().getRunningShields().get(
				getControllerTag())).setLedEventHandler(ledEventHandler);
		toggleLed(((LedShield) getApplication().getRunningShields().get(
				getControllerTag())).refreshLed());
		ConnectingPinsView.getInstance().reset(
				getApplication().getRunningShields().get(getControllerTag()),
				new OnPinSelectionListener() {

					@Override
					public void onSelect(ArduinoPin pin) {
						if (pin != null) {
							((LedShield) getApplication().getRunningShields()
									.get(getControllerTag()))
									.setConnected(new ArduinoConnectedPin(
											pin.microHardwarePin,
											ArduinoFirmata.INPUT));
							toggleLed(getApplication().getAppFirmata()
									.digitalRead(pin.microHardwarePin));
						} else {
							((LedShield) getApplication().getRunningShields()
									.get(getControllerTag())).connectedPin = -1;
							toggleLed(false);
						}

					}
				}); // TODO Auto-generated method stub
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

	private LedEventHandler ledEventHandler = new LedEventHandler() {

		@Override
		public void onLedChange(final boolean isLedOn) {
			// TODO Auto-generated method stub
			if (canChangeUI()) {
				toggleLed(isLedOn);
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
					new LedShield(getActivity(), getControllerTag()));
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
