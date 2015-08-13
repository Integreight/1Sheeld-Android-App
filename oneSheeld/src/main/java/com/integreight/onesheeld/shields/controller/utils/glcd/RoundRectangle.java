package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public class RoundRectangle implements Shape {
    float x, y, width, height, radius;
    boolean isFill;
    boolean visibility = true;

    public RoundRectangle(float x, float y, float width, float height, float radius, boolean isFill) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.isFill = isFill;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            if (isFill)
                view.fillRoundRectangle(x, y, width, height, radius, GlcdShield.BLACK);
            else
                view.drawRoundRectangle(x, y, width, height, radius, GlcdShield.BLACK);
        }
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setRadius(float radius) {
        this.radius = radius;
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
