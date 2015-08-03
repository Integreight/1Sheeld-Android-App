package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
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
 * Created by Mouso on 6/7/2015.
 */
public class GlcdShield extends ControllerParent<GlcdShield>{

    private static final byte SHIELD_ID = UIShield.GLCD_SHIELD.getId();
    private GlcdEventHandler glcdEventHandler;
    private Shape tmpShape = null;
//    private GlcdView glcdView;

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

    private static final byte PROGRESSBAR_SET_RANGE = 0x03;
    private static final byte PROGRESSBAR_SET_VALUE = 0x04;
    private static final byte PROGRESSBAR_SET_DIMENSIONS = 0x05;

    private static final byte GAUGE_SET_RANGE = 0x03;
    private static final byte GAUGE_SET_VALUE = 0x04;
    private static final byte GAUGE_SET_RADIUS = 0x05;

    private static final byte BUTTON_SET_TEXT = 0x03;
    private static final byte BUTTON_SET_DIMENSIONS = 0x04;
    private static final byte BUTTON_SET_STYLE = 0x05;


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
    // TODO
    private int shapeKey;
    private int shapeX, shapeY,shapeX2,shapeY2, shapeRadius , shapeRadius2, shapeWidth, shapeHeight;
    private byte shapeSize;
    private String shapeText;

    public GlcdShield(){}

    public GlcdShield(Activity activity, String tag) {
        super(activity, tag);
    }

//    public GlcdView getGlcdView() {
//        return glcdView;
//    }

