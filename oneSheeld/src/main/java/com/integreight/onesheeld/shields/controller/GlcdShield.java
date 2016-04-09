package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.MotionEvent;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;
import com.integreight.onesheeld.shields.controller.utils.glcd.AnalogGauge;
import com.integreight.onesheeld.shields.controller.utils.glcd.Button;
import com.integreight.onesheeld.shields.controller.utils.glcd.CheckBox;
import com.integreight.onesheeld.shields.controller.utils.glcd.Ellipse;
import com.integreight.onesheeld.shields.controller.utils.glcd.Label;
import com.integreight.onesheeld.shields.controller.utils.glcd.Line;
import com.integreight.onesheeld.shields.controller.utils.glcd.Point;
import com.integreight.onesheeld.shields.controller.utils.glcd.ProgressBar;
import com.integreight.onesheeld.shields.controller.utils.glcd.RadioButton;
import com.integreight.onesheeld.shields.controller.utils.glcd.RadioGroup;
import com.integreight.onesheeld.shields.controller.utils.glcd.RoundRectangle;
import com.integreight.onesheeld.shields.controller.utils.glcd.Shape;
import com.integreight.onesheeld.shields.controller.utils.glcd.Slider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 6/7/2015.
 */
public class GlcdShield extends ControllerParent<GlcdShield> {

    private static final byte SHIELD_ID = UIShield.GLCD_SHIELD.getId();


    public static final int glcdWidth = 256, glcdHeight = 128;
    public static int BLACK = Color.parseColor("#11443d"), WHITE = Color.parseColor("#338f45");
    public static final int TEXT_SMALL = 1, TEXT_MEDUIM = 3, TEXT_LARGE = 5;
    public static final int FONT_ARIEL_REGULAR = 0, FONT_ARIEL_BLACK = 1, FONT_ARIEL_ITALIC = 3, FONT_COMICSANS = 4, FONT_SERIF = 5;
    public static final int ORDER_SETTOUCH = 1, ORDER_CLEAR = 2, ORDER_HANDLETOUCH = 4, ORDER_APPLYTOUCH = 5;
    public static final byte SHAPE_BUTTON = 0x08, SHAPE_CHECKBOX = 0x0A, SHAPE_SLIDER = 0x0B, SHAPE_RADIOBUTTON = 0x09;
    public static final byte STATE_PRESSED = 0x01, STATE_RELEASED = 0x00, STATE_TOUCHED = 0x02;


    private GlcdEventHandler glcdEventHandler;
    public SparseArray<Shape> shapes = new SparseArray<>();
    public SparseArray<SparseArray<Integer>> touchs;
    public SparseArray<RadioGroup> radioGroups;
    private Shape tmpShape = null;
    public Integer currentPressedKey = null;
    private int buttonCounter = 0;

    private static final byte TYPE_GLCD = 0x00;
    private static final byte TYPE_POINT = 0x01;
    private static final byte TYPE_RECTANGLE = 0x02;
    private static final byte TYPE_LINE = 0x03;
    private static final byte TYPE_ELLIPSE = 0x04;
    private static final byte TYPE_LABEL = 0x05;
    private static final byte TYPE_PROGRESSBAR = 0x06;
    private static final byte TYPE_GAUGE = 0x07;
    private static final byte TYPE_BUTTON = 0x08;
    private static final byte TYPE_RADIOBUTTON = 0x09;
    private static final byte TYPE_CHECKBOX = 0x0A;
    private static final byte TYPE_SLIDER = 0x0B;

    private static final byte GLCD_CLEAR = 0x00;
    private static final byte GLCD_CLEAR_RECTANGLE = 0x01;

    private static final byte SHAPE_DRAW = 0x00;
    private static final byte SHAPE_SET_POSTION = 0x01;
    private static final byte SHAPE_SET_VISIBILITY = 0x02;

    private static final byte RECTANGLE_SET_RADIUS = 0x03;
    private static final byte RECTANGLE_SET_FILL = 0x04;
    private static final byte RECTANGLE_SET_DIMENSIONS = 0x05;

    private static final byte LINE_SET_COORDINATES = 0x03;

    private static final byte ELLIPSE_SET_RADIUS = 0x03;
    private static final byte ELLIPSE_SET_FILL = 0x04;

    private static final byte LABEL_SET_FONT = 0x03;
    private static final byte LABEL_SET_SIZE = 0x04;
    private static final byte LABEL_SET_TEXT = 0X05;

