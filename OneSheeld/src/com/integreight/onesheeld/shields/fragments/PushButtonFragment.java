package com.integreight.onesheeld.shields.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.controller.PushButtonShield;
import com.integreight.onesheeld.utils.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;

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
		final AppSlidingLeftMenu menu = (AppSlidingLeftMenu) getActivity()
				.findViewById(R.id.sliding_pane_layout);
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
						menu.setCanSlide(false);
					} else {
						((PushButtonShield) getApplication()
								.getRunningShields().get(getControllerTag()))
								.setButton(false);
						menu.setCanSlide(true);
					}
					return true;
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
					((PushButtonShield) getApplication().getRunningShields()
							.get(getControllerTag())).setButton(false);
					menu.setCanSlide(false);
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
							if (rect.contains((int) event.getX(),
									(int) event.getY())) {
								((PushButtonShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.setButton(true);
								menu.setCanSlide(false);
							} else {
								((PushButtonShield) getApplication()
										.getRunningShields().get(
												getControllerTag()))
										.setButton(false);
								menu.setCanSlide(true);
							}
							return true;
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							((PushButtonShield) getApplication()
									.getRunningShields()
									.get(getControllerTag())).setButton(false);
							menu.setCanSlide(true);
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

		ConnectingPinsView.getInstance().reset(
				getApplication().getRunningShields().get(getControllerTag()),
				new OnPinSelectionListener() {

					@Override
					public void onSelect(ArduinoPin pin) {
						if (pin != null) {
							((PushButtonShield) getApplication()
									.getRunningShields()
									.get(getControllerTag()))
									.setConnected(new ArduinoConnectedPin(
											pin.microHardwarePin,
											ArduinoFirmata.OUTPUT));
						}

					}
				});
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
	}

	@Override
	public void doOnServiceConnected() {
	}

}