package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 7/21/2015.
 */
public interface ButtonShape extends Shape{
    void setIsPressed(boolean isPressed);
    void setTouched(int touchX,int touchY);
    void setBtnTouchId(GlcdView view,int btnTouchId);
    void applyTouch(GlcdView view);
}
