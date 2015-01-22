package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

public class LcdShield extends ControllerParent<LcdShield> {
    private LcdEventHandler eventHandler;
    // private Activity activity;
    public int rows = 2;
    public int columns = 16;
    public char[] chars;
    public int currIndx = 0;
    // Method ids
    private static final byte PRINT = (byte) 0x11;
    private static final byte BEGIN = (byte) 0x01;
    private static final byte CLEAR = (byte) 0x02;
    private static final byte HOME = (byte) 0x03;
    // private static final byte NO_DISPLAY = (byte) 0x05;
    // private static final byte DISPLAY = (byte) 0x06;
    private static final byte NO_BLINK = (byte) 0x04;
    private static final byte BLINK = (byte) 0x05;
    private static final byte NO_CURSOR = (byte) 0x06;
    private static final byte CURSOR = (byte) 0x07;
    private static final byte SCROLL_DISPLAY_LEFT = (byte) 0x08;
    private static final byte SCROLL_DISPLAY_RIGHT = (byte) 0x09;
    private static final byte LEFT_TO_RIGHT = (byte) 0x0A;
    private static final byte RIGHT_TO_LEFT = (byte) 0x0B;
    // private static final byte CREATE_CHAR = (byte) 0x0F;
    private static final byte SET_CURSOR = (byte) 0x0E;
    private static final byte WRITE = (byte) 0x0F;
    private static final byte AUTO_SCROLL = (byte) 0x0C;
    private static final byte NO_AUTO_SCROLL = (byte) 0x0D;
    public int lastScrollLeft = 0;
    public boolean isBlinking = false, isCursoring = false;
    private boolean isAutoScroll = false;
    private boolean isLeftToRight = true;

    public LcdShield() {
        super();
    }

    public LcdShield(Activity activity, String tag) {
        super(activity, tag);
        setChars(new char[rows * columns]);
    }

    @Override
    public ControllerParent<LcdShield> init(String tag) {
        setChars(new char[columns * rows]);
        return super.init(tag);
    }

