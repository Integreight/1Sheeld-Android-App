package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.BitsUtils;

public class GamepadShield extends ControllerParent<GamepadShield> {
    public enum GamePadMode {KEYS,ANALOG}

    private GamePadMode gamePadMode;
    private ShieldFrame sf;
    private byte keysStatus; //Every bit holds status of a key
    private byte analogX;
    private byte analogY;
    private int angle;
    private byte power;
    private byte direction;
    private static final byte GAMEPAD_KEYS = 0x01;
    private static final byte GAMEPAD_ANALOG = 0x02;
    private static final String[] keysPins = {"Up Arrow", "Right Arrow", "Down Arrow",
            "Left Arrow", "Yellow Button", "Red Button", "Green Button",
            "Blue Button"};
    private static final String[] analogPins = {"Yellow Button", "Red Button", "Green Button",
            "Blue Button","Analog X","Analog Y","Power","Angle"};

    public GamepadShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<GamepadShield> init(String tag) {
        return super.init(tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        // TODO Auto-generated method stub
        super.setConnected(pins);
    }

    public GamepadShield() {
        super();
        requiredPinsIndex = 0;
        keysStatus = 0;
        // The View Switcher starts with the KEYS mode
        setGamePadMode(GamePadMode.KEYS);
    }

    public void setPinToHigh(String pinName, int pinId) {
        keysStatus = BitsUtils.setBit(keysStatus, pinId);
        ArduinoPin arduinoPin = matchedShieldPins.get(pinName);
        if (arduinoPin != null) digitalWrite(arduinoPin.microHardwarePin, ArduinoFirmata.HIGH);
        sendShieldFrame();
    }

    public void setPinToLow(String pinName, int pinId) {
        keysStatus = BitsUtils.resetBit(keysStatus, pinId);
        ArduinoPin arduinoPin = matchedShieldPins.get(pinName);
        if (arduinoPin != null) digitalWrite(arduinoPin.microHardwarePin, ArduinoFirmata.LOW);
        sendShieldFrame();
    }

    public void setAnalogPins(byte analogX, byte analogY, int angle, byte power, byte direction){
        this.analogX = analogX;
        this.analogY = analogY;
        this.angle = angle;
        this.power = power;
        this.direction = direction;

        ArduinoPin analogXArduinoPin = matchedShieldPins.get("Analog X");
        ArduinoPin analogYArduinoPin = matchedShieldPins.get("Analog Y");
        ArduinoPin powerArduinoPin = matchedShieldPins.get("Power");
        ArduinoPin angleArduinoPin = matchedShieldPins.get("Angle");

        if (analogXArduinoPin != null) analogWrite(analogXArduinoPin.microHardwarePin,(analogX & 0xFF));
        if (analogYArduinoPin != null) analogWrite(analogYArduinoPin.microHardwarePin,(analogY & 0xFF));
        if (powerArduinoPin != null) analogWrite(powerArduinoPin.microHardwarePin,(power & 0xFF));
        if (angleArduinoPin != null) analogWrite(angleArduinoPin.microHardwarePin,angle);
        sendShieldFrame();
    }

    public enum Key {
        UP_ARROW(4, "Up Arrow"), RIGHT_ARROW(7, "Right Arrow"), DOWN_ARROW(5,
                "Down Arrow"), LEFT_ARROW(6, "Left Arrow"), YELLOW_BUTTON(0,
                "Yellow Button"), RED_BUTTON(1, "Red Button"), GREEN_BUTTON(2,
                "Green Button"), BLUE_BUTTON(3, "Blue Button");

        String name;
        int id;

        Key(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    public GamePadMode getGamePadMode(){
        return gamePadMode;
    }

    public void setGamePadMode(GamePadMode gamePadMode){
        reset();
        this.gamePadMode = gamePadMode;
        switch (gamePadMode) {
            case ANALOG:
                shieldPins = analogPins;
                break;
            case KEYS:
                shieldPins = keysPins;
                break;
        }
    }

    private void sendShieldFrame(){
        if(gamePadMode == GamePadMode.KEYS) {
            sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), GAMEPAD_KEYS);
            sf.addByteArgument(keysStatus);
            sendShieldFrame(sf);
        }
        else if (gamePadMode == GamePadMode.ANALOG){
            keysStatus = (byte) (keysStatus & 0x0F); // masking unused keys for precaution
            sf = new ShieldFrame(UIShield.GAMEDPAD_SHIELD.getId(), GAMEPAD_ANALOG);
            sf.addByteArgument(keysStatus);
            sf.addByteArgument(analogX);
            sf.addByteArgument(analogY);
            sf.addIntegerArgument(2,angle);
            sf.addByteArgument(power);
            sf.addByteArgument(direction);
            sendShieldFrame(sf);
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        sf = null;
        keysStatus = 0;
        analogX = 127;
        analogY = 127;
        angle = 0;
        power = 0;
        direction = 0;
    }
}
