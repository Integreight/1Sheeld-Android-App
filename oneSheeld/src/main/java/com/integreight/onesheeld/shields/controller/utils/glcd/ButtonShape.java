package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public interface ButtonShape extends Shape {
    boolean setIsPressed(boolean isPressed);

    boolean setTouched(int touchX, int touchY);

    void setBtnTouchId(GlcdView view, int btnTouchId);

    void applyTouch(GlcdView view);

    void clearTouch(GlcdView view);
}
