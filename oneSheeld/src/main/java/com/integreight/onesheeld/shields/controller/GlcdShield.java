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
//                                view.clear(view.WHITE, frame.getArgumentAsInteger(1), frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4));
                                params = new ArrayList<>();
                                params.add(view.WHITE);
                                params.add(frame.getArgumentAsInteger(1));
                                params.add(frame.getArgumentAsInteger(2));
                                params.add(frame.getArgumentAsInteger(3));
                                params.add(frame.getArgumentAsInteger(4));
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
                                view.addToShapes(new Point(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
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
                                view.addToShapes(new RoundRectangle(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), frame.getArgumentAsInteger(6), false), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                                    else
                                        view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                                }
                                break;
                            case RECTANGLE_SET_RADIUS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((RoundRectangle) tmpShape).setRadius(frame.getArgumentAsInteger(2));
                                break;
                            case RECTANGLE_SET_FILL:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        ((RoundRectangle) tmpShape).setIsFill(false);
                                    else
                                        ((RoundRectangle) tmpShape).setIsFill(true);
                                }
                                break;
                            case RECTANGLE_SET_DIMENSIONS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((RoundRectangle) tmpShape).setWidth(frame.getArgumentAsInteger(2));
                                    ((RoundRectangle) tmpShape).setHeight(frame.getArgumentAsInteger(3));
                                }
                                break;
                        }
                        break;
                    case TYPE_LINE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new Line(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case LINE_SET_COORDINATES:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((Line) tmpShape).setPoint1(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                        }
                        break;
                    case TYPE_ELLIPSE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new Ellipse(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), false), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case ELLIPSE_SET_RADIUS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((Ellipse) tmpShape).setRadiusX(frame.getArgumentAsInteger(2));
                                    ((Ellipse) tmpShape).setRadiusX(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case ELLIPSE_SET_FILL:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
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
                                view.addToShapes(new Label(frame.getArgumentAsString(4), frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), GlcdView.TEXT_SMALL, GlcdView.FONT_ARIEL_REGULAR), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case LABEL_SET_FONT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
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
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    switch (frame.getArgumentAsInteger(2)) {
                                        case 0:
                                            ((Label) tmpShape).setTextSize(GlcdView.TEXT_SMALL);
                                            break;
                                        case 1:
                                            ((Label) tmpShape).setTextSize(GlcdView.TEXT_MEDUIM);
                                            break;
                                        case 2:
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
                                view.addToShapes(new ProgressBar(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case PROGRESSBAR_SET_RANGE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((ProgressBar) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((ProgressBar) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case PROGRESSBAR_SET_VALUE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((ProgressBar) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                break;
                            case PROGRESSBAR_SET_DIMENSIONS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((ProgressBar) tmpShape).setWidth(frame.getArgumentAsInteger(2));
                                    ((ProgressBar) tmpShape).setHeight(frame.getArgumentAsInteger(3));
                                }
                                break;
                        }
                        break;
                    case TYPE_GAUGE:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new AnalogGauge(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case GAUGE_SET_RANGE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((AnalogGauge) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((AnalogGauge) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case GAUGE_SET_VALUE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((AnalogGauge) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                break;
                            case GAUGE_SET_RADIUS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((AnalogGauge) tmpShape).setRadius(frame.getArgumentAsInteger(2));
                                break;
                        }
                        break;
                    case TYPE_BUTTON:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                if (frame.getArguments().size() < 7)
                                    view.addToShapes(new Button(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), frame.getArgumentAsInteger(1), " "), frame.getArgumentAsInteger(1));
                                else
                                    view.addToShapes(new Button(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), frame.getArgumentAsInteger(1), frame.getArgumentAsString(6)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                tmpShape.clearDraw(view);
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case BUTTON_SET_TEXT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    Button btn = ((Button) tmpShape);
                                    String btnTxt = frame.getArgumentAsString(2);
                                    btn.setText(view, btnTxt);
                                }
                                break;
                            case BUTTON_SET_DIMENSIONS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((Button) tmpShape).setWidth(frame.getArgumentAsInteger(2));
                                    ((Button) tmpShape).setHeight(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case BUTTON_SET_STYLE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((Button) tmpShape).setStyle(frame.getArgument(2)[0]);
                                break;
                        }
                        break;
                    case TYPE_RADIOBUTTON:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new RadioButton(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), (byte) 0, frame.getArgumentAsInteger(1), frame.getArgumentAsString(4)), frame.getArgumentAsInteger(1));
                                //view.getFromRadioGroups(0).add(((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case RADIOBUTTON_SET_TEXT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((RadioButton) tmpShape).setText(view, frame.getArgumentAsString(2));
                                break;
                            case RADIOBUTTON_SET_SIZE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((RadioButton) tmpShape).setSize(frame.getArgument(2)[0]);
                                break;
                            case RADIOBUTTON_SET_GROUP:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null)
                                        rg.remove(((RadioButton) tmpShape));
                                    view.getFromRadioGroups(frame.getArgumentAsInteger(2)).add(((RadioButton) tmpShape));
                                }
                                break;
                            case RADIOBUTTON_SELECT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    rg = ((RadioButton) tmpShape).getRadioGroup();
                                    if (rg != null)
                                        ((RadioButton) tmpShape).getRadioGroup().select(((RadioButton) tmpShape));
                                break;
                        }
                        break;
                    case TYPE_CHECKBOX:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new CheckBox(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), (byte) 0, frame.getArgumentAsInteger(1), frame.getArgumentAsString(4)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case CHECKBOX_SET_TEXT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((CheckBox) tmpShape).setText(view, frame.getArgumentAsString(2));
                                break;
                            case CHECKBOX_SET_SIZE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((CheckBox) tmpShape).setSize(view, frame.getArgument(2)[0]);
                                break;
                            case CHECKBOX_SELECT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((CheckBox) tmpShape).setSelected(true);
                                break;
                            case CHECKBOX_UNSELECT:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((CheckBox) tmpShape).setSelected(false);
                                break;
                        }
                        break;
                    case TYPE_SLIDER:
                        switch (frame.getArgument(0)[0]) {
                            case SHAPE_DRAW:
                                view.addToShapes(new Slider(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), frame.getArgumentAsInteger(1)), frame.getArgumentAsInteger(1));
                                break;
                            case SHAPE_SET_POSTION:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    tmpShape.setPosition(frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3));
                                break;
                            case SHAPE_SET_VISIBILITY:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    if (frame.getArgument(2)[0] == 0)
                                        tmpShape.setVisibility(true);
                                    else
                                        tmpShape.setVisibility(false);
                                }
                                break;
                            case SLIDER_SET_RANGE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((Slider) tmpShape).setStart(frame.getArgumentAsInteger(2));
                                    ((Slider) tmpShape).setEnd(frame.getArgumentAsInteger(3));
                                }
                                break;
                            case SLIDER_SET_VALUE:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null)
                                    ((Slider) tmpShape).setCurrentValue(frame.getArgumentAsInteger(2));
                                break;
                            case SLIDER_SET_DIMENSIONS:
                                tmpShape = view.getFromShapes(frame.getArgumentAsInteger(1));
                                if(tmpShape != null) {
                                    ((Slider) tmpShape).setWidth(frame.getArgumentAsInteger(2));
                                    ((Slider) tmpShape).setHeight(frame.getArgumentAsInteger(3));
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
