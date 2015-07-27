package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

/**
 * Created by Mouso on 7/21/2015.
 */
public class Point implements Shape {
    float x,y;
    boolean visiblity = true;

    public Point(float x,float y){
        this.x = x;
        this.y = y;
    }
    @Override
    public void draw(GlcdView view) {
        if (visiblity)
            view.setPixel((int) x,(int) y, view.BLACK);
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
    public void setIsPressed(boolean isPressed) {

    }

    @Override
    public void setTouched(int touchX, int touchY) {

    }
}
