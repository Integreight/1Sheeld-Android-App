package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Moustafa Nasr on 7/21/2015.
 */
public class Label implements Shape {
    String text = "";
    float x, y;
    int textSize = 0, textFont = 0;
    boolean visibility = true;

    public Label(String text, float x, float y, int textSize, int textFont) {
        this.x = x;
        this.y = y;
        this.text = text;
        textSize %= 15;  // max text size is 15
        this.textSize = textSize;
        this.textFont = textFont;
    }

    @Override
    public void draw(GlcdView view) {
        if (visibility)
            view.drawString(text, x, y, textSize, textFont, GlcdShield.BLACK);
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

    public void setText(String text) {
        this.text = text;
    }

    public void setTextFont(int textFont) {
        this.textFont = textFont;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
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
