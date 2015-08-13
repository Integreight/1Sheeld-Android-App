package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/22/2015.
 */
public class ProgressBar implements Shape {

    float x, y, width, height, start, end, currentValue;
    boolean visibility = true;

    public ProgressBar(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.start = 0;
        this.end = 100;
        this.currentValue = start;
    }

    public void setCurrentValue(float value) {
        if (value < start)
            value = start;
        else if (value > end)
            value = end;
        this.currentValue = value;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            // Using linear interpolation eguation to get the average value.
            float progress = (((currentValue - start) * (width - 5)) / (end - start)) + 5;

            view.fillRoundRectangle(x, y, width, height, 5, GlcdShield.WHITE);
            view.drawRoundRectangle(x, y, width, height, 5, GlcdShield.BLACK);
            view.fillRoundRectangle(x, y, progress, height, 5, GlcdShield.BLACK);
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

    public void setStart(float start) {
        this.start = start;
        setCurrentValue(currentValue);
    }

    public void setEnd(float end) {
        this.end = end;
        setCurrentValue(currentValue);
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
