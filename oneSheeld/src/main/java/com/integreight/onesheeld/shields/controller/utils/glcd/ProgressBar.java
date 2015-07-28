package com.integreight.onesheeld.shields.controller.utils.glcd;

import com.integreight.onesheeld.shields.controller.utils.GlcdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mouso on 7/22/2015.
 */
public class ProgressBar implements Shape{

    float x,y,width,height,start,end,currentValue;
    boolean visibility = true;

    public ProgressBar(float x,float y,float width,float height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.start = 0;
        this.end = 100;
        this.currentValue = start;
    }

//    public ProgressBar(float x,float y,float width,float height,float value){
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.start = 0;
//        this.end = 100;
//        if (value < start)
//            value = start;
//        else if (value > end)
//            value = end;
//        this.currentValue = value;
//    }

//    public ProgressBar(float x,float y,float width,float height,float start,float end){
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.start = start;
//        this.end = end;
//        this.currentValue = start;
//    }

//    public ProgressBar(float x,float y,float width,float height,float start,float end,float value){
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.start = start;
//        this.end = end;
//        if (value < start)
//            value = start;
//        else if (value > end)
//            value = end;
//        this.currentValue = value;
//    }

    public void setCurrentValue(float value) {
        if (value < start)
            value = start;
        else if (value > end)
            value = end;
        this.currentValue = value;
    }

    @Override
    public void draw(GlcdView view) {
        clearDraw(view);
        if (visibility) {
            // Using linear interpolation eguation to get the average value.
            //http://www.ajdesigner.com/phpinterpolation/linear_interpolation_equation.php
            float progress = (((currentValue - start) * (width - 5)) / (end - start)) + 5;

            view.drawRoundRectangle(x, y, width, height, 5, view.BLACK);
            view.fillRoundRectangle(x, y, progress, height, 5, view.BLACK);
        }
    }

    @Override
    public void clearDraw(GlcdView view) {
//        view.clear(view.WHITE,(int) x,(int) y,(int) width,(int) height);
        List<Integer> params = new ArrayList<>();
        params.add(view.WHITE);
        params.add((int) x);
        params.add((int) y);
        params.add((int) width);
        params.add((int) height);
        List<Boolean> premissions= new ArrayList<>();
        premissions.add(true);
        premissions.add(true);
        premissions.add(null);
        premissions.add(null);
        view.doOrder(GlcdView.ORDER_CLEAR, params, premissions);
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
    public void setIsPressed(boolean isPressed) {

    }

    @Override
    public void setTouched(int touchX, int touchY) {

    }
}
