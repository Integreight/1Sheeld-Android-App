package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.nfc.Tag;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdShield extends ControllerParent<GlcdShield>{

    private static final byte SHIELD_ID = UIShield.GLCD_SHIELD.getId();
    private Tag currentTag;
    private GlcdEventHandler glcdEventHandler;
//    private GlcdView glcdView;

    private static final byte TYPE_GLCD = 0x00;
    private static final byte TYPE_POINT = 0x01;
    private static final byte TYPE_RECTANGLE = 0x02;
    private static final byte TYPE_LINE = 0x03;
    private static final byte TYPE_ELLIPSE = 0x04;
    private static final byte TYPE_TEXTBOX = 0x05;
    private static final byte TYPE_PROGRESSBAR = 0x06;
    private static final byte TYPE_GAUGE = 0x07;
    private static final byte TYPE_BUTTON = 0x08;
    private static final byte TYPE_RADIO_BUTTON = 0x09;
    private static final byte TYPE_CHECK_BOX = 0x0A;
    private static final byte TYPE_SLIDER = 0x0B;


    public GlcdShield(){}

    public GlcdShield(Activity activity, String tag) {
        super(activity, tag);
    }

//    public GlcdView getGlcdView() {
//        return glcdView;
//    }

    public void setCurrentTag(Tag currentTag) {
        this.currentTag = currentTag;
    }

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
            switch (frame.getFunctionId()){
                case TYPE_GLCD:
                    break;
                case TYPE_POINT:
                    break;
                case TYPE_RECTANGLE:
                    break;
                case TYPE_LINE:
                    break;
                case TYPE_ELLIPSE:
                    break;
                case TYPE_TEXTBOX:
                    break;
                case TYPE_PROGRESSBAR:
                    break;
                case TYPE_GAUGE:
                    break;
                case TYPE_BUTTON:
                    break;
                case TYPE_RADIO_BUTTON:
                    break;
                case TYPE_CHECK_BOX:
                    break;
                case TYPE_SLIDER:
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
