package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moustafa Nasr on 7/22/2015.
 */
public class AnalogGauge implements Shape {
    private float xCenter, yCenter, radius, start = 0, end = 100, currentValue = 0, angleStart = 2.355f, angleEnd = 7.065f;
    boolean visibility = true;

    public AnalogGauge(float xCenter, float yCenter, float radius) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;
        this.start = 0;
        this.end = 100;
        this.currentValue = 0;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility) {
            //using Linear Interpolation get the angle corresponding to the given value.
            float angle = (((currentValue - start) * (angleEnd - angleStart)) / (end - start)) + angleStart;

            view.fillCircle(xCenter, yCenter, radius, GlcdShield.WHITE);
            drawPartOfCircle(view, radius, GlcdShield.BLACK);
            drawPartOfCircle(view, (float) (radius * 0.8), GlcdShield.BLACK);
            drawPointer(view, (float) (radius * 0.7), angle, GlcdShield.BLACK);
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

    @Override
    public boolean setIsPressed(boolean isPressed) {
        return false;
    }

    @Override
    public boolean setTouched(int touchX, int touchY) {
        return false;
    }

    public void setCurrentValue(float value) {
        if (value < start)
            value = start;
        else if (value > end)
            value = end;
        this.currentValue = value;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setStart(float start) {
        this.start = start;
        setCurrentValue(currentValue);
    }

    public void setEnd(float end) {
        this.end = end;
        setCurrentValue(currentValue);
    }

    private void drawIndicator(GlcdView view, float angleInRadian, int color) {
        float x1 = (float) (xCenter + (radius * 0.8 * Math.cos(angleInRadian))), x2 = (float) (xCenter + (radius * Math.cos(angleInRadian)));
        float y1 = (float) (yCenter + (radius * 0.8 * Math.sin(angleInRadian))), y2 = (float) (yCenter + (radius * Math.sin(angleInRadian)));
        view.drawLine(x1, y1, x2, y2, color);
    }

    private void drawPointer(GlcdView view, float radius, float angleInRadian, int color) {
        float x1 = xCenter, x2 = (float) (xCenter + (radius * Math.cos(angleInRadian)));
        float y1 = yCenter, y2 = (float) (yCenter + (radius * Math.sin(angleInRadian)));
        view.drawLine(x1, y1, x2, y2, color);
    }

    private void drawPartOfCircle(GlcdView view, float radius, int color) {
        float x = xCenter - radius;
        float y = yCenter - radius;
        float width = 2 * radius, height = 2 * radius;

        if (width >= height) {
            if (radius > (width / 2))
                radius = (width / 2);
        } else {
            if (radius > (height / 2))
                radius = (height / 2);
        }

        float tSwitch;
        float x1 = 0, y1 = radius;
        tSwitch = 3 - 2 * radius;
        while (x1 <= y1) {

            view.drawPoint((int) (x + radius - x1), (int) (y + radius - y1), color);
            view.drawPoint((int) (x + radius - y1), (int) (y + radius - x1), color);

            view.drawPoint((int) (x + width - radius + x1), (int) (y + radius - y1), color);
            view.drawPoint((int) (x + width - radius + y1), (int) (y + radius - x1), color);

            view.drawPoint((int) (x + width - radius + y1), (int) (y + height - radius + x1), color);

            view.drawPoint((int) (x + radius - y1), (int) (y + height - radius + x1), color);

            if (tSwitch < 0)
                tSwitch += (4 * x1 + 6);
            else {
                tSwitch += (4 * (x1 - y1) + 10);
                y1--;
            }
            x1++;
        }

    }

}
