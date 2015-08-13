package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public class Ellipse implements Shape {
    float xCenter, yCenter, radiusX, radiusY;
    boolean isFill;
    boolean visibility = true;

    public Ellipse(float xCenter, float yCenter, float radiusX, float radiusY, boolean isFill) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.isFill = isFill;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            if (isFill)
                view.fillEllipse(xCenter, yCenter, radiusX, radiusY, GlcdShield.BLACK);
            else
                view.drawEllipse(xCenter, yCenter, radiusX, radiusY, GlcdShield.BLACK);
        }
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.xCenter = x;
        this.yCenter = y;
    }

    public void setRadiusX(float radiusX) {
        this.radiusX = radiusX;
    }

    public void setRadiusY(float radiusY) {
        this.radiusY = radiusY;
    }

    public void setIsFill(boolean isFill) {
        this.isFill = isFill;
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
