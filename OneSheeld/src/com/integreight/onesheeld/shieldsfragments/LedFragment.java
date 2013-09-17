package com.integreight.onesheeld.shieldsfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.LedShield;
import com.integreight.onesheeld.shields.LedShield.LedEventHandler;

public class LedFragment extends Fragment {

	ImageView ledImage;
	LedShield led;
	Button connectButton;
	ShieldsOperationActivity activity;

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
		// TODO Auto-generated method stub
		super.onStart();

		if (activity.getFirmata() == null) {
			activity.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(activity.getFirmata());
		}
		if(led!=null)toggleLed(led.refreshLed());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.led_fragment_connect_button);

		final CharSequence[] items = { "0","1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4", "A5" };

		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				AlertDialog.Builder builder3 = new AlertDialog.Builder(
						getActivity());
				builder3.setTitle("Connect With").setItems(items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// TODO Auto-generated method stub
								led.setLedEventHandler(ledEventHandler, which);
								toggleLed(led.refreshLed());

							}

						});

				builder3.show();

			}

		});
		activity=(ShieldsOperationActivity)getActivity();
	}

	private LedEventHandler ledEventHandler = new LedEventHandler() {

		@Override
		public void onLedChange(boolean isLedOn) {
			// TODO Auto-generated method stub
			toggleLed(isLedOn);

		}
	};

	private OneSheeldServiceHandler serviceHandler = new OneSheeldServiceHandler() {

		@Override
		public void onServiceConnected(ArduinoFirmata firmata) {
			// TODO Auto-generated method stub

			initializeFirmata(firmata);

		}

		@Override
		public void onServiceDisconnected() {
			// TODO Auto-generated method stub

		}
	};

	private void toggleLed(boolean isOn) {
		if (isOn) {
			ledImage.setImageResource(R.drawable.led_shield_led_on);
		} else {
			ledImage.setImageResource(R.drawable.led_shield_led_off);
		}
	}

	// private void intializeFirmata(ArduinoFirmata firmata, int connectedPin){
	//
	// led=new Led(firmata,connectedPin);
	// led.setLedEventHandler(ledEventHandler);
	// toggleLed(led.isLedOn());
	// }

	private void initializeFirmata(ArduinoFirmata firmata) {
		if(led==null)led = new LedShield(firmata);

	}

}
