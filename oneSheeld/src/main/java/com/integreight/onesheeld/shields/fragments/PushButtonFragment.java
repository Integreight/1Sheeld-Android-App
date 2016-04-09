package com.integreight.onesheeld.shields.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.PushButtonShield;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;
import com.integreight.onesheeld.utils.customviews.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;

public class PushButtonFragment extends
        ShieldFragmentParent<PushButtonFragment> {
    Rect rect;
    AppSlidingLeftMenu menu;
    OneSheeldButton push;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.push_button_shield_fragment_layout,
                container, false);

    }

    private void on(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            ((PushButtonShield) getApplication().getRunningShields().get(
                    getControllerTag())).setButton(true);
        push.setBackgroundResource(R.drawable.button_shield_green);
        menu.setCanSlide(false);
    }

    private void off() {
        ((PushButtonShield) getApplication().getRunningShields().get(
                getControllerTag())).setButton(false);
        push.setBackgroundResource(R.drawable.button_shield_red);
        menu.setCanSlide(true);
    }

    @Override
    public void doOnStart() {
        ConnectingPinsView.getInstance().reset(
                getApplication().getRunningShields().get(getControllerTag()),
                new OnPinSelectionListener() {

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null) {
                            (getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag()))
                                    .setConnected(new ArduinoConnectedPin(
                                            pin.microHardwarePin,
                                            OneSheeldDevice.OUTPUT));
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                    }
                });
        if (getApplication().getRunningShields().get(
                getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new PushButtonShield(activity, getControllerTag()));
        }

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
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
                        off();
                    }
                    return true;
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    off();
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    public void doOnServiceConnected() {
    }

}