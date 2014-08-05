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
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView.OnPinSelectionListener;

public class PushButtonFragment extends
		ShieldFragmentParent<PushButtonFragment> {

	Button connectButton;
	Rect rect;
	AppSlidingLeftMenu menu;
	OneSheeldButton push;
	View v;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.push_button_shield_fragment_layout,
				container, false);
		if ((PushButtonShield) getApplication().getRunningShields().get(
				getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new PushButtonShield(activity, getControllerTag()));
		}

		push = (OneSheeldButton) v
				.findViewById(R.id.push_button_shield_button_push_button);
		menu = (AppSlidingLeftMenu) activity
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
						on(arg1);
					} else {
						off(arg1);
					}
					return true;
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
					off(arg1);
					return true;
				}
				return false;
			}
		});
		return v;

	}

	private void on(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			((PushButtonShield) getApplication().getRunningShields().get(
					getControllerTag())).setButton(true);
		push.setBackgroundResource(R.drawable.button_shield_green);
		menu.setCanSlide(false);
	}

	private void off(MotionEvent event) {
		((PushButtonShield) getApplication().getRunningShields().get(
				getControllerTag())).setButton(false);
		push.setBackgroundResource(R.drawable.button_shield_red);
		menu.setCanSlide(true);
	}

	@Override
	public void onStart() {
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

					@Override
					public void onUnSelect(ArduinoPin pin) {
					}
				});
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

	@Override
	public void doOnServiceConnected() {
	}

}