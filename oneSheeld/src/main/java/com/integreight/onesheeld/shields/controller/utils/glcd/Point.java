package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

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
        if (visiblity) {
            List<Integer> params = new ArrayList<>();
            params.add((int) x);
            params.add((int) y);
            params.add(view.BLACK);
            List<Boolean> premissions= new ArrayList<>();
            premissions.add(true);
            premissions.add(null);
            premissions.add(null);
            premissions.add(null);
            view.doOrder(view.ORDER_SETDOT, params, premissions);
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
