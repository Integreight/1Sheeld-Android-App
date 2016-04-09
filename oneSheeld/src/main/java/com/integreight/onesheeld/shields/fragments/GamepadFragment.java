package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.shields.controller.GamepadShield.Pin;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;
import com.integreight.onesheeld.utils.customviews.Key;
import com.integreight.onesheeld.utils.customviews.Key.KeyTouchEventListener;

public class GamepadFragment extends ShieldFragmentParent<GamepadFragment> {

    private Key upArrowKey;
    private Key downArrowKey;
    private Key leftArrowKey;
    private Key rightArrowKey;
    private Key yellowArrowKey;
    private Key greenArrowKey;
    private Key blueArrowKey;
    private Key redArrowKey;

    KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

        @Override
        public void onPressed(Key k) {
            // TODO Auto-generated method stub
            switch (k.getId()) {
                case R.id.gamepad_up_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.UP_ARROW.getName(), Pin.UP_ARROW.getId());
                    break;
                case R.id.gamepad_down_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.DOWN_ARROW.getName(), Pin.DOWN_ARROW.getId());
                    break;
                case R.id.gamepad_right_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.RIGHT_ARROW.getName(), Pin.RIGHT_ARROW.getId());
                    break;

                case R.id.gamepad_left_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.LEFT_ARROW.getName(), Pin.LEFT_ARROW.getId());
                    break;

                case R.id.gamepad_yellow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.YELLOW_BUTTON.getName(), Pin.YELLOW_BUTTON.getId());
                    break;
                case R.id.gamepad_green_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.GREEN_BUTTON.getName(), Pin.GREEN_BUTTON.getId());
                    break;
                case R.id.gamepad_red_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.RED_BUTTON.getName(), Pin.RED_BUTTON.getId());
                    break;
                case R.id.gamepad_blue_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            Pin.BLUE_BUTTON.getName(), Pin.BLUE_BUTTON.getId());
                    break;

                default:
                    break;
            }

        }

        @Override
        public void onReleased(Key k) {
            // TODO Auto-generated method stub
            switch (k.getId()) {
                case R.id.gamepad_up_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.UP_ARROW.getName(), Pin.UP_ARROW.getId());
                    break;
                case R.id.gamepad_down_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.DOWN_ARROW.getName(), Pin.DOWN_ARROW.getId());
                    break;
                case R.id.gamepad_right_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.RIGHT_ARROW.getName(), Pin.RIGHT_ARROW.getId());
                    break;

                case R.id.gamepad_left_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.LEFT_ARROW.getName(), Pin.LEFT_ARROW.getId());
                    break;

                case R.id.gamepad_yellow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.YELLOW_BUTTON.getName(), Pin.YELLOW_BUTTON.getId());
                    break;
                case R.id.gamepad_green_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.GREEN_BUTTON.getName(), Pin.GREEN_BUTTON.getId());
                    break;
                case R.id.gamepad_red_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.RED_BUTTON.getName(), Pin.RED_BUTTON.getId());
                    break;
                case R.id.gamepad_blue_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            Pin.BLUE_BUTTON.getName(), Pin.BLUE_BUTTON.getId());
                    break;

                default:
                    break;
            }

        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gamepad_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        upArrowKey = (Key) v.findViewById(R.id.gamepad_up_arrow_key);
        downArrowKey = (Key) v.findViewById(R.id.gamepad_down_arrow_key);
        leftArrowKey = (Key) v.findViewById(R.id.gamepad_left_arrow_key);
        rightArrowKey = (Key) v.findViewById(R.id.gamepad_right_arrow_key);
        yellowArrowKey = (Key) v.findViewById(R.id.gamepad_yellow_key);
        redArrowKey = (Key) v.findViewById(R.id.gamepad_red_key);
        greenArrowKey = (Key) v.findViewById(R.id.gamepad_green_key);
        blueArrowKey = (Key) v.findViewById(R.id.gamepad_blue_key);

        upArrowKey.setEventListener(touchEventListener);
        downArrowKey.setEventListener(touchEventListener);
        leftArrowKey.setEventListener(touchEventListener);
        rightArrowKey.setEventListener(touchEventListener);
        yellowArrowKey.setEventListener(touchEventListener);
        redArrowKey.setEventListener(touchEventListener);
        greenArrowKey.setEventListener(touchEventListener);
        blueArrowKey.setEventListener(touchEventListener);
    }

    @Override
    public void doOnStart() {
        ConnectingPinsView.getInstance().reset(
                getApplication().getRunningShields().get(getControllerTag()),
                new OnPinSelectionListener() {

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null) {
                            getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag())
                                    .setConnected(new ArduinoConnectedPin(
                                            pin.microHardwarePin,
                                            OneSheeldDevice.OUTPUT));
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    private void initializeFirmata() {
        if ((getApplication().getRunningShields().get(getControllerTag())) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new GamepadShield(activity, getControllerTag()));
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}
