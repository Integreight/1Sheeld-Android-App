package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewSwitcher;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;
import com.integreight.onesheeld.utils.customviews.AnalogStickView;
import com.integreight.onesheeld.utils.customviews.Key;
import com.integreight.onesheeld.utils.customviews.Key.KeyTouchEventListener;

public class GamepadFragment extends ShieldFragmentParent<GamepadFragment> {

    private AnalogStickView analogStick;
    private Key upArrowKey;
    private Key downArrowKey;
    private Key leftArrowKey;
    private Key rightArrowKey;
    private Key yellowArrowKey;
    private Key greenArrowKey;
    private Key blueArrowKey;
    private Key redArrowKey;
    private Button gamePadModeSwitcher;
    private ViewSwitcher gamePadViewSwitcher;

    private AnalogStickView.AnalogStickTouchListener analogStickTouchListener
            = new AnalogStickView.AnalogStickTouchListener() {
        @Override
        public void onValueChange(int x, int y, int angle, int power, int direction) {
            ((GamepadShield) getApplication().getRunningShields().get(getControllerTag()))
                    .setAnalogPins((byte)x,(byte)y,angle,(byte)power,(byte)direction);
        }
    };

    KeyTouchEventListener touchEventListener = new KeyTouchEventListener() {

        @Override
        public void onPressed(Key k) {
            // TODO Auto-generated method stub
            switch (k.getId()) {
                case R.id.gamepad_up_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.UP_ARROW.getName(), GamepadShield.Key.UP_ARROW.getId());
                    break;
                case R.id.gamepad_down_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.DOWN_ARROW.getName(), GamepadShield.Key.DOWN_ARROW.getId());
                    break;
                case R.id.gamepad_right_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.RIGHT_ARROW.getName(), GamepadShield.Key.RIGHT_ARROW.getId());
                    break;

                case R.id.gamepad_left_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.LEFT_ARROW.getName(), GamepadShield.Key.LEFT_ARROW.getId());
                    break;

                case R.id.gamepad_yellow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.YELLOW_BUTTON.getName(), GamepadShield.Key.YELLOW_BUTTON.getId());
                    break;
                case R.id.gamepad_green_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.GREEN_BUTTON.getName(), GamepadShield.Key.GREEN_BUTTON.getId());
                    break;
                case R.id.gamepad_red_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.RED_BUTTON.getName(), GamepadShield.Key.RED_BUTTON.getId());
                    break;
                case R.id.gamepad_blue_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToHigh(
                            GamepadShield.Key.BLUE_BUTTON.getName(), GamepadShield.Key.BLUE_BUTTON.getId());
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
                            GamepadShield.Key.UP_ARROW.getName(), GamepadShield.Key.UP_ARROW.getId());
                    break;
                case R.id.gamepad_down_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.DOWN_ARROW.getName(), GamepadShield.Key.DOWN_ARROW.getId());
                    break;
                case R.id.gamepad_right_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.RIGHT_ARROW.getName(), GamepadShield.Key.RIGHT_ARROW.getId());
                    break;

                case R.id.gamepad_left_arrow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.LEFT_ARROW.getName(), GamepadShield.Key.LEFT_ARROW.getId());
                    break;

                case R.id.gamepad_yellow_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.YELLOW_BUTTON.getName(), GamepadShield.Key.YELLOW_BUTTON.getId());
                    break;
                case R.id.gamepad_green_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.GREEN_BUTTON.getName(), GamepadShield.Key.GREEN_BUTTON.getId());
                    break;
                case R.id.gamepad_red_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.RED_BUTTON.getName(), GamepadShield.Key.RED_BUTTON.getId());
                    break;
                case R.id.gamepad_blue_key:
                    ((GamepadShield) getApplication().getRunningShields().get(
                            getControllerTag())).setPinToLow(
                            GamepadShield.Key.BLUE_BUTTON.getName(), GamepadShield.Key.BLUE_BUTTON.getId());
                    break;

                default:
                    break;
            }
        }
    };

    private Button.OnClickListener onGamePadModeSwitcherClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(!((GamepadShield)getApplication().getRunningShields().get(getControllerTag()))
                    .isReadyToSwitch()){
                return;
            }
            resetConnectingPins();
            if (((GamepadShield)getApplication().getRunningShields().get(getControllerTag()))
                    .getGamePadMode() == GamepadShield.GamePadMode.KEYS){
                ((GamepadShield)getApplication().getRunningShields().get(getControllerTag()))
                        .setGamePadMode(GamepadShield.GamePadMode.ANALOG);
                gamePadViewSwitcher.showPrevious();
            }
            else if (((GamepadShield)getApplication().getRunningShields().get(getControllerTag()))
                    .getGamePadMode() == GamepadShield.GamePadMode.ANALOG){
                ((GamepadShield)getApplication().getRunningShields().get(getControllerTag()))
                        .setGamePadMode(GamepadShield.GamePadMode.KEYS);
                gamePadViewSwitcher.showNext();
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

        analogStick  = (AnalogStickView) v.findViewById(R.id.gamepad_analog_stick);
        analogStick.setTouchListener(analogStickTouchListener);

        gamePadModeSwitcher = (Button) v.findViewById(R.id.gamepad_mode_switcher);
        gamePadModeSwitcher.setOnClickListener(onGamePadModeSwitcherClickListener);

        gamePadViewSwitcher = (ViewSwitcher) v.findViewById(R.id.gamepad_view_switcher);
    }

    @Override
    public void doOnStart() {
        resetConnectingPins();
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

    private void resetConnectingPins(){
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
                                            ArduinoFirmata.OUTPUT));
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                        // TODO Auto-generated method stub

                    }
                });
    }
}

