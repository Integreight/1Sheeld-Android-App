package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 7/22/2015.
 */
public class Slider implements ButtonShape {

    float btnX, btnY, btnWidth, btnHeight, start, end, currentValue;
    int btnTouchId;
    boolean isPressed = false, visibility = true;

    public Slider(GlcdShield controller, float x, float y, float width, float height, int touchId) {
        this.btnX = x;
        this.btnY = y;
        this.btnWidth = width;
        this.btnHeight = height;
        this.start = 0;
        this.end = 100;
        this.btnTouchId = touchId;
        applyTouch(controller);
        this.currentValue = start;
        this.isPressed = false;
    }

    public void setCurrentValue(float value) {
        if (value < start)
            value = start;
        else if (value > end)
            value = end;
        this.currentValue = value;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            // Using linear interpolation eguation to get the average value.
            float progress = (((currentValue - start) * ((btnX + btnWidth - (btnHeight / 2)) - (btnX + (btnHeight / 2)))) / (end - start)) + (btnX + (btnHeight / 2));

            view.drawLine(btnX, btnY + (btnHeight / 2), btnX + btnWidth, btnY + (btnHeight / 2), GlcdShield.BLACK);
            view.fillCircle(progress, btnY + (btnHeight / 2), btnHeight / 2, GlcdShield.BLACK);
        }
    }

    public void setStart(float start) {
        this.start = start;
        setCurrentValue(currentValue);
    }

    public void setEnd(float end) {
        this.end = end;
        setCurrentValue(currentValue);
    }

    public void setWidth(float width) {
        this.btnWidth = width;
    }

    public void setHeight(float height) {
        this.btnHeight = height;
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setPosition(float x, float y) {
        this.btnX = x;
        this.btnY = y;
    }

    @Override
    public boolean setIsPressed(boolean isPressed) {
        return true;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        float touchedValue = (((touchX - btnX) * (end - start)) / (btnX + btnWidth - btnX)) + start;
        if (currentValue == touchedValue)
            return false;
        currentValue = touchedValue;
        return true;
    }

    @Override
    public void setBtnTouchId(GlcdShield controller, int btnTouchId) {
        this.btnTouchId = btnTouchId;
        applyTouch(controller);
    }

    @Override
    public void applyTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX));
            params.add((int) (btnY));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnHeight));
            params.add(btnTouchId);
            controller.doOrder(GlcdShield.ORDER_APPLYTOUCH, params);
        }
    }

    @Override
    public void clearTouch(GlcdShield controller) {
        if (controller != null) {
            List<Integer> params = new ArrayList<>();
            params.add((int) (btnX));
            params.add((int) (btnY));
            params.add((int) (btnX + btnWidth));
            params.add((int) (btnY + btnHeight));
            params.add(null);
            controller.doOrder(GlcdShield.ORDER_APPLYTOUCH, params);
        }
    }

}
