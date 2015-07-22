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
import com.integreight.onesheeld.shields.controller.utils.glcd.RoundRectangle;
import com.integreight.onesheeld.shields.controller.utils.glcd.Slider;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdShield extends ControllerParent<GlcdShield>{

    private static final byte SHIELD_ID = UIShield.GLCD_SHIELD.getId();
    private GlcdEventHandler glcdEventHandler;
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

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == SHIELD_ID) {
            GlcdView view = glcdEventHandler.getView();
            switch (frame.getFunctionId()){
                case TYPE_GLCD:
                    switch (frame.getArgument(0)[0]){
                        case GLCD_CLEAR:
                            view.clear(view.WHITE);
                            break;
                        case GLCD_CLEAR_RECTANGLE:
                            view.clear(view.WHITE,frame.getArgumentAsInteger(1),frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4));
                            break;
                    }
                    break;
                case TYPE_POINT:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Point(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3)),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                    }
                    break;
                case TYPE_RECTANGLE:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new RoundRectangle(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4),frame.getArgumentAsInteger(5),frame.getArgumentAsInteger(6),false),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case RECTANGLE_SET_RADIUS:
                            ((RoundRectangle) view.getFromShapes(frame.getArgumentAsInteger(1))).setRadius(frame.getArgumentAsInteger(2));
                            break;
                        case RECTANGLE_SET_FILL:
                            if (frame.getArgument(2)[0] == 0)
                                ((RoundRectangle) view.getFromShapes(frame.getArgumentAsInteger(1))).setIsFill(false);
                            else
                                ((RoundRectangle) view.getFromShapes(frame.getArgumentAsInteger(1))).setIsFill(true);
                            break;
                        case RECTANGLE_SET_DIMENSIONS:
                            ((RoundRectangle) view.getFromShapes(frame.getArgumentAsInteger(1))).setWidth(frame.getArgumentAsInteger(2));
                            ((RoundRectangle) view.getFromShapes(frame.getArgumentAsInteger(1))).setHeight(frame.getArgumentAsInteger(3));
                            break;
                    }
                    break;
                case TYPE_LINE:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Line(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4),frame.getArgumentAsInteger(5)),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case LINE_SET_COORDINATES:
                            ((Line) view.getFromShapes(frame.getArgumentAsInteger(1))).setPoint1(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                    }
                    break;
                case TYPE_ELLIPSE:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Ellipse(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4),frame.getArgumentAsInteger(5),false),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case ELLIPSE_SET_RADIUS:
                            ((Ellipse) view.getFromShapes(frame.getArgumentAsInteger(1))).setRadiusX(frame.getArgumentAsInteger(2));
                            ((Ellipse) view.getFromShapes(frame.getArgumentAsInteger(1))).setRadiusX(frame.getArgumentAsInteger(3));
                            break;
                        case ELLIPSE_SET_FILL:
                            if (frame.getArgument(2)[0] == 0)
                                ((Ellipse) view.getFromShapes(frame.getArgumentAsInteger(1))).setIsFill(false);
                            else
                                ((Ellipse) view.getFromShapes(frame.getArgumentAsInteger(1))).setIsFill(true);
                            break;
                    }
                    break;
                case TYPE_LABEL:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Label(frame.getArgumentAsString(4),frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),GlcdView.TEXT_SMALL,GlcdView.FONT_ARIEL_REGULAR),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case LABEL_SET_FONT:
                            switch (frame.getArgumentAsInteger(2)){
                                case GlcdView.FONT_ARIEL_REGULAR:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextFont(GlcdView.FONT_ARIEL_REGULAR);
                                    break;
                                case GlcdView.FONT_ARIEL_BLACK:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextFont(GlcdView.FONT_ARIEL_BLACK);
                                    break;
                                case GlcdView.FONT_ARIEL_ITALIC:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextFont(GlcdView.FONT_ARIEL_ITALIC);
                                    break;
                                case GlcdView.FONT_COMICSANS:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextFont(GlcdView.FONT_COMICSANS);
                                    break;
                                case GlcdView.FONT_SERIF:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextFont(GlcdView.FONT_SERIF);
                                    break;
                            }
                            break;
                        case LABEL_SET_SIZE:
                            switch (frame.getArgumentAsInteger(2)){
                                case 0:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextSize(GlcdView.TEXT_SMALL);
                                    break;
                                case 1:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextSize(GlcdView.TEXT_MEDUIM);
                                    break;
                                case 2:
                                    ((Label) view.getFromShapes(frame.getArgumentAsInteger(1))).setTextSize(GlcdView.TEXT_LARGE);
                                    break;
                            }
                            break;
                    }
                    break;
                case TYPE_PROGRESSBAR:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new ProgressBar(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4),frame.getArgumentAsInteger(5)),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case PROGRESSBAR_SET_RANGE:
                            ((ProgressBar) view.getFromShapes(frame.getArgumentAsInteger(1))).setStart(frame.getArgumentAsInteger(2));
                            ((ProgressBar) view.getFromShapes(frame.getArgumentAsInteger(1))).setEnd(frame.getArgumentAsInteger(3));
                            break;
                        case PROGRESSBAR_SET_VALUE:
                            ((ProgressBar) view.getFromShapes(frame.getArgumentAsInteger(1))).setCurrentValue(frame.getArgumentAsInteger(2));
                            break;
                        case PROGRESSBAR_SET_DIMENSIONS:
                            ((ProgressBar) view.getFromShapes(frame.getArgumentAsInteger(1))).setWidth(frame.getArgumentAsInteger(2));
                            ((ProgressBar) view.getFromShapes(frame.getArgumentAsInteger(1))).setHeight(frame.getArgumentAsInteger(3));
                            break;
                    }
                    break;
                case TYPE_GAUGE:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new AnalogGauge(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4)),frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case GAUGE_SET_RANGE:
                            ((AnalogGauge) view.getFromShapes(frame.getArgumentAsInteger(1))).setStart(frame.getArgumentAsInteger(2));
                            ((AnalogGauge) view.getFromShapes(frame.getArgumentAsInteger(1))).setEnd(frame.getArgumentAsInteger(3));
                            break;
                        case GAUGE_SET_VALUE:
                            ((AnalogGauge) view.getFromShapes(frame.getArgumentAsInteger(1))).setCurrentValue(frame.getArgumentAsInteger(2));
                            break;
                        case GAUGE_SET_RADIUS:
                            ((AnalogGauge) view.getFromShapes(frame.getArgumentAsInteger(1))).setRadius(frame.getArgumentAsInteger(2));
                            break;
                    }
                    break;
                case TYPE_BUTTON:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Button(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), frame.getArgumentAsInteger(4), frame.getArgumentAsInteger(5), frame.getArgumentAsInteger(1), " "), frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case BUTTON_SET_TEXT:
                            ((Button) view.getFromShapes(frame.getArgumentAsInteger(1))).setText(view,frame.getArgumentAsString(2));
                            break;
                        case BUTTON_SET_DIMENSIONS:
                            ((Button) view.getFromShapes(frame.getArgumentAsInteger(1))).setWidth(frame.getArgumentAsInteger(2));
                            ((Button) view.getFromShapes(frame.getArgumentAsInteger(1))).setHeight(frame.getArgumentAsInteger(3));
                            break;
                        case BUTTON_SET_STYLE:
                            ((Button) view.getFromShapes(frame.getArgumentAsInteger(1))).setStyle(frame.getArgument(2)[0]);
                            break;
                    }
                    break;
                case TYPE_RADIOBUTTON:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new RadioButton(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), (byte) 0, frame.getArgumentAsInteger(1), frame.getArgumentAsString(4)), frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case RADIOBUTTON_SET_TEXT:
                            ((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))).setText(view,frame.getArgumentAsString(2));
                            break;
                        case RADIOBUTTON_SET_SIZE:
                            ((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))).setSize(frame.getArgument(2)[0]);
                            break;
                        case RADIOBUTTON_SET_GROUP:
