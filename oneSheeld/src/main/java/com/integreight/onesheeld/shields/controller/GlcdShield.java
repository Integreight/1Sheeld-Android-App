package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.nfc.Tag;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdShield extends ControllerParent<GlcdShield>{

    private Tag currentTag;
    private GlcdEventHandler glcdEventHandler;
    //private GlcdView glcdView;

    public GlcdShield(){}

    public GlcdShield(Activity activity, String tag) {
        super(activity, tag);
    }

    //public GlcdView getGlcdView() {
    //    return glcdView;
    //}

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
        void setView(Bitmap bitmap);
    }

    @Override
    public void reset() {

    }
}
