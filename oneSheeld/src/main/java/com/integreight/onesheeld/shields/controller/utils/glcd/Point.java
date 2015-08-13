package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public class Point implements Shape {
    float x, y;
    boolean visiblity = true;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(GlcdView view) {
        if (visiblity) {
            view.drawPoint((int) x,(int) y,GlcdShield.BLACK);
        }
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visiblity = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        return false;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return false;
    }
}