//                            ((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))).setRadioGroup();
                            break;
                        case RADIOBUTTON_SELECT:
                            ((RadioButton) view.getFromShapes(frame.getArgumentAsInteger(1))).setSelected(true);
                            break;
                    }
                    break;
                case TYPE_CHECKBOX:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new CheckBox(view, frame.getArgumentAsInteger(2), frame.getArgumentAsInteger(3), (byte) 0, frame.getArgumentAsInteger(1), frame.getArgumentAsString(4)), frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case CHECKBOX_SET_TEXT:
                            ((CheckBox) view.getFromShapes(frame.getArgumentAsInteger(1))).setText(view,frame.getArgumentAsString(2));
                            break;
                        case CHECKBOX_SET_SIZE:
                            ((CheckBox) view.getFromShapes(frame.getArgumentAsInteger(1))).setSize(view,frame.getArgument(2)[0]);
                            break;
                        case CHECKBOX_SELECT:
                            ((CheckBox) view.getFromShapes(frame.getArgumentAsInteger(1))).setSelected(true);
                            break;
                        case CHECKBOX_UNSELECT:
                            ((CheckBox) view.getFromShapes(frame.getArgumentAsInteger(1))).setSelected(false);
                            break;
                    }
                    break;
                case TYPE_SLIDER:
                    switch (frame.getArgument(0)[0]){
                        case SHAPE_DRAW:
                            view.addToShapes(new Slider(view,frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3),frame.getArgumentAsInteger(4),frame.getArgumentAsInteger(5),frame.getArgumentAsInteger(1)), frame.getArgumentAsInteger(1));
                            break;
                        case SHAPE_SET_POSTION:
                            view.getFromShapes(frame.getArgumentAsInteger(1)).setPosition(frame.getArgumentAsInteger(2),frame.getArgumentAsInteger(3));
                            break;
                        case SHAPE_SET_VISIBILITY:
                            if (frame.getArgument(2)[0] == 0)
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(true);
                            else
                                view.getFromShapes(frame.getArgumentAsInteger(1)).setVisibility(false);
                            break;
                        case SLIDER_SET_RANGE:
                            ((Slider) view.getFromShapes(frame.getArgumentAsInteger(1))).setStart(frame.getArgumentAsInteger(2));
                            ((Slider) view.getFromShapes(frame.getArgumentAsInteger(1))).setEnd(frame.getArgumentAsInteger(3));
                            break;
                        case SLIDER_SET_VALUE:
                            ((Slider) view.getFromShapes(frame.getArgumentAsInteger(1))).setCurrentValue(frame.getArgumentAsInteger(2));
                            break;
                        case SLIDER_SET_DIMENSIONS:
                            ((Slider) view.getFromShapes(frame.getArgumentAsInteger(1))).setWidth(frame.getArgumentAsInteger(2));
                            ((Slider) view.getFromShapes(frame.getArgumentAsInteger(1))).setHeight(frame.getArgumentAsInteger(3));
                            break;
                    }
                    break;
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
}
