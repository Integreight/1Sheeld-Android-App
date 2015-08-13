package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public class Line implements Shape {
    float x1, y1, x2, y2;
    boolean visibility = true;

    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public void move(float newX1, float newY1) {
        setPoint2(x2 + (newX1 - x1), y2 + (newY1 - y1));
        setPoint1(newX1, newY1);
    }

    public void setPoint1(float newX1, float newY1) {
        this.x1 = newX1;
        this.y1 = newY1;
    }

    public void setPoint2(float newX2, float newY2) {
        this.x2 = newX2;
        this.y2 = newY2;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility)
            view.drawLine(x1, y1, x2, y2, GlcdShield.BLACK);
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.move(x, y);
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