    private static final byte PROGRESSBAR_SET_RANGE = 0x03;
    private static final byte PROGRESSBAR_SET_VALUE = 0x04;
    private static final byte PROGRESSBAR_SET_DIMENSIONS = 0x05;

    private static final byte GAUGE_SET_RANGE = 0x03;
    private static final byte GAUGE_SET_VALUE = 0x04;
    private static final byte GAUGE_SET_RADIUS = 0x05;

    private static final byte BUTTON_SET_TEXT = 0x03;
    private static final byte BUTTON_SET_DIMENSIONS = 0x04;
    private static final byte BUTTON_SET_STYLE = 0x05;

    private static final byte LABEL_FONT_ARIEL_REGULAR = 0X00;
    private static final byte LABEL_FONT_ARIEL_BOLD = 0X01;
    private static final byte LABEL_FONT_ARIEL_ITALIC = 0X02;
    private static final byte LABEL_FONT_COMICSANS = 0X03;
    private static final byte LABEL_FONT_SERIF = 0X04;

    private static final byte RADIOBUTTON_SET_TEXT = 0x03;
    private static final byte RADIOBUTTON_SET_SIZE = 0x04;
    private static final byte RADIOBUTTON_SET_GROUP = 0x05;
    private static final byte RADIOBUTTON_SELECT = 0x06;

    private static final byte CHECKBOX_SET_TEXT = 0x03;
    private static final byte CHECKBOX_SET_SIZE = 0x04;
    private static final byte CHECKBOX_SELECT = 0x05;
    private static final byte CHECKBOX_UNSELECT = 0x06;

    private static final byte SLIDER_SET_RANGE = 0x03;
    private static final byte SLIDER_SET_VALUE = 0x04;
    private static final byte SLIDER_SET_DIMENSIONS = 0x05;

    // vars used in frame analysis
    private int shapeKey;
    private int shapeX, shapeY, shapeX2, shapeY2, shapeRadius, shapeRadius2, shapeWidth, shapeHeight;
    private byte shapeSize;
    private String shapeText;
    RadioGroup rg;

    public GlcdShield() {
        initializeGLcd();
    }

    public GlcdShield(Activity activity, String tag) {
        super(activity, tag);
        initializeGLcd();
    }

    private boolean isInitialized = false;

    public void initializeGLcd() {
        if (!isInitialized) {
            params = new ArrayList<>();
            params.add(WHITE);
            doOrder(ORDER_CLEAR, params);
            isInitialized = true;
        }
    }

