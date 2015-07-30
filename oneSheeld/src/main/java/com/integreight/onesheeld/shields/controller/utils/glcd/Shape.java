package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 7/21/2015.
 */

public interface Shape{
    void draw(GlcdView view);
    void clearDraw(GlcdView view,boolean clearGraphics,boolean clearTouch);
    void setVisibility(boolean visibility);
    void setPosition(float x,float y);
    boolean setIsPressed(boolean isPressed);
    boolean setTouched(int touchX, int touchY);
}
