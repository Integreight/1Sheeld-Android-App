package com.integreight.onesheeld.shieldsfragments;

import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import com.integreight.onesheeld.shields.SevenSegmentShield;
import com.integreight.onesheeld.shields.SevenSegmentShield.Segment;
import com.integreight.onesheeld.shields.SevenSegmentShield.SevenSegmentsEventHandler;

public class SevenSegmentFragment extends Fragment {

	SevenSegmentShield sevenSegment;
	Button connectButton;
	ShieldsOperationActivity activity;
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
		// TODO Auto-generated method stub
		super.onStart();

		if (activity.getFirmata() == null) {
			activity.addServiceEventHandler(serviceHandler);
		} else {
			initializeFirmata(activity.getFirmata());
		}
		if (sevenSegment != null)
			refreshSegments(sevenSegment.refreshSegments());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.sevensegment_fragment_connect_button);

		final CharSequence[] arduinoPins = { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3",
				"A4", "A5" };

		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				final AlertDialog.Builder builder3 = new AlertDialog.Builder(
						getActivity());
				builder3.setTitle("Choose pin to connect").setItems(
						Segment.getSegmentsNames(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									final int whichSegment) {

								// TODO Auto-generated method stub
								AlertDialog.Builder builder2 = new AlertDialog.Builder(
										getActivity());
								builder2.setTitle("Connect With").setItems(
										arduinoPins,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int whichArduinoPin) {
												sevenSegment.connectSegmentWithPin(
														Segment.getSegment(whichSegment),
														whichArduinoPin);
												builder3.show();
											}

										});

								builder2.show();

							}

						});
				builder3.setPositiveButton("Done!", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						sevenSegment
								.setSevenSegmentsEventHandler(sevenSegmentsEventHandler);
						refreshSegments(sevenSegment.refreshSegments());
					}
				});
				builder3.show();

			}

		});
		activity = (ShieldsOperationActivity) getActivity();
	}

	private SevenSegmentsEventHandler sevenSegmentsEventHandler = new SevenSegmentsEventHandler() {

		@Override
		public void onSegmentsChange(Map<Segment, Boolean> segmentsStatus) {
			// TODO Auto-generated method stub
			refreshSegments(segmentsStatus);

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

	private void refreshSegments(Map<Segment, Boolean> segmentsStatus) {
		if (segmentsStatus.get(Segment.A)) {
			aSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			aSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}
		
		if (segmentsStatus.get(Segment.B)) {
			bSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			bSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}
		
		if (segmentsStatus.get(Segment.C)) {
			cSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			cSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}
		
		if (segmentsStatus.get(Segment.D)) {
			dSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			dSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}
		
		if (segmentsStatus.get(Segment.E)) {
			eSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			eSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}
		
		if (segmentsStatus.get(Segment.F)) {
			fSegment.setImageResource(R.drawable.seventsegment_on_vertical_image);
		} else {
			fSegment.setImageResource(R.drawable.seventsegment_off_vertical_image);
		}
		
		if ((segmentsStatus.get(Segment.A)&&segmentsStatus.get(Segment.B)&&segmentsStatus.get(Segment.E)&&segmentsStatus.get(Segment.D))||
				(segmentsStatus.get(Segment.A)&&segmentsStatus.get(Segment.B)&&segmentsStatus.get(Segment.C)&&segmentsStatus.get(Segment.D))||
				(segmentsStatus.get(Segment.F)&&segmentsStatus.get(Segment.B)&&segmentsStatus.get(Segment.C))||
				(segmentsStatus.get(Segment.A)&&segmentsStatus.get(Segment.F)&&segmentsStatus.get(Segment.C)&&segmentsStatus.get(Segment.D))||
				(segmentsStatus.get(Segment.C)&&segmentsStatus.get(Segment.D)&&segmentsStatus.get(Segment.E)&&segmentsStatus.get(Segment.F)&&segmentsStatus.get(Segment.A))||
				(segmentsStatus.get(Segment.F)&&segmentsStatus.get(Segment.A)&&segmentsStatus.get(Segment.B)&&segmentsStatus.get(Segment.C)&&segmentsStatus.get(Segment.D))
				) {
			gSegment.setImageResource(R.drawable.seventsegment_on_horizontal_image);
		} else {
			gSegment.setImageResource(R.drawable.seventsegment_off_horizontal_image);
		}
		
		if (segmentsStatus.get(Segment.DOT)) {
			dotSegment.setImageResource(R.drawable.seventsegment_on_dot_image);
		} else {
			dotSegment.setImageResource(R.drawable.seventsegment_off_dot_image);
		}

	}

	private void initializeFirmata(ArduinoFirmata firmata) {
		if (sevenSegment != null)return;
			sevenSegment = new SevenSegmentShield(firmata);

	}

}