    List<Integer> params;
    GlcdView view;

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {

        if (frame.getShieldId() == SHIELD_ID) {
            if (glcdEventHandler != null) {
                view = glcdEventHandler.getView();
            }
            switch (frame.getFunctionId()) {
                case TYPE_GLCD:
                    switch (frame.getArgument(0)[0]) {
                        case GLCD_CLEAR:
                            params = new ArrayList<>();
                            params.add(WHITE);
                            doOrder(ORDER_CLEAR, params);
                            break;
                        case GLCD_CLEAR_RECTANGLE:
                            shapeX = frame.getArgumentAsInteger(1);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(2);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeWidth = frame.getArgumentAsInteger(3);
                            if (shapeWidth < 0)
                                shapeWidth = 0;
                            else if ((shapeWidth + shapeX) > glcdWidth)
                                shapeWidth = glcdWidth - 1 - shapeX;

                            shapeHeight = frame.getArgumentAsInteger(4);
                            if (shapeHeight < 0)
                                shapeHeight = 0;
                            else if ((shapeHeight + shapeY) > glcdHeight)
                                shapeHeight = glcdHeight - 1 - shapeY;

                            // Not Implemented Yet
                            break;
                    }
                    break;
                case TYPE_POINT:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            addToShapes(new Point(shapeX, shapeY), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                    }
                    break;
                case TYPE_RECTANGLE:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeWidth = frame.getArgumentAsInteger(4);
                            if (shapeWidth < 0)
                                shapeWidth = 0;
                            else if ((shapeWidth + shapeX) > glcdWidth)
                                shapeWidth = glcdWidth - 1 - shapeX;

                            shapeHeight = frame.getArgumentAsInteger(5);
                            if (shapeHeight < 0)
                                shapeHeight = 0;
                            else if ((shapeHeight + shapeY) > glcdHeight)
                                shapeHeight = glcdHeight - 1 - shapeY;

                            shapeRadius = frame.getArgumentAsInteger(6);
                            if (shapeRadius < 0)
                                shapeRadius = 0;

                            addToShapes(new RoundRectangle(shapeX, shapeY, shapeWidth, shapeHeight, shapeRadius, false), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case RECTANGLE_SET_RADIUS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  RoundRectangle) {
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    ((RoundRectangle) tmpShape).setRadius(shapeRadius);
                                }
                            }
                            break;
                        case RECTANGLE_SET_FILL:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  RoundRectangle) {
                                    if (frame.getArgument(2)[0] == 0)
                                        ((RoundRectangle) tmpShape).setIsFill(false);
                                    else
                                        ((RoundRectangle) tmpShape).setIsFill(true);
                                }
                            }
                            break;
                        case RECTANGLE_SET_DIMENSIONS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  RoundRectangle) {
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    if (shapeWidth < 0)
                                        shapeWidth = 0;

                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    if (shapeHeight < 0)
                                        shapeHeight = 0;

                                    ((RoundRectangle) tmpShape).setWidth(shapeWidth);
                                    ((RoundRectangle) tmpShape).setHeight(shapeHeight);
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_LINE:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeX2 = frame.getArgumentAsInteger(4);
                            if (shapeX2 < 0)
                                shapeX2 = 0;
                            else if (shapeX2 > glcdWidth)
                                shapeX2 = glcdWidth - 1;

                            shapeY2 = frame.getArgumentAsInteger(5);
                            if (shapeY2 < 0)
                                shapeY2 = 0;
                            else if (shapeY2 > glcdHeight)
                                shapeY2 = glcdHeight - 1;

                            addToShapes(new Line(shapeX, shapeY, shapeX2, shapeY2), frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case LINE_SET_COORDINATES:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Line) {
                                    shapeX = frame.getArgumentAsInteger(2);
                                    if (shapeX < 0)
                                        shapeX = 0;
                                    else if (shapeX > glcdWidth)
                                        shapeX = glcdWidth - 1;

                                    shapeY = frame.getArgumentAsInteger(3);
                                    if (shapeY < 0)
                                        shapeY = 0;
                                    else if (shapeY > glcdHeight)
                                        shapeY = glcdHeight - 1;

                                    shapeX2 = frame.getArgumentAsInteger(4);
                                    if (shapeX2 < 0)
                                        shapeX2 = 0;
                                    else if (shapeX2 > glcdWidth)
                                        shapeX2 = glcdWidth - 1;

                                    shapeY2 = frame.getArgumentAsInteger(5);
                                    if (shapeY2 < 0)
                                        shapeY2 = 0;
                                    else if (shapeY2 > glcdHeight)
                                        shapeY2 = glcdHeight - 1;

                                    ((Line) tmpShape).setPoint1(shapeX, shapeY);
                                    ((Line) tmpShape).setPoint2(shapeX2, shapeY2);
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_ELLIPSE:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeRadius = frame.getArgumentAsInteger(4);
                            if (shapeRadius < 0)
                                shapeRadius = 0;

                            shapeRadius2 = frame.getArgumentAsInteger(5);
                            if (shapeRadius2 < 0)
                                shapeRadius2 = 0;

                            addToShapes(new Ellipse(shapeX, shapeY, shapeRadius, shapeRadius2, false), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case ELLIPSE_SET_RADIUS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Ellipse) {
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    if (shapeRadius < 0)
                                        shapeRadius = 0;

                                    shapeRadius2 = frame.getArgumentAsInteger(3);
                                    if (shapeRadius2 < 0)
                                        shapeRadius2 = 0;

                                    ((Ellipse) tmpShape).setRadiusX(shapeRadius);
                                    ((Ellipse) tmpShape).setRadiusY(shapeRadius2);
                                }
                            }
                            break;
                        case ELLIPSE_SET_FILL:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Ellipse) {
                                    if (frame.getArgument(2)[0] == 0)
                                        ((Ellipse) tmpShape).setIsFill(false);
                                    else
                                        ((Ellipse) tmpShape).setIsFill(true);
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_LABEL:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            shapeY = frame.getArgumentAsInteger(3);
                            shapeText = frame.getArgumentAsString(4);

                            addToShapes(new Label(shapeText, shapeX, shapeY, TEXT_SMALL, FONT_ARIEL_REGULAR), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case LABEL_SET_FONT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Label) {
                                    switch (frame.getArgument(2)[0]) {
                                        case LABEL_FONT_ARIEL_REGULAR:
                                            ((Label) tmpShape).setTextFont(FONT_ARIEL_REGULAR);
                                            break;
                                        case LABEL_FONT_ARIEL_BOLD:
                                            ((Label) tmpShape).setTextFont(FONT_ARIEL_BLACK);
                                            break;
                                        case LABEL_FONT_ARIEL_ITALIC:
                                            ((Label) tmpShape).setTextFont(FONT_ARIEL_ITALIC);
                                            break;
                                        case LABEL_FONT_COMICSANS:
                                            ((Label) tmpShape).setTextFont(FONT_COMICSANS);
                                            break;
                                        case LABEL_FONT_SERIF:
                                            ((Label) tmpShape).setTextFont(FONT_SERIF);
                                            break;
                                    }
                                }
                            }
                            break;
                        case LABEL_SET_SIZE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Label) {
                                    switch (frame.getArgument(2)[0]) {
                                        case 0x00:
                                            ((Label) tmpShape).setTextSize(TEXT_SMALL);
                                            break;
                                        case 0x01:
                                            ((Label) tmpShape).setTextSize(TEXT_MEDUIM);
                                            break;
                                        case 0x02:
                                            ((Label) tmpShape).setTextSize(TEXT_LARGE);
                                            break;
                                    }
                                }
                            }
                            break;
                        case LABEL_SET_TEXT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Label)
                                    ((Label) tmpShape).setText(frame.getArgumentAsString(2));
                            }
                            break;
                    }
                    break;
                case TYPE_PROGRESSBAR:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeWidth = frame.getArgumentAsInteger(4);
                            if (shapeWidth < 0)
                                shapeWidth = 0;
                            else if ((shapeWidth + shapeX) > glcdWidth)
                                shapeWidth = glcdWidth - 1 - shapeX;

                            shapeHeight = frame.getArgumentAsInteger(5);
                            if (shapeHeight < 0)
                                shapeHeight = 0;
                            else if ((shapeHeight + shapeY) > glcdHeight)
                                shapeHeight = glcdHeight - 1 - shapeY;

                            addToShapes(new ProgressBar(shapeX, shapeY, shapeWidth, shapeHeight), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case PROGRESSBAR_SET_RANGE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  ProgressBar) {
                                    ((ProgressBar) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((ProgressBar) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                            }
                            break;
                        case PROGRESSBAR_SET_VALUE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  ProgressBar)
                                    ((ProgressBar) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                            }
                            break;
                        case PROGRESSBAR_SET_DIMENSIONS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  ProgressBar) {
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    if (shapeWidth < 0)
                                        shapeWidth = 0;

                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    if (shapeHeight < 0)
                                        shapeHeight = 0;

                                    ((ProgressBar) tmpShape).setWidth(shapeWidth);
                                    ((ProgressBar) tmpShape).setHeight(shapeHeight);
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_GAUGE:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeRadius = frame.getArgumentAsInteger(4);
                            if (shapeRadius < 0)
                                shapeRadius = 0;

                            addToShapes(new AnalogGauge(shapeX, shapeY, shapeRadius), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                shapeX = frame.getArgumentAsInteger(2);
                                if (shapeX < 0)
                                    shapeX = 0;
                                else if (shapeX > glcdWidth)
                                    shapeX = glcdWidth - 1;

                                shapeY = frame.getArgumentAsInteger(3);
                                if (shapeY < 0)
                                    shapeY = 0;
                                else if (shapeY > glcdHeight)
                                    shapeY = glcdHeight - 1;

                                tmpShape.setPosition(shapeX, shapeY);
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (frame.getArgument(2)[0] == 0)
                                    tmpShape.setVisibility(false);
                                else
                                    tmpShape.setVisibility(true);
                            }
                            break;
                        case GAUGE_SET_RANGE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  AnalogGauge) {
                                    ((AnalogGauge) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((AnalogGauge) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                            }
                            break;
                        case GAUGE_SET_VALUE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  AnalogGauge)
                                    ((AnalogGauge) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                            }
                            break;
                        case GAUGE_SET_RADIUS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  AnalogGauge) {
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    if (shapeRadius < 0)
                                        shapeRadius = 0;

                                    ((AnalogGauge) tmpShape).setRadius(shapeRadius);
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_BUTTON:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeWidth = frame.getArgumentAsInteger(4);
                            if (shapeWidth < 0)
                                shapeWidth = 0;
                            else if ((shapeWidth + shapeX) > glcdWidth)
                                shapeWidth = glcdWidth - 1 - shapeX;

                            shapeHeight = frame.getArgumentAsInteger(5);
                            if (shapeHeight < 0)
                                shapeHeight = 0;
                            else if ((shapeHeight + shapeY) > glcdHeight)
                                shapeHeight = glcdHeight - 1 - shapeY;

                            if (frame.getArguments().size() < 7) {
                                shapeText = "Button" + String.valueOf(buttonCounter);
                                buttonCounter++;
                            } else
                                shapeText = frame.getArgumentAsString(6);

                            addToShapes(new Button(this, shapeX, shapeY, shapeWidth, shapeHeight, shapeKey, shapeText), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Button) {
                                    shapeX = frame.getArgumentAsInteger(2);
                                    if (shapeX < 0)
                                        shapeX = 0;
                                    else if (shapeX > glcdWidth)
                                        shapeX = glcdWidth - 1;

                                    shapeY = frame.getArgumentAsInteger(3);
                                    if (shapeY < 0)
                                        shapeY = 0;
                                    else if (shapeY > glcdHeight)
                                        shapeY = glcdHeight - 1;

                                    ((Button) tmpShape).clearTouch(this);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((Button) tmpShape).applyTouch(this);
                                    ((Button) tmpShape).setText(getView(), ((Button) tmpShape).getText());
                                }
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Button) {
                                    if (frame.getArgument(2)[0] == 0) {
                                        tmpShape.setVisibility(false);
                                        ((Button) tmpShape).clearTouch(this);
                                    } else {
                                        tmpShape.setVisibility(true);
                                        ((Button) tmpShape).applyTouch(this);
                                    }
                                }
                            }
                            break;
                        case BUTTON_SET_TEXT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Button) {
                                    Button btn = ((Button) tmpShape);
                                    ((Button) tmpShape).clearTouch(this);
                                    shapeText = frame.getArgumentAsString(2);
                                    btn.setText(getView(), shapeText);
                                    btn.applyTouch(this);
                                }
                            }
                            break;
                        case BUTTON_SET_DIMENSIONS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Button) {
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    if (shapeWidth < 0)
                                        shapeWidth = 0;

                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    if (shapeHeight < 0)
                                        shapeHeight = 0;

                                    ((Button) tmpShape).clearTouch(this);
                                    ((Button) tmpShape).setWidth(shapeWidth);
                                    ((Button) tmpShape).setHeight(shapeHeight);
                                    ((Button) tmpShape).applyTouch(this);
                                    ((Button) tmpShape).setText(getView(), ((Button) tmpShape).getText());
                                }
                            }
                            break;
                        case BUTTON_SET_STYLE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof  Button)
                                    ((Button) tmpShape).setStyle(frame.getArgument(2)[0]);
                            }
                            break;
                    }
                    break;
                case TYPE_RADIOBUTTON:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeSize = 0;
                            shapeText = frame.getArgumentAsString(4);
                            addToShapes(new RadioButton(this, shapeX, shapeY, shapeSize, shapeKey, shapeText), shapeKey);
                            if (frame.getArguments().size() > 5)
                                getFromRadioGroups(frame.getArgumentAsInteger(5)).add(((RadioButton) getFromShapes(frame.getArgumentAsInteger(1))));
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof RadioButton) {
                                    shapeX = frame.getArgumentAsInteger(2);
                                    if (shapeX < 0)
                                        shapeX = 0;
                                    else if (shapeX > glcdWidth)
                                        shapeX = glcdWidth - 1;

                                    shapeY = frame.getArgumentAsInteger(3);
                                    if (shapeY < 0)
                                        shapeY = 0;
                                    else if (shapeY > glcdHeight)
                                        shapeY = glcdHeight - 1;

                                    ((RadioButton) tmpShape).clearTouch(this);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((RadioButton) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof RadioButton) {
                                    if (frame.getArgument(2)[0] == 0) {
                                        tmpShape.setVisibility(false);
                                        ((RadioButton) tmpShape).clearTouch(this);
                                    } else {
                                        tmpShape.setVisibility(true);
                                        ((RadioButton) tmpShape).applyTouch(this);
                                    }
                                }
                            }
                            break;
                        case RADIOBUTTON_SET_TEXT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof RadioButton) {
                                    shapeText = frame.getArgumentAsString(2);
                                    ((RadioButton) tmpShape).clearTouch(this);
                                    ((RadioButton) tmpShape).setText(getView(), shapeText);
                                    ((RadioButton) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case RADIOBUTTON_SET_SIZE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                ((RadioButton) tmpShape).clearTouch(this);
                                ((RadioButton) tmpShape).setSize(frame.getArgument(2)[0]);
                                ((RadioButton) tmpShape).applyTouch(this);
                            }
                            break;
                        case RADIOBUTTON_SET_GROUP:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof RadioButton) {
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null) {
                                        if (((RadioButton) tmpShape).getSelected())
                                            rg.reset();
                                        rg.remove(((RadioButton) tmpShape));
                                    }
                                    getFromRadioGroups(frame.getArgumentAsInteger(2)).add(((RadioButton) tmpShape));
                                }
                            }
                            break;
                        case RADIOBUTTON_SELECT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof RadioButton) {
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null)
                                        ((RadioButton) tmpShape).getRadioGroup().select(((RadioButton) tmpShape));
                                }
                            }
                            break;
                    }
                    break;
                case TYPE_CHECKBOX:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeSize = 0;
                            shapeText = frame.getArgumentAsString(4);
                            addToShapes(new CheckBox(this, shapeX, shapeY, shapeSize, shapeKey, shapeText), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox) {
                                    shapeX = frame.getArgumentAsInteger(2);
                                    if (shapeX < 0)
                                        shapeX = 0;
                                    else if (shapeX > glcdWidth)
                                        shapeX = glcdWidth - 1;

                                    shapeY = frame.getArgumentAsInteger(3);
                                    if (shapeY < 0)
                                        shapeY = 0;
                                    else if (shapeY > glcdHeight)
                                        shapeY = glcdHeight - 1;

                                    ((CheckBox) tmpShape).clearTouch(this);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((CheckBox) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox) {
                                    if (frame.getArgument(2)[0] == 0) {
                                        tmpShape.setVisibility(false);
                                        ((CheckBox) tmpShape).clearTouch(this);
                                    } else {
                                        tmpShape.setVisibility(true);
                                        ((CheckBox) tmpShape).applyTouch(this);
                                    }
                                }
                            }
                            break;
                        case CHECKBOX_SET_TEXT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox) {
                                    shapeText = frame.getArgumentAsString(2);
                                    ((CheckBox) tmpShape).clearTouch(this);
                                    ((CheckBox) tmpShape).setText(getView(), shapeText);
                                    ((CheckBox) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case CHECKBOX_SET_SIZE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox) {
                                    shapeSize = frame.getArgument(2)[0];
                                    ((CheckBox) tmpShape).clearTouch(this);
                                    ((CheckBox) tmpShape).setSize(getView(), shapeSize);
                                    ((CheckBox) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case CHECKBOX_SELECT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox)
                                    ((CheckBox) tmpShape).setSelected(true);
                            }
                            break;
                        case CHECKBOX_UNSELECT:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof CheckBox)
                                    ((CheckBox) tmpShape).setSelected(false);
                            }
                            break;
                    }
                    break;
                case TYPE_SLIDER:
                    switch (frame.getArgument(0)[0]) {
                        case SHAPE_DRAW:
                            shapeKey = frame.getArgumentAsInteger(1);
                            shapeX = frame.getArgumentAsInteger(2);
                            if (shapeX < 0)
                                shapeX = 0;
                            else if (shapeX > glcdWidth)
                                shapeX = glcdWidth - 1;

                            shapeY = frame.getArgumentAsInteger(3);
                            if (shapeY < 0)
                                shapeY = 0;
                            else if (shapeY > glcdHeight)
                                shapeY = glcdHeight - 1;

                            shapeWidth = frame.getArgumentAsInteger(4);
                            if (shapeWidth < 0)
                                shapeWidth = 0;
                            else if ((shapeWidth + shapeX) > glcdWidth)
                                shapeWidth = glcdWidth - 1 - shapeX;

                            shapeHeight = frame.getArgumentAsInteger(5);
                            if (shapeHeight < 0)
                                shapeHeight = 0;
                            else if ((shapeHeight + shapeY) > glcdHeight)
                                shapeHeight = glcdHeight - 1 - shapeY;

                            addToShapes(new Slider(this, shapeX, shapeY, shapeWidth, shapeHeight, shapeKey), shapeKey);
                            break;
                        case SHAPE_SET_POSTION:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Slider) {
                                    shapeX = frame.getArgumentAsInteger(2);
                                    if (shapeX < 0)
                                        shapeX = 0;
                                    else if (shapeX > glcdWidth)
                                        shapeX = glcdWidth - 1;

                                    shapeY = frame.getArgumentAsInteger(3);
                                    if (shapeY < 0)
                                        shapeY = 0;
                                    else if (shapeY > glcdHeight)
                                        shapeY = glcdHeight - 1;

                                    ((Slider) tmpShape).clearTouch(this);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((Slider) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                        case SHAPE_SET_VISIBILITY:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Slider) {
                                    if (frame.getArgument(2)[0] == 0) {
                                        tmpShape.setVisibility(false);
                                        ((Slider) tmpShape).clearTouch(this);
                                    } else {
                                        tmpShape.setVisibility(true);
                                        ((Slider) tmpShape).applyTouch(this);
                                    }
                                }
                            }
                            break;
                        case SLIDER_SET_RANGE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Slider) {
                                    ((Slider) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((Slider) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                            }
                            break;
                        case SLIDER_SET_VALUE:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Slider)
                                    ((Slider) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                            }
                            break;
                        case SLIDER_SET_DIMENSIONS:
                            shapeKey = frame.getArgumentAsInteger(1);
                            tmpShape = getFromShapes(shapeKey);
                            if (tmpShape != null) {
                                if (tmpShape instanceof Slider) {
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    if (shapeWidth < 0)
                                        shapeWidth = 0;

                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    if (shapeHeight < 0)
                                        shapeHeight = 0;

                                    ((Slider) tmpShape).clearTouch(this);
                                    ((Slider) tmpShape).setWidth(shapeWidth);
                                    ((Slider) tmpShape).setHeight(shapeHeight);
                                    ((Slider) tmpShape).applyTouch(this);
                                }
                            }
                            break;
                    }
                    break;
            }
            if (glcdEventHandler != null)
                glcdEventHandler.setView(getView());
        }

    }

    public int getShapesSize() {
        if (shapes != null)
            return this.shapes.size();
        else
            return 0;
    }

    public Shape getFromShapesByIndex(int index) {
        if (shapes != null)
            if (shapes.size() > index)
                return shapes.valueAt(index);
        return null;
    }

    public Shape getFromShapes(int key) {
        if (shapes != null)
            if (shapes.indexOfKey(key) > -1)
                if (shapes.size() > 0)
                    return shapes.get(key);
        return null;
    }


    public void addToShapes(Shape shape, int key) {
        shapes.put(key, shape);
    }

    public void addToRadioGroups(RadioGroup group, int key) {
        radioGroups.append(key, group);
    }

    public RadioGroup getFromRadioGroups(int key) {
        if (radioGroups.indexOfKey(key) <= -1)
            addToRadioGroups(new RadioGroup(), key);
        return radioGroups.get(key);
    }

    public void setEventHandler(GlcdEventHandler glcdEventHandler) {
        this.glcdEventHandler = glcdEventHandler;
    }

    public GlcdEventHandler getGlcdEventHandler() {
        return glcdEventHandler;
    }

    public static interface GlcdEventHandler {
        void setView(GlcdView glcdView);

        GlcdView getView();
    }

    @Override
    public void reset() {

    }

    public GlcdView getView() {
        if (view == null)
            view = new GlcdView(getApplication().getApplicationContext(), glcdWidth, glcdHeight, getTag());
        return view;
    }

    ShieldFrame frame;

    public void sendTouch(byte shapeType, int key, byte state) {
        frame = new ShieldFrame(SHIELD_ID, shapeType);
        frame.addArgument((byte) 0x01);
        frame.addArgument(2, key);
        frame.addArgument(state);
        sendShieldFrame(frame, false);
    }

    public void sendTouch(byte shapeType, int key, byte state, int value) {
        frame = new ShieldFrame(SHIELD_ID, shapeType);
        frame.addArgument((byte) 0x01);
        frame.addArgument(2, key);
        frame.addArgument(2, value);
        sendShieldFrame(frame, false);
    }

    boolean sendFrame = false;

    public synchronized boolean doOrder(int order, List<Integer> params) {
        Integer BgColor = WHITE, key = 0, action = 0, touchId = 0, startX = 0, startY = 0, finalX = 0, finalY = 0;

        switch (order) {
            case ORDER_SETTOUCH:
                if (params.size() < 3)
                    return false;
                startX = params.get(0);
                startY = params.get(1);
                touchId = params.get(2);
                if (startX < touchs.size() && startX >= 0)
                    if (startY < touchs.get(startX).size() && startY >= 0)
                        touchs.get(startX).setValueAt(startY, touchId);
                break;
            case ORDER_CLEAR:
                if (params.size() < 0)
                    return false;
                buttonCounter = 0;
                shapes = new SparseArray<>();
                radioGroups = new SparseArray<>();
                touchs = new SparseArray<>();
                for (int x = 0; x < glcdWidth; x++) {
                    SparseArray<Integer> tempTouchs = new SparseArray<>();
                    for (int y = 0; y < glcdHeight; y++) {
                        tempTouchs.append(y, null);
                    }
                    touchs.append(x, tempTouchs);
                }
                break;
            case ORDER_HANDLETOUCH:
                if (params.size() < 2)
                    return false;

                action = params.get(0);
                startX = params.get(1);
                startY = params.get(2);

                if (touchs.size() > 0) {
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // press
                            if (currentPressedKey != null)
                                if (shapes.indexOfKey(currentPressedKey) > -1)
                                    shapes.get(currentPressedKey).setIsPressed(false);

                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setIsPressed(true);
                                currentPressedKey = key;

                                if (sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Button.class.toString()))
                                        sendTouch(SHAPE_BUTTON, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(CheckBox.class.toString()))
                                        sendTouch(SHAPE_CHECKBOX, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(RadioButton.class.toString()))
                                        sendTouch(SHAPE_RADIOBUTTON, key, STATE_PRESSED);
                                    else if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        sendTouch(SHAPE_SLIDER, key, STATE_RELEASED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }

                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            //release
                            if (currentPressedKey != null)
                                if (shapes.indexOfKey(currentPressedKey) > -1)
                                    shapes.get(currentPressedKey).setIsPressed(false);

                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setIsPressed(false);

                                if (sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Button.class.toString()))
                                        sendTouch(SHAPE_BUTTON, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(CheckBox.class.toString()))
                                        sendTouch(SHAPE_CHECKBOX, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(RadioButton.class.toString()))
                                        sendTouch(SHAPE_RADIOBUTTON, key, STATE_RELEASED);
                                    else if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        sendTouch(SHAPE_SLIDER, key, STATE_RELEASED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // touch
                            key = touchs.get(startX).get(startY);
                            if (key != null) {
                                sendFrame = shapes.get(key).setTouched(startX, startY);
                                if (sendFrame) {
                                    if (shapes.get(key).getClass().toString().equals(Slider.class.toString()))
                                        sendTouch(SHAPE_SLIDER, key, STATE_TOUCHED, (int) ((Slider) shapes.get(key)).getCurrentValue());
                                }
                            } else {
                                if (currentPressedKey != null)
                                    if (shapes.indexOfKey(currentPressedKey) > -1)
                                        shapes.get(currentPressedKey).setIsPressed(false);
                            }
                            break;
                        default:
                            if (currentPressedKey != null)
                                if (shapes.indexOfKey(currentPressedKey) > -1)
                                    shapes.get(currentPressedKey).setIsPressed(false);
                            break;
                    }
                }
                break;
            case ORDER_APPLYTOUCH:
                if (params.size() < 0)
                    return false;
                touchId = 0;
                startX = 0;
                startY = 0;
                finalX = 0;
                finalY = 0;
                if (params.size() > 4) {
                    startX = params.get(0);
                    if (startX < 0)
                        startX = 0;
                    else if (startX > glcdWidth)
                        startX = glcdWidth - 1;

                    startY = params.get(1);
                    if (startY < 0)
                        startY = 0;
                    else if (startY > glcdHeight)
                        startY = glcdHeight - 1;

                    finalX = params.get(2);
                    if (finalX < 0)
                        finalX = 0;
                    else if (finalX > glcdWidth)
                        finalX = glcdWidth - 1;

                    finalY = params.get(3);
                    if (finalY < 0)
                        finalY = 0;
                    else if (finalY > glcdHeight)
                        finalY = glcdHeight - 1;

                    touchId = params.get(4);
                } else {
                    return false;
                }
                if (touchs.size() > 0) {
                    for (int x = startX; x < finalX; x++) {
                        if (touchs.get(x).size() > 0) {
                            for (int y = startY; y < finalY; y++) {
                                touchs.get(x).setValueAt(y, touchId);
                            }
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