    public void setLcdEventHandler(LcdEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public void write(char ch) {
        if (currIndx > -1 && currIndx < chars.length) {
            // if (isLeftToRight) {
            if (!isAutoScroll) {
                chars[currIndx] = ch;
                changeCursor(isLeftToRight ? currIndx + 1 : currIndx - 1);
            } else {
                final char[] tmp = chars;
                for (int i = 0; i < currIndx; i++) {
                    if (i + 1 < tmp.length && i + 1 > -1)
                        chars[i] = tmp[i + 1];
                }
                if (currIndx - 1 > -1 && currIndx - 1 < chars.length)
                    chars[currIndx - 1] = ch;
            }
        }
    }

    public void changeCursor(int indx) {
        if (!isAutoScroll && indx > -1 && indx < rows * columns) {
            if (eventHandler != null) {
                eventHandler.noBlink();
                eventHandler.noCursor();
            }
            currIndx = indx;
        }
    }

    public synchronized void scrollDisplayLeft() {
        lastScrollLeft = 1;
        char[] tmp = new char[chars.length];
        for (int i = 0; i < tmp.length; i++) {
            if (i + lastScrollLeft > -1 && i + lastScrollLeft < chars.length) {
                tmp[i] = chars[i + lastScrollLeft];
            }
        }
        if (eventHandler != null)
            eventHandler.noBlink();
        changeCursor(currIndx - 1);
        chars = tmp;
        if (eventHandler != null) {
            eventHandler.updateLCD(chars);
            if (isBlinking)
                eventHandler.blink();
        }
        Log.d("LCD", (">>>>>>>  Left  " + lastScrollLeft));
    }

    public synchronized void scrollDisplayRight() {
        lastScrollLeft = -1;
        char[] tmp = new char[chars.length];
        for (int i = 0; i < tmp.length; i++) {
            if (i + lastScrollLeft > -1 && i + lastScrollLeft < chars.length) {
                tmp[i] = chars[i + lastScrollLeft];
            }
        }
        if (eventHandler != null)
            eventHandler.noBlink();
        changeCursor(currIndx + 1);
        chars = tmp;
        if (eventHandler != null) {
            eventHandler.updateLCD(chars);
            if (isBlinking)
                eventHandler.blink();
        }
        Log.d("LCD", (">>>>>>>  Right  " + lastScrollLeft));
    }

    public static interface LcdEventHandler {
        public void updateLCD(char[] arrayToUpdate);

        public void blink();

        public void noBlink();

        public void cursor();

        public void noCursor();
    }

    private void processInput(ShieldFrame frame) {
        switch (frame.getFunctionId()) {
            case CLEAR:
                if (eventHandler != null) {
                    eventHandler.noBlink();
                    eventHandler.noCursor();
                }
                lastScrollLeft = 0;
                chars = new char[columns * rows];
                if (eventHandler != null)
                    eventHandler.updateLCD(chars);
                changeCursor(0);
                if (isBlinking && eventHandler != null)
                    eventHandler.blink();
                if (isCursoring && eventHandler != null)
                    eventHandler.cursor();
                break;
            case HOME:
                if (eventHandler != null)
                    eventHandler.noBlink();
                if (eventHandler != null)
                    eventHandler.noCursor();
                changeCursor(0);
                if (isBlinking && eventHandler != null)
                    eventHandler.blink();
                if (isCursoring && eventHandler != null)
                    eventHandler.cursor();
                break;
            case BLINK:
                if (eventHandler != null)
                    eventHandler.blink();
                isBlinking = true;
                break;
            case NO_BLINK:
                if (eventHandler != null)
                    eventHandler.noBlink();
                isBlinking = false;
                break;

            case SCROLL_DISPLAY_LEFT:
                scrollDisplayLeft();
                break;

            case SCROLL_DISPLAY_RIGHT:
                scrollDisplayRight();
                break;

            case BEGIN:
                break;
            case SET_CURSOR:
                if (eventHandler != null) {
                    eventHandler.noBlink();
                    eventHandler.noCursor();
                }
                int row = (int) frame.getArgument(0)[0];
                int col = (int) frame.getArgument(1)[0];
                changeCursor((row * columns) + col);
                if (isBlinking && eventHandler != null)
                    eventHandler.blink();
                if (isCursoring && eventHandler != null)
                    eventHandler.cursor();
                break;
            case WRITE:
                write(frame.getArgumentAsString(0).charAt(0));
                if (eventHandler != null) {
                    eventHandler.updateLCD(chars);
                    if (isBlinking)
                        eventHandler.blink();
                    if (isCursoring)
                        eventHandler.cursor();
                }
                break;
            case PRINT:
                lastScrollLeft = 0;
                if (eventHandler != null) {
                    eventHandler.noBlink();
                    eventHandler.noCursor();
                }
                lastScrollLeft = 0;
                String txt = frame.getArgumentAsString(0);
                for (int i = 0; i < txt.length(); i++) {
                    write(txt.charAt(i));
                }
                if (eventHandler != null) {
                    eventHandler.updateLCD(chars);
                    if (isBlinking)
                        eventHandler.blink();
                    if (isCursoring)
                        eventHandler.cursor();
                }
                break;

            case CURSOR:
                if (eventHandler != null)
                    eventHandler.cursor();
                isCursoring = true;
                break;

            case NO_CURSOR:
                if (eventHandler != null)
                    eventHandler.noCursor();
                isCursoring = false;
                break;
            case AUTO_SCROLL:
                isAutoScroll = true;
                break;
            case NO_AUTO_SCROLL:
                isAutoScroll = false;
                break;
            case LEFT_TO_RIGHT:
                isLeftToRight = true;
                break;
            case RIGHT_TO_LEFT:
                isLeftToRight = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.LCD_SHIELD.getId())
            processInput(frame);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    public void setChars(char[] chars) {
        this.chars = chars;
    }

}
