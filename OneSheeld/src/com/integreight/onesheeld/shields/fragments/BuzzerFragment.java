package com.integreight.onesheeld.shields.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.ShieldsOperationActivity;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield.SpeakerEventHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class BuzzerFragment extends ShieldFragmentParent<BuzzerFragment> {

	SpeakerShield speaker;
	Button connectButton;
	MainActivity activity;

	private static final int soundResourceId = R.raw.door_chime_sound;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.buzzer_shield_fragment_layout,
				container, false);
		return v;

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.speaker_fragment_connect_button);

		final CharSequence[] items = { "0", "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4",
				"A5" };

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
								speaker.setSpeakerEventHandler(
										speakerEventHandler, which);

							}

						});

				builder3.show();

			}

		});

		activity = (MainActivity) getActivity();
	}

	private SpeakerEventHandler speakerEventHandler = new SpeakerEventHandler() {

		@Override
		public void onSpeakerPinChange(boolean isOn) {
			// TODO Auto-generated method stub
			// if (isOn) {
			// activity.playSound(soundResourceId);
			// }

		}
	};

	private void initializeFirmata() {
		if (speaker == null)
			speaker = new SpeakerShield(getApplication().getAppFirmata());

	}

	@Override
	public void doOnServiceConnected() {
		initializeFirmata();
	}

}
