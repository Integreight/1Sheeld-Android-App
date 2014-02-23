package com.integreight.onesheeld.shields.fragments;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.PushButtonShield;
import com.integreight.onesheeld.shields.controller.ToggleButtonShield;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PushButtonFragment extends
		ShieldFragmentParent<PushButtonFragment> {

	Button connectButton;
	Rect rect;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View v = inflater.inflate(
				R.layout.push_button_shield_fragment_layout, container, false);
		if ((PushButtonShield) getApplication().getRunningShields().get(
				getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new PushButtonShield(getActivity(), getControllerTag()));
		}

		final Button push = (Button) v
				.findViewById(R.id.push_button_shield_button_push_button);
		push.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (rect == null) {
					rect = new Rect(push.getLeft(), push.getTop(), push
							.getRight(), push.getBottom());
				}
				if (arg1.getAction() == MotionEvent.ACTION_DOWN
						|| arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if (rect.contains((int) arg1.getX() + rect.left,
							(int) arg1.getY() + rect.top)) {
						((PushButtonShield) getApplication()
								.getRunningShields().get(getControllerTag()))
								.setButton(true);
					} else {
						((PushButtonShield) getApplication()
								.getRunningShields().get(getControllerTag()))
								.setButton(false);
					}
					return true;
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
					((PushButtonShield) getApplication().getRunningShields()
							.get(getControllerTag())).setButton(false);
					return true;
				}
				return false;
			}
		});
		v.findViewById(R.id.pushContainer).setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (rect == null) {
							rect = new Rect(push.getLeft(), push.getTop(), push
									.getRight(), push.getBottom());
						}
						if (event.getAction() == MotionEvent.ACTION_DOWN
								|| event.getAction() == MotionEvent.ACTION_MOVE) {
							((PushButtonShield) getApplication()
									.getRunningShields()
									.get(getControllerTag())).setButton(rect
									.contains((int) event.getX(),
											(int) event.getY()));
							return true;
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							((PushButtonShield) getApplication()
									.getRunningShields()
									.get(getControllerTag())).setButton(false);
							return true;
						}
						return false;
					}
				});
		return v;

	}

	@Override
	public void onStart() {

		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(true);
		super.onStart();

	}

	@Override
	public void onStop() {
		getApplication().getRunningShields().get(getControllerTag())
				.setHasForgroundView(false);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		connectButton = (Button) getView().findViewById(
				R.id.push_button_fragment_connect_button);

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
								((PushButtonShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.setConnected(new ArduinoConnectedPin(
												which, ArduinoFirmata.OUTPUT));

							}

						});

				builder3.show();

			}

		});
	}

	@Override
	public void doOnServiceConnected() {
	}

}