    @Override
    public ControllerParent<GlcdShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        selectionAction.onSuccess();
        return super.invalidate(selectionAction, isToastable);
    }
    RadioGroup rg;
    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (glcdEventHandler != null) {
            //glcdEventHandler.setView(new GlcdView(getActivity()));
            List<Integer> params;
            List<Boolean> premissions;
            if (frame.getShieldId() == SHIELD_ID) {
                GlcdView view = glcdEventHandler.getView();
                view.setGlcdViewEventListener(glcdViewEventListener);
                switch (frame.getFunctionId()) {
                    case TYPE_GLCD:
                        switch (frame.getArgument(0)[0]) {
                            case GLCD_CLEAR:
//                                view.clear(view.WHITE);
                                params = new ArrayList<>();
                                params.add(view.WHITE);
                                premissions= new ArrayList<>();
                                premissions.add(true);
                                premissions.add(true);
                                premissions.add(true);
                                premissions.add(true);
                                view.doOrder(view.ORDER_CLEAR, params, premissions);
                                break;
                            case GLCD_CLEAR_RECTANGLE:
                                shapeX = frame.getArgumentAsInteger(1);
                                shapeY = frame.getArgumentAsInteger(2);
                                shapeWidth = frame.getArgumentAsInteger(3);
                                shapeHeight = frame.getArgumentAsInteger(4);
//                                view.clear(view.WHITE, frame.getArgumentAsInteger(1), frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4));
                                params = new ArrayList<>();
                                params.add(view.WHITE);
                                params.add(shapeX);
                                params.add(shapeY);
                                params.add(shapeWidth);
                                params.add(shapeHeight);
                                premissions= new ArrayList<>();
                                premissions.add(true);
                                premissions.add(true);
                                premissions.add(true);
                                premissions.add(null);
                                view.doOrder(view.ORDER_CLEAR, params, premissions);
                                break;
                        }
                        break;
                    case TYPE_POINT:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                view.addToShapes(new Point(shapeX, shapeY), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY =  frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX,shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                        }
                        break;
                    case TYPE_RECTANGLE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeWidth = frame.getArgumentAsInteger(4);
                                shapeHeight =  frame.getArgumentAsInteger(5);
                                shapeRadius = frame.getArgumentAsInteger(6);
                                view.addToShapes(new RoundRectangle(shapeX, shapeY,shapeWidth ,shapeHeight, shapeRadius, false), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                                    else
                                        view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                                }
                                break;
                            case RECTANGLE_SET_RADIUS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    ((RoundRectangle) tmpShape).setRadius(shapeRadius);
                                }
                                break;
                            case RECTANGLE_SET_FILL:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        ((RoundRectangle) tmpShape).setIsFill(false);
                                    else
                                        ((RoundRectangle) tmpShape).setIsFill(true);
                                }
                                break;
                            case RECTANGLE_SET_DIMENSIONS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    ((RoundRectangle) tmpShape).setWidth(shapeWidth);
                                    ((RoundRectangle) tmpShape).setHeight(shapeHeight);
                                }
                                break;
                        }
                        break;
                    case TYPE_LINE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeX2 = frame.getArgumentAsInteger(4);
                                shapeY2 = frame.getArgumentAsInteger(5);
                                view.addToShapes(new Line(shapeX, shapeY, shapeX2, shapeY2), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case LINE_SET_COORDINATES:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    ((Line) tmpShape).setPoint1(shapeX,shapeY);
                                }
                                break;
                        }
                        break;
                    case TYPE_ELLIPSE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeRadius = frame.getArgumentAsInteger(4);
                                shapeRadius2 = frame.getArgumentAsInteger(5);
                                view.addToShapes(new Ellipse(shapeX, shapeY, shapeRadius, shapeRadius2, false), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case ELLIPSE_SET_RADIUS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    shapeRadius2 = frame.getArgumentAsInteger(3);
                                    ((Ellipse) tmpShape).setRadiusX(shapeRadius);
                                    ((Ellipse) tmpShape).setRadiusX(shapeRadius2);
                                }
                                break;
                            case ELLIPSE_SET_FILL:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        ((Ellipse) tmpShape).setIsFill(false);
                                    else
                                        ((Ellipse) tmpShape).setIsFill(true);
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
                                view.addToShapes(new Label(shapeText, shapeX, shapeY, GlcdView.TEXT_SMALL, GlcdView.FONT_ARIEL_REGULAR), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case LABEL_SET_FONT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    switch (frame.getArgumentAsInteger(2)) {
                                        case GlcdView.FONT_ARIEL_REGULAR:
                                            ((Label) tmpShape).setTextFont(GlcdView.FONT_ARIEL_REGULAR);
                                            break;
                                        case GlcdView.FONT_ARIEL_BLACK:
                                            ((Label) tmpShape).setTextFont(GlcdView.FONT_ARIEL_BLACK);
                                            break;
                                        case GlcdView.FONT_ARIEL_ITALIC:
                                            ((Label) tmpShape).setTextFont(GlcdView.FONT_ARIEL_ITALIC);
                                            break;
                                        case GlcdView.FONT_COMICSANS:
                                            ((Label) tmpShape).setTextFont(GlcdView.FONT_COMICSANS);
                                            break;
                                        case GlcdView.FONT_SERIF:
                                            ((Label) tmpShape).setTextFont(GlcdView.FONT_SERIF);
                                            break;
                                    }
                                }
                                break;
                            case LABEL_SET_SIZE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    switch (frame.getArgument(2)[0]) {
                                        case 0x00:
                                            ((Label) tmpShape).setTextSize(GlcdView.TEXT_SMALL);
                                            break;
                                        case 0x01:
                                            ((Label) tmpShape).setTextSize(GlcdView.TEXT_MEDUIM);
                                            break;
                                        case 0x02:
                                            ((Label) tmpShape).setTextSize(GlcdView.TEXT_LARGE);
                                            break;
                                    }
                                }
                                break;
                        }
                        break;
                    case TYPE_PROGRESSBAR:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeWidth = frame.getArgumentAsInteger(4);
                                shapeHeight =  frame.getArgumentAsInteger(5);
                                view.addToShapes(new ProgressBar(shapeX, shapeY, shapeWidth,shapeHeight), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX,shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case PROGRESSBAR_SET_RANGE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    ((ProgressBar) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((ProgressBar) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case PROGRESSBAR_SET_VALUE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    ((ProgressBar) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                }
                                break;
                            case PROGRESSBAR_SET_DIMENSIONS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    ((ProgressBar) tmpShape).setWidth(shapeWidth);
                                    ((ProgressBar) tmpShape).setHeight(shapeHeight);
                                }
                                break;
                        }
                        break;
                    case TYPE_GAUGE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                view.addToShapes(new AnalogGauge(shapeX, shapeY, frame.getArgumentAsInteger(4)), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case GAUGE_SET_RANGE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    ((AnalogGauge) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((AnalogGauge) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case GAUGE_SET_VALUE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    ((AnalogGauge) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                }
                                break;
                            case GAUGE_SET_RADIUS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    shapeRadius = frame.getArgumentAsInteger(2);
                                    ((AnalogGauge) tmpShape).setRadius(shapeRadius);
                                }
                                break;
                        }
                        break;
                    case TYPE_BUTTON:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeWidth = frame.getArgumentAsInteger(4);
                                shapeHeight = frame.getArgumentAsInteger(5);
                                if (frame.getArguments().size() < 7)
                                    shapeText = "..";
                                else
                                    shapeText = frame.getArgumentAsString(6);
                                view.addToShapes(new Button(view, shapeX, shapeY, shapeWidth, shapeHeight, shapeKey, shapeText), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((Button) tmpShape).applyTouch(view);
                                    ((Button) tmpShape).setText(view,((Button) tmpShape).getText());
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case BUTTON_SET_TEXT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,true);
                                    Button btn = ((Button) tmpShape);
                                    shapeText = frame.getArgumentAsString(2);
                                    btn.setText(view, shapeText);
                                    btn.applyTouch(view);
                                }
                                break;
                            case BUTTON_SET_DIMENSIONS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,true);
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    ((Button) tmpShape).setWidth(shapeWidth);
                                    ((Button) tmpShape).setHeight(shapeHeight);
                                    ((Button) tmpShape).applyTouch(view);
                                    ((Button) tmpShape).setText(view, ((Button) tmpShape).getText());
                                }
                                break;
                            case BUTTON_SET_STYLE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
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
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeSize = 0;
                                shapeText = frame.getArgumentAsString(4);
                                view.addToShapes(new RadioButton(view, shapeX, shapeY , shapeSize, shapeKey, shapeText), shapeKey);
                                //view.getFromRadioGroups(0).add(((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))));
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((RadioButton) tmpShape).applyTouch(view);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case RADIOBUTTON_SET_TEXT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeText = frame.getArgumentAsString(2);
                                    ((RadioButton) tmpShape).setText(view, shapeText);
                                    ((RadioButton) tmpShape).applyTouch(view);
                                }
                                break;
                            case RADIOBUTTON_SET_SIZE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,true);
                                    ((RadioButton) tmpShape).setSize(frame.getArgument(2)[0]);
                                    ((RadioButton) tmpShape).applyTouch(view);
                                }
                                break;
                            case RADIOBUTTON_SET_GROUP:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    //tmpShape.clearDraw(view,false,false);
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null)
                                        rg.remove(((RadioButton) tmpShape));
                                    view.getFromRadioGroups(frame.getArgumentAsInteger(2)).add(((RadioButton) tmpShape));
                                    ((RadioButton) tmpShape).applyTouch(view);
                                }
                                break;
                            case RADIOBUTTON_SELECT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null)
                                        ((RadioButton) tmpShape).getRadioGroup().select(((RadioButton) tmpShape));
                                }
                                break;
                        }
                        break;
                    case TYPE_CHECKBOX:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey = frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY =  frame.getArgumentAsInteger(3);
                                shapeSize = 0;
                                shapeText = frame.getArgumentAsString(4);
                                view.addToShapes(new CheckBox(view, shapeX, shapeY, shapeSize, shapeKey, shapeText), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((CheckBox) tmpShape).applyTouch(view);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case CHECKBOX_SET_TEXT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeText = frame.getArgumentAsString(2);
                                    ((CheckBox) tmpShape).setText(view, shapeText);
                                    ((CheckBox) tmpShape).applyTouch(view);
                                }
                                break;
                            case CHECKBOX_SET_SIZE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeSize = frame.getArgument(2)[0];
                                    ((CheckBox) tmpShape).setSize(view, shapeSize);
                                    ((CheckBox) tmpShape).applyTouch(view);
                                }
                                break;
                            case CHECKBOX_SELECT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    //tmpShape.clearDraw(view,false,false);
                                    ((CheckBox) tmpShape).setSelected(true);
                                }
                                break;
                            case CHECKBOX_UNSELECT:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    //tmpShape.clearDraw(view,false,false);
                                    ((CheckBox) tmpShape).setSelected(false);
                                }
                                break;
                        }
                        break;
                    case TYPE_SLIDER:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                shapeKey =  frame.getArgumentAsInteger(1);
                                shapeX = frame.getArgumentAsInteger(2);
                                shapeY = frame.getArgumentAsInteger(3);
                                shapeWidth = frame.getArgumentAsInteger(4);
                                shapeHeight = frame.getArgumentAsInteger(5);
                                view.addToShapes(new Slider(view, shapeX, shapeY, shapeWidth, shapeHeight, shapeKey), shapeKey);
                                break;
                            case SHAPE_SET_POSTION:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,true);
                                    shapeX = frame.getArgumentAsInteger(2);
                                    shapeY = frame.getArgumentAsInteger(3);
                                    tmpShape.setPosition(shapeX, shapeY);
                                    ((Slider) tmpShape).applyTouch(view);
                                }
                                break;
                            case SHAPE_SET_VISIBILITY:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case SLIDER_SET_RANGE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,false);
                                    ((Slider) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((Slider) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case SLIDER_SET_VALUE:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null){
                                    tmpShape.clearDraw(view,true,false);
                                    ((Slider) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                }
                                break;
                            case SLIDER_SET_DIMENSIONS:
                                shapeKey = frame.getArgumentAsInteger(1);
                                tmpShape = view.getFromShapes(shapeKey);
                                if(tmpShape != null) {
                                    tmpShape.clearDraw(view,true,true);
                                    shapeWidth = frame.getArgumentAsInteger(2);
                                    shapeHeight = frame.getArgumentAsInteger(3);
                                    ((Slider) tmpShape).setWidth(shapeWidth);
                                    ((Slider) tmpShape).setHeight(shapeHeight);
                                    ((Slider) tmpShape).applyTouch(view);
                                }
                                break;
                        }
                        break;
                }
                glcdEventHandler.setView(view);
            }
        }
    }

    public void doClear(){
        /*if (glcdView == null) {
            glcdView = new GlcdView(activity);
            glcdView.invalidate();
        }
        if (glcdEventHandler != null){
            glcdEventHandler.setView(glcdView.getMbitmap());
        }*/
    }

    public void setEventHandler(GlcdEventHandler glcdEventHandler) {
        this.glcdEventHandler = glcdEventHandler;
    }

    public GlcdEventHandler getGlcdEventHandler() {
        return glcdEventHandler;
    }

    public static interface GlcdEventHandler{
        void setView(GlcdView glcdView);
        GlcdView getView();
    }

    @Override
    public void reset() {

    }

    ShieldFrame frame;

    GlcdView.GlcdViewEventListener glcdViewEventListener = new GlcdView.GlcdViewEventListener() {
        @Override
        public void sendTouch(byte shapeType, int key, byte state) {
            frame = new ShieldFrame(SHIELD_ID,shapeType);
            frame.addByteArgument((byte) 0x01);
            frame.addIntegerArgument(2, key);
            frame.addByteArgument(state);
            sendShieldFrame(frame,true);
        }

        @Override
        public void sendTouch(byte shapeType, int key, byte state, int value) {
            frame = new ShieldFrame(SHIELD_ID,shapeType);
            frame.addByteArgument((byte) 0x01);
            frame.addIntegerArgument(2, key);
            frame.addIntegerArgument(2,value);
            sendShieldFrame(frame, true);
        }
    };
}
