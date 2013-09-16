package com.integreight.onesheeld.shieldsfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.activities.ShieldsOperationActivity.OneSheeldServiceHandler;
import com.integreight.onesheeld.shields.SliderShield;


public class SliderFragment extends Fragment {

	SeekBar seekBar;
	SliderShield sliderShield;
	Button connectButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater
				.inflate(R.layout.slider_shield_fragment_layout,
						container, false);
		seekBar = (SeekBar) v
				.findViewById(R.id.slider_fragment_seekbar);
		seekBar.setEnabled(false);
		seekBar.setMax(255);
		seekBar.setProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				sliderShield.setSliderValue(progress);
				
				
			}
		});
		
		if (sliderShield != null)
			seekBar.setEnabled(true);

		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (((ShieldsOperationActivity) getActivity()).getFirmata() == null) {
			((ShieldsOperationActivity) getActivity())
					.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(((ShieldsOperationActivity) getActivity())
					.getFirmata());
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.slider_fragment_connect_button);

		final CharSequence[] items = { "3", "5", "6", "9", "10","11" };

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
								int pin=Integer.parseInt(items[which].toString());
								sliderShield.setConnectedPin(pin);
								seekBar.setEnabled(true);

							}

						});

				builder3.show();

			}

		});
	}

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

	private void initializeFirmata(ArduinoFirmata firmata) {

		if (sliderShield != null)
			return;
		sliderShield = new SliderShield(firmata);

	}

